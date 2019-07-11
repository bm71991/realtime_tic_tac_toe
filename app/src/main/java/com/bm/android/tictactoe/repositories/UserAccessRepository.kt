package com.bm.android.tictactoe.repositories

import android.util.Log
import com.bm.android.tictactoe.user_access.models.FirebaseUserInfo
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction

/*************************************************************
 * Methods which do Firestore/Firebase Auth operations.
 * Return tasks which are then handled at the ViewModel level.
 ************************************************************/
class UserAccessRepository {
    private val db = FirebaseFirestore.getInstance()
    private val USER_COLLECTION = "users"
    private val TAG = "UserAccessRepository"

    /*********************
     * User Signup Methods
     ********************/
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

    /*********************
     * User Login Methods
     *******************/
    fun getUserDocument(username:String): Task<DocumentSnapshot> {
        val userDocumentRef = db.collection(USER_COLLECTION).document(username)
        return userDocumentRef.get()
    }

    fun getEmail(documentSnapshot: DocumentSnapshot):String   {
        val firebaseUserInfo = getFirebaseUserObject(documentSnapshot)
        return firebaseUserInfo.email
    }

    private fun getFirebaseUserObject(documentSnapshot: DocumentSnapshot) :FirebaseUserInfo  {
        return documentSnapshot.toObject(FirebaseUserInfo::class.java)!!
    }

    fun signInWithEmail(email:String, password: String, auth: FirebaseAuth): Task<AuthResult>   {
        return auth.signInWithEmailAndPassword(email, password)
    }
}