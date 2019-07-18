package com.bm.android.tictactoe.repositories

import android.util.Log
import com.bm.android.tictactoe.game.models.Game
import com.bm.android.tictactoe.game.models.MatchPlayersInfo
import com.bm.android.tictactoe.game.models.PlayerPair
import com.bm.android.tictactoe.game.models.WaitingPlayers
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import java.util.*

class GameRepository(private val gameplayCallback:GameplayInterface)   {
    interface GameplayInterface    {
        fun onPlayerAdded(playerAdded:String, gameStartListener: ListenerRegistration?)
        fun onGameInfoChange(gameInfo:Game, gameListener:ListenerRegistration?)
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
                        val newPair = PlayerPair(currentPair.player1, mAuth.currentUser!!.displayName!!,
                            currentPair.id)
                        transaction.update(waitingPlayersRef, "playerPairs",
                            FieldValue.arrayUnion(newPair))
                        transaction.update(waitingPlayersRef, "playerPairs", FieldValue.arrayRemove(currentPair))
                        Log.i("test", "playerPair id = " + newPair.id)
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

        val newGame = Game(playerList, "started", "", playerPair.player1, -1)
        return gameRef.set(newGame)
    }

    fun deletePlayerPair(playerPair: PlayerPair): Task<Void>    {
        return waitingPlayersRef.update("playerPairs", FieldValue.arrayRemove(playerPair))
    }

    fun waitForPlayerMatchup(gameId:String):ListenerRegistration? {
        val gameDocRef = db.collection(GAMES_COLLECTION).document(gameId)
        var gameStartListener:ListenerRegistration? = null

        gameStartListener =  gameDocRef.addSnapshotListener(
            fun(snapshot: DocumentSnapshot?, e: FirebaseFirestoreException?) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return
                }
                if (snapshot != null && snapshot.exists()) {
                    val gameInfo = snapshot.toObject(Game::class.java)
                    val playerAdded = gameInfo!!.players[1]
                    gameplayCallback.onPlayerAdded(playerAdded, gameStartListener)
                    addGameListener(gameId)
                } else {
                    Log.d(TAG, "Current data: null")
                }
            })
        return gameStartListener
    }

    fun addGameListener(gameId:String):ListenerRegistration?  {
        val gameDocRef = db.collection(GAMES_COLLECTION).document(gameId)
        var gameListener:ListenerRegistration? = null

        gameListener = gameDocRef.addSnapshotListener(
            fun(snapshot: DocumentSnapshot?, e: FirebaseFirestoreException?) {
                if (e != null) {
                    Log.d(TAG, "Listen failed.", e)
                    return
                }

                if (snapshot != null && snapshot.exists()) {
                    val gameInfo = snapshot.toObject(Game::class.java)
                    gameplayCallback.onGameInfoChange(gameInfo!!, gameListener)
                }
            })
        return gameListener
    }

    fun switchTurns(opponent:String, playIndex:Int, gameId:String):Task<Void>  {
        val gameDocRef = db.collection(GAMES_COLLECTION).document(gameId)
        return gameDocRef.update(
            mapOf<String, Any>(
                "lastPlay" to playIndex,
                "currentTurn" to opponent
            ))
    }

    fun sendGameStatusChange(opponent:String, playIndex:Int, winner:String,
                      gameId:String):Task<Void>   {
        val gameDocRef = db.collection(GAMES_COLLECTION).document(gameId)

        return gameDocRef.update(
            mapOf<String, Any>(
                "lastPlay" to playIndex,
                "currentTurn" to opponent,
                "status" to "ended",
                "winner" to winner
            ))
    }
}