package com.bm.android.tictactoe.game

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bm.android.tictactoe.game.models.Game
import com.bm.android.tictactoe.game.models.GameState
import com.bm.android.tictactoe.game.models.PlayerPair
import com.bm.android.tictactoe.game.views.GridViewItem
import com.bm.android.tictactoe.repositories.GameRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration

class GameViewModel: ViewModel()  {

    private val mAuth = FirebaseAuth.getInstance()
    val START_GAME = "start game"
    var allowedToMakeMove = false
    private var gameState = GameState()
    private var playerMatchupListener:ListenerRegistration? = null
    private var mGameListener:ListenerRegistration? = null
    var dataChangeCallback: GameFragment.DataChangeInterface? = null
    private var gameplayStatus = MutableLiveData<String>()

    private val gameplayCallback =  object : GameRepository.GameplayInterface  {
        //This is called only if that user created a new game and is player 1
        override fun onPlayerAdded(playerAdded:String, gameStartListener:ListenerRegistration?) {
            gameStartListener?.remove()
            gameState.opponent = playerAdded
            gameState.playerLetter = "X"
            gameStartStatus.value = START_GAME
        }

        override fun onGameInfoChange(gameInfo: Game, gameListener: ListenerRegistration?) {
            if (gameInfo.status == "ended") {
                if (gameInfo.winner == "none")  {
                    gameplayStatus.value = "it is a draw."
                    Log.i("test", "it is a draw.")
                } else {
                    gameplayStatus.value = "${gameInfo.winner} won the game."
                    Log.i("test", "${gameInfo.winner} won the game.")
                }
                setGameOver()
            }

            if (gameInfo.currentTurn == getUserDisplayName())   {
                val lastPlayIndex = gameInfo.lastPlay
                if (lastPlayIndex != -1)    {
                    gameState.boardPlays[lastPlayIndex] = getOpponentLetter()
                    dataChangeCallback?.notifyAdapterOfChange(lastPlayIndex)
                    gameState.turnCount++
                }
                allowedToMakeMove = true
            }
        }

        override fun setGameListener(gameListener:ListenerRegistration?)    {
            mGameListener = gameListener
        }
    }

    private val mGameRepository = GameRepository(gameplayCallback)
    private var gameStartStatus = MutableLiveData<String>()

    fun getGameplayStatus():LiveData<String> = gameplayStatus
    fun getGameStartStatus():LiveData<String> = gameStartStatus
    fun getBoardPlays() = gameState.boardPlays
    fun getOpponent() = gameState.opponent
    fun getOpponentLetter() = if (gameState.playerLetter == "X") "O" else "X"
    fun getUserDisplayName() = mAuth.currentUser!!.displayName.toString()
    fun setGameOver()   {
        gameState.gameOver = true
        mGameListener?.remove()
    }
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
                mGameRepository.addGameListener(gameState.gameId)
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
                    gameState.playerHasWon() -> {
                        mGameRepository.sendGameStatusChange(gameState.opponent,
                            position, getUserDisplayName(), gameState.gameId)
                        //gameOverMessage.setValue(You have won the game)
                    }
                    gameState.turnCount == 9 -> {
                        mGameRepository.sendGameStatusChange(gameState.opponent,
                            position, "none", gameState.gameId)
                    }
                    else -> {
                        mGameRepository.switchTurns(gameState.opponent, position, gameState.gameId)
                        allowedToMakeMove = false
                    }
                }
            }
        }
    }

    private fun moveHasBeenPlayed(gridItem:GridViewItem) = gridItem.text != ""

    /* Called in TicTacToeActivity to clear GameViewModel (in case I add "play another game" functionality) */
    fun clear()    {
        gameStartStatus = MutableLiveData()
        gameplayStatus = MutableLiveData()
        allowedToMakeMove = false
        playerMatchupListener?.remove()
        mGameListener?.remove()
        gameState = GameState()
    }
}