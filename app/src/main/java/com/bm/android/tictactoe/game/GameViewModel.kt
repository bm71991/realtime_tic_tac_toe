package com.bm.android.tictactoe.game

import android.util.Log
import android.widget.GridView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bm.android.tictactoe.game.models.GameState
import com.bm.android.tictactoe.game.models.PlayerPair
import com.bm.android.tictactoe.game.views.GridViewItem
import com.bm.android.tictactoe.repositories.GameRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration

class GameViewModel(): ViewModel()  {

    private val mAuth = FirebaseAuth.getInstance()
    val START_GAME = "start game"
    var allowedToMakeMove = false
    private var gameState = GameState()
    private var playerMatchupListener:ListenerRegistration? = null

    private val gameSetupCallback =  object : GameRepository.GameSetupInterface  {
        //This is called only if that user created a new game and is player 1
        override fun onPlayerAdded(playerAdded:String, gameStartListener:ListenerRegistration?) {
            gameStartListener?.remove()
            gameState.opponent = playerAdded
            gameState.playerLetter = "X"
            allowedToMakeMove = true
            gameStartStatus.value = START_GAME
        }
    }

    private val mGameRepository = GameRepository(gameSetupCallback)
    private var gameStartStatus = MutableLiveData<String>()

    fun getGameStartStatus():LiveData<String> = gameStartStatus
    fun getBoardPlays() = gameState.boardPlays
    fun getOpponent() = gameState.opponent

    fun getUserDisplayName() = mAuth.currentUser!!.displayName.toString()
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
                        gameState.gameId = it.playerPair.id
                        playerMatchupListener = mGameRepository.waitForPlayerMatchup(gameState.gameId)
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
        Log.i("test", "game id = " + gameState.gameId)
        mGameRepository.onPlayerMatch(gameState.gameId, PlayerPair(gameState.opponent,
            //changed
            getUserDisplayName()))
            .addOnSuccessListener {
                gameStartStatus.value = START_GAME
            }
            .addOnFailureListener {
                gameStartStatus.value = it.toString()
                //undo deletePlayerPair and findGame
            }
    }

    //This is called only if that user joined as player 2
    private fun onJoinGame(playerPair: PlayerPair) {
        gameState.opponent = playerPair.player1
        Log.i("test", "onJoinGame, current player: ${getUserDisplayName()}, opponent = ${gameState.opponent}")
        gameState.playerLetter = "O"
        allowedToMakeMove = false
        gameState.gameId = playerPair.id
    }

    val itemClickHandler = object : BoardAdapter.ItemClickInterface {
        override fun onClickItem(position:Int, gridItem:GridViewItem, adapter:BoardAdapter ) {
            if (!gameState.gameOver && allowedToMakeMove && !moveHasBeenPlayed(gridItem)) {
                gameState.boardPlays[position] = gameState.playerLetter
                adapter.notifyItemChanged(position)
                gameState.turnCount++
                when {
                    playerHasWon() -> {
                        gameState.gameOver = true
                        mGameRepository.sendGameStatusChange(gameState.opponent,
                            position, getUserDisplayName(), gameState.gameId)
                        //gameOverMessage.setValue(You have won the game)
                    }
                    gameState.turnCount == 9 -> {
                        mGameRepository.sendGameStatusChange(gameState.opponent,
                            position, "none", gameState.gameId)
                    }
                    else -> mGameRepository.switchTurns(gameState.opponent, position, gameState.gameId)
                }
            }
        }
    }

    fun moveHasBeenPlayed(gridItem:GridViewItem) = gridItem.text != ""





    private fun getPlayerCount(boardIndexes:ArrayList<Int>, boardPlays: ArrayList<String>): Int {
        //counts the number of Xs or Os for a certain list of indexes on the game board
        return arrayOf(
            boardPlays[boardIndexes[0]],
            boardPlays[boardIndexes[1]],
            boardPlays[boardIndexes[2]])
            .count  {it == gameState.playerLetter}
    }

    fun playerHasWon():Boolean {
        //contains lists of all possible board indexes from which a player could win the game
        val indexesToCheck = arrayListOf(
            arrayListOf(0,1,2),
            arrayListOf(3,4,5),
            arrayListOf(6,7,8),
            arrayListOf(0,4,8),
            arrayListOf(2,4,6),
            arrayListOf(0,3,6),
            arrayListOf(1,4,7),
            arrayListOf(2,5,8)
        )

        /* check each possible win combination: if there are 3 of the player's letter in any
         of the combinations, that player has won the game
         */
        indexesToCheck.forEach  {
            if (getPlayerCount(it, gameState.boardPlays) == 3)    {
                return true
            }
        }
        return false
    }

    /* Called in TicTacToeActivity to clear GameViewModel (in case I add "play another game" functionality) */
    fun clear()    {
        gameStartStatus = MutableLiveData()
        allowedToMakeMove = false
        playerMatchupListener?.remove()
        gameState = GameState()
    }
}