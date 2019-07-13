package com.bm.android.tictactoe.repositories

import android.util.Log
import com.bm.android.tictactoe.game.models.Game
import com.bm.android.tictactoe.game.models.MatchPlayersInfo
import com.bm.android.tictactoe.game.models.PlayerPair
import com.bm.android.tictactoe.game.models.WaitingPlayers
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import java.util.*

class GameRepository(private val gameSetupCallback:GameSetupInterface)   {
    interface GameSetupInterface    {
        fun onGameFound()
        fun onOpponentFound(playerPair: PlayerPair)
    }

    private val db = FirebaseFirestore.getInstance()
    private val mAuth = FirebaseAuth.getInstance()
    private val GAMES_COLLECTION = "games"
    private val TAG = "GameAccessRepository"
    val MATCHED_PLAYERS = "matched players"
    val MADE_NEW_PAIR = "made new pair"
    private val waitingPlayersRef = db.collection(GAMES_COLLECTION)
        .document("waitingPlayers")

    fun matchPlayers():Task<MatchPlayersInfo> {
        return db.runTransaction(
            fun(transaction: Transaction): MatchPlayersInfo {
            val snapshot = transaction.get(waitingPlayersRef)
            if (snapshot.exists()) {
                val playerPairs = snapshot.toObject(WaitingPlayers::class.java)!!.playerPairs
                var i = 0
                while (i < playerPairs.size) {
                    Log.i("test", "player pair size: ${playerPairs.size}")
                    var currentPair = playerPairs[i]
                    if (currentPair.player2 == "") {
                        val newPair = PlayerPair(currentPair.player1, mAuth.currentUser!!.displayName!!, currentPair.id)
                        transaction.update(
                            waitingPlayersRef, "playerPairs",
                            FieldValue.arrayUnion(newPair))
                        transaction.update(waitingPlayersRef, "playerPairs", FieldValue.arrayRemove(currentPair))
                        Log.i("test", "playerPair id = " + newPair.id)
                        gameSetupCallback.onOpponentFound(newPair)
                        return MatchPlayersInfo(MATCHED_PLAYERS, newPair)
                    }
                    i++
                }
                val newPair = PlayerPair(mAuth.currentUser!!.displayName!!, "", UUID.randomUUID().toString())
                transaction.update(waitingPlayersRef, "playerPairs", FieldValue.arrayUnion(newPair))
                return MatchPlayersInfo(MADE_NEW_PAIR, newPair)
            }
            else {
                throw Exception(
                    "Document \"waitingPlayers\" does not exist, though it should have been" + " created already.")
            }
        })
    }

    fun onPlayerMatch(gameId:String, playerPair: PlayerPair): Task<Void> {
        val gameRef = db.collection(GAMES_COLLECTION).document(gameId)
        val playerList = arrayListOf(playerPair.player1, playerPair.player2)

        val newGame = Game(playerList, "started", "", playerPair.player1)
        return gameRef.set(newGame)
    }

    fun deletePlayerPair(playerPair: PlayerPair): Task<Void>    {
        return waitingPlayersRef.update("playerPairs", FieldValue.arrayRemove(playerPair))
    }
}