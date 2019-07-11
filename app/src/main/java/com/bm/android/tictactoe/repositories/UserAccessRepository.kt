package com.bm.android.tictactoe.repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import kotlin.Exception

/*************************************************************
 * Methods which do Firestore/Firebase Auth operations.
 * Return tasks which are then handled at the ViewModel level.
 ************************************************************/
class UserAccessRepository {
    private val db = FirebaseFirestore.getInstance()
    private val USER_COLLECTION = "users"
    private val TAG = "UserAccessRepository"

    fun registerFirebaseUser(email:String, password:String, auth:FirebaseAuth):Task<AuthResult>   {
        return auth.createUserWithEmailAndPassword(email, password)
    }

    fun checkFirestore(username:String, email:String):Task<Transaction> {
        return db.runTransaction { transaction ->
                val usernameRef = db.collection("users").document(username)
                val snapshot = transaction.get(usernameRef)

                if (!snapshot.exists()) {
                    Log.i("test", "snapshot does not exist")
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    transaction.set(usernameRef, User(uid, email))
                } else {
                    Log.i("test", "snapshot exists")
                    throw FirebaseFirestoreException(
                        "Username requested is already taken, please choose another.",
                        FirebaseFirestoreException.Code.ABORTED)
                }
            }
    }

    fun setUsernameAsDisplayName(username:String, currentUser:FirebaseUser): Task<Void>   {
        val usernameUpdate = UserProfileChangeRequest.Builder()
            .setDisplayName(username).build()
        return currentUser.updateProfile(usernameUpdate)
    }

    fun deleteUserDocument(username:String):Task<Void>    {
        val usernameDocRef = db.collection(USER_COLLECTION).document(username)
        return usernameDocRef.delete()
    }

    /* Used in checkFirestore to add a user document to collection users */
    class User(var uid:String?, var email:String)
}