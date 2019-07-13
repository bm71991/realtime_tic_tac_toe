package com.bm.android.tictactoe.game

import android.util.Log
import androidx.lifecycle.ViewModel
import com.bm.android.tictactoe.game.models.PlayerPair
import com.bm.android.tictactoe.repositories.GameRepository
import com.google.firebase.auth.FirebaseAuth

class GameViewModel: ViewModel()  {
    private val mAuth = FirebaseAuth.getInstance()
    private var opponent = ""
    private var gameId = ""
    val gameSetupCallback =  object : GameRepository.GameSetupInterface  {
        override fun onGameFound() {

        }

        override fun onOpponentFound(playerPair: PlayerPair) {
            opponent = playerPair.player1
            gameId = playerPair.id
        }
    }
    private val mGameRepository = GameRepository(gameSetupCallback)
    
    fun findGame()  {
        mGameRepository.matchPlayers()
            .addOnSuccessListener {
                Log.i("test", it.code)
                when (it.code) {
                    mGameRepository.MATCHED_PLAYERS -> { deletePlayerPair(it.playerPair) }
                    mGameRepository.MADE_NEW_PAIR -> { }
                    else -> { }
                }
            }
            .addOnFailureListener {
                Log.i("test",  it.toString())
            }
    }

    fun deletePlayerPair(playerPair: PlayerPair)    {
        mGameRepository.deletePlayerPair(playerPair)
            .addOnSuccessListener {
                onPlayerMatch()
            }
            .addOnFailureListener {
                Log.i("test", it.toString())
            }
    }

    fun onPlayerMatch() {
        Log.i("test", "game id = " + gameId)
        mGameRepository.onPlayerMatch(gameId, PlayerPair(opponent, mAuth.currentUser!!.displayName!!))
            .addOnSuccessListener {
                Log.i("test", "new game document should have been added")
            }
            .addOnFailureListener {
                Log.i("test", it.toString())
            }
    }
}