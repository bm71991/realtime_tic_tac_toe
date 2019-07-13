package com.bm.android.tictactoe.repositories

import android.util.Log
import com.bm.android.tictactoe.game.models.PlayerPair
import com.bm.android.tictactoe.game.models.WaitingPlayers
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import java.util.*

class GameRepository(/*  val gameSetupCallback:GameSetupInterface */)    {
    interface GameSetupInterface    {
        fun onGameFound()
        fun onOpponentFound()
    }

    private val db = FirebaseFirestore.getInstance()
    private val mAuth = FirebaseAuth.getInstance()
    private val GAMES_COLLECTION = "games"
    private val TAG = "GameAccessRepository"
    val waitingPlayersRef = db.collection(GAMES_COLLECTION)
        .document("waitingPlayers")

    fun findGame() /*  Task<Transaction> */ {
        /*
        	snapshot = get(player_pairs)
	if (snapshot.exists())	{
		loop through pair_array	(while i < array.length && gameFound == false){
			if (pair.player2 == "")	{
			  gameFound = true
			  pair.player2 = this.username
			  make new game document with id = pair.id
		          /********put in interface
			  viewModel.opponent = pair.player1
			  viewModel.gameId = pair.id
			  mCallback.toGameFragment
			  ************/
			}
		}
	}
	if (!snapshot.exists() || gameFound == false)	{
	  add a new pair to the array: player1 = this.username, pair.id = new UUID, currentTurn: this.username
          mViewModel.gameId = pair.id
	  db.collection("games").document(pair.id)
	  .addSnapshotListener()  {
	    if (snapshot.exists()) {
              viewmodel.opponent = pair.player2
              mCallback.toGameFragment()
            }
	  }
	}
         */
        matchPlayers()
        .addOnSuccessListener {
            Log.i("test", it)
        }
        .addOnFailureListener {
            Log.i("test", it.toString())
        }
    }

    fun matchPlayers():Task<String> {
        return db.runTransaction(
            fun(transaction: Transaction): String {
            val snapshot = transaction.get(waitingPlayersRef)
            if (snapshot.exists()) {
                val playerPairs = snapshot.toObject(WaitingPlayers::class.java)!!.playerPairs
                var isFound = false
                var i = 0

                while (i < playerPairs.size && !isFound) {
                    var currentPair = playerPairs[i]
                    if (currentPair.player2 == "") {
                        val newPair = PlayerPair(currentPair.player1, "bar", currentPair.id)
                        transaction.update(
                            waitingPlayersRef, "playerPairs",
                            FieldValue.arrayUnion(newPair))
                        transaction.update(waitingPlayersRef, "playerPairs", FieldValue.arrayRemove(currentPair))
                        return "matched players"
                    }
                    i++
                }
                if (!isFound) {
                    val newPair = PlayerPair("bar", "", UUID.randomUUID().toString())
                    transaction.update(waitingPlayersRef, "playerPairs", FieldValue.arrayUnion(newPair))
                    return "made new pair"
                }
            }
            else {
                throw Exception(
                    "Document \"waitingPlayers\" does not exist, though it should have been" + " created already.")
            }
            return "exception"
        })

//        fun matchPlayers(): Task<Transaction> {
//            return db.runTransaction { transaction ->
//                Log.i("test", " snapshot exists")
//                // add a new pair to the array: player1 = this.username, pair.id = new UUID, currentTurn: username
//                val snapshot = transaction.get(waitingPlayersRef)
//                if (snapshot.exists())  {
//                    val newPlayerPair = PlayerPair("player1", "", UUID.randomUUID().toString())
//                    transaction.update(waitingPlayersRef, "playerPairs",
//                        FieldValue.arrayUnion(newPlayerPair))
//                } else {
//                    throw Exception("Document \"waitingPlayers\" does not exist, though it should have been" +
//                            " created already.")
//                }
//            }
    }









}