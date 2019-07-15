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
    private var opponent = ""
    private var gameId = ""
    val gameSetupCallback =  object : GameRepository.GameSetupInterface  {
        override fun onPlayerAdded(playerAdded:String, gameStartListener:ListenerRegistration?) {
            gameStartListener?.remove()
            opponent = playerAdded
            gameStartStatus.value = opponent
        }
    }
    private val mGameRepository = GameRepository(gameSetupCallback)
    private var gameStartStatus = MutableLiveData<String>()

    fun getGameStartStatus():LiveData<String> = gameStartStatus
    /**********************************************************
     * findGame calls matchPlayers in GameRepository, which
     * will either return code "MATCHED_PLAYERS" or
     * "MADE_NEW_PAIR."
     * If code == MATCHED_PLAYERS:
     * The playerPair of the matched players will be deleted
     * from firestore and a new Game document will be created
     * in the "games" collection. The new Game document's
     * identifier is the id field from the erased player pair,
     * which the opponent will have saved in its own
     * GameViewModel.
     *********************************************************/
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
                Log.i("test",  it.toString())
            }
    }

    private fun deletePlayerPair(playerPair: PlayerPair)    {
        mGameRepository.deletePlayerPair(playerPair)
            .addOnSuccessListener {
                onPlayerMatch()
            }
            .addOnFailureListener {
                Log.i("test", it.toString())
            }
    }

    private fun onPlayerMatch() {
        Log.i("test", "game id = " + gameId)
        mGameRepository.onPlayerMatch(gameId, PlayerPair(opponent, mAuth.currentUser!!.displayName!!))
            .addOnSuccessListener {
                Log.i("test", "new game document should have been added")
            }
            .addOnFailureListener {
                Log.i("test", it.toString())
            }
    }


    /* Called in TicTacToeActivity to clear GameViewModel (in case I add "play another game" functionality) */
    fun clear()    {
        opponent = ""
        gameId = ""
    }

    fun onJoinGame(playerPair: PlayerPair) {
        opponent = playerPair.player1
        gameId = playerPair.id
        gameStartStatus.value = opponent
    }
}