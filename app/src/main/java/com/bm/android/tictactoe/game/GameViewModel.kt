package com.bm.android.tictactoe.game

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bm.android.tictactoe.game.models.PlayerPair
import com.bm.android.tictactoe.repositories.GameRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration

class GameViewModel: ViewModel()  {
    private val mAuth = FirebaseAuth.getInstance()
    var opponent = ""
    private var gameId = ""
    val START_GAME = "start game"
    val gameSetupCallback =  object : GameRepository.GameSetupInterface  {
        override fun onPlayerAdded(playerAdded:String, gameStartListener:ListenerRegistration?) {
            gameStartListener?.remove()
            opponent = playerAdded
            gameStartStatus.value = START_GAME
        }
    }
    private val mGameRepository = GameRepository(gameSetupCallback)
    private var gameStartStatus = MutableLiveData<String>()

    fun getGameStartStatus():LiveData<String> = gameStartStatus
    /***************************************************************
     * findGame calls matchPlayers in GameRepository, which
     * will either return code "MATCHED_PLAYERS" or MADE_NEW_PAIR.
     * If code == MATCHED_PLAYERS:
     * The playerPair of the matched players will be deleted
     * from firestore and a new Game document will be created
     * in the "games" collection. The new Game document's
     * identifier is the id field from the erased player pair,
     * which the opponent will have saved in its own
     * GameViewModel.
     * if code == MADE_NEW_PAIR:
     * A listener waits for the creation of a Game document whose
     * id is the id of the PlayerPair object which was just
     * created. From this new document, the client can obtain
     * details about the game, such as the name of the opponent
     * and which player plays first.
     *************************************************************/
    fun findGame()  {
        mGameRepository.matchPlayers()
            .addOnSuccessListener {
                Log.i("test", it.code)
                when (it.code) {
                    mGameRepository.MATCHED_PLAYERS -> {
                        onJoinGame(it.playerPair)
                        deletePlayerPair(it.playerPair)
                    }
                    mGameRepository.MADE_NEW_PAIR -> {
                        gameId = it.playerPair.id
                        mGameRepository.waitForPlayerMatchup(gameId)
                    }
                    else -> {
                    }
                }
            }
            .addOnFailureListener {
                gameStartStatus.value = it.toString()
            }
    }

    private fun deletePlayerPair(playerPair: PlayerPair)    {
        mGameRepository.deletePlayerPair(playerPair)
            .addOnSuccessListener {
                onPlayerMatch()
            }
            .addOnFailureListener {
                gameStartStatus.value = it.toString()
                //undo findGame
            }
    }

    private fun onPlayerMatch() {
        Log.i("test", "game id = " + gameId)
        mGameRepository.onPlayerMatch(gameId, PlayerPair(opponent, mAuth.currentUser!!.displayName!!))
            .addOnSuccessListener {
                gameStartStatus.value = START_GAME
            }
            .addOnFailureListener {
                gameStartStatus.value = it.toString()
                //undo deletePlayerPair and findGame
            }
    }

    /* Called in TicTacToeActivity to clear GameViewModel (in case I add "play another game" functionality) */
    fun clear()    {
        gameStartStatus = MutableLiveData()
        opponent = ""
        gameId = ""
    }

    fun onJoinGame(playerPair: PlayerPair) {
        opponent = playerPair.player1
        gameId = playerPair.id
    }
}