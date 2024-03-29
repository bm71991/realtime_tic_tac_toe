package com.bm.android.tictactoe.user_access

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bm.android.tictactoe.repositories.UserAccessRepository
import com.google.firebase.auth.FirebaseAuth

class UserAccessViewModel : ViewModel() {
    private var signupStatus =  MutableLiveData<String>()
    private var loginStatus = MutableLiveData<String>()
    private val mAccessRepository:UserAccessRepository = UserAccessRepository()
    private val mAuth = FirebaseAuth.getInstance()
    private val TAG = "UserAccessViewModel"

    fun getSignupStatus(): LiveData<String> = signupStatus
    fun getLoginStatus(): LiveData<String> = loginStatus

    /***********************************************************************
     * Called by SignupFragment: sets off chain of methods checkFirestore
     * and setUsernameAsDisplayName for each successful task. An
     * unsuccessful task will propagate the error message up to
     * SignupFragment by setting the value of signupStatus. These methods
     * are wrappers for repository calls and handle the tasks they
     * return.
     **********************************************************************/
    fun signupUser(username:String, email:String, password:String)  {
        mAccessRepository.registerFirebaseUser(email, password, mAuth)
            .addOnSuccessListener {
                checkFirestore(username, email, password)
            }
            .addOnFailureListener {
                Log.i("test", "signupUser Fail Listener current user = " + (mAuth.currentUser))
                signupStatus.value = it.message.toString()
            }
    }

    private fun checkFirestore(username:String, email:String, password:String)  {
        mAccessRepository.checkFirestore(username, email)
            .addOnSuccessListener {
                setUsernameAsDisplayName(username)
            }
            .addOnFailureListener {
                Log.i("test", "checkFirestore Fail Listener current user = " + (mAuth.currentUser))
                undoFirebaseRegister()
                signupStatus.value = it.message.toString()
            }
    }

    private fun setUsernameAsDisplayName(username:String)  {
        mAccessRepository.setUsernameAsDisplayName(username, mAuth.currentUser!!)
            .addOnSuccessListener {
                mAuth.currentUser?.sendEmailVerification()
                mAuth.signOut()
                signupStatus.value = "User was successfully registered"
            }
            .addOnFailureListener {
                undoFirebaseRegister()
                deleteCancelledUserDocument(username)
                Log.i("test", "setUsernameAsDisplay Fail Listener current user = " + (mAuth.currentUser))
                signupStatus.value = it.message.toString()
            }
    }

    private fun deleteCancelledUserDocument(username:String)   {
        mAccessRepository.deleteUserDocument(username)
            .addOnSuccessListener {
                Log.i(TAG, "removed  cancelled document from collection \"users\"")
            }
            .addOnFailureListener {
                Log.i(TAG, "exception: could not remove cancelled document from collection \"users\" : $it")
            }
    }

    private fun undoFirebaseRegister()  {
        mAuth.currentUser?.delete()
        mAuth.signOut()
    }

    fun clearSignupStatus() {
        signupStatus = MutableLiveData()
    }

    fun clearLoginStatus()  {
        loginStatus = MutableLiveData()
    }


    /*************************
     * Called by LoginFragment
     ************************/
    fun loginUser(username:String, password:String)    {
        Log.i("test", "username = " + username)
        mAccessRepository.getUserDocument(username)
            .addOnSuccessListener {
                if (it.exists())    {
                    val email = mAccessRepository.getEmail(it)
                    loginWithEmail(email, password)
                } else {
                    /* Username does not exist */
                    loginStatus.value = "Username does not exist"
                }
            }
            .addOnFailureListener {
                /* exception with accessing database */
                loginStatus.value = it.message.toString()
            }
    }

    private fun loginWithEmail(email:String, password:String)   {
        mAccessRepository.signInWithEmail(email, password, mAuth)
            .addOnSuccessListener {
                if (mAuth.currentUser!!.isEmailVerified)    {
                    loginStatus.value = "Login was successful"
                } else {
                    mAuth.signOut()
                    loginStatus.value = "Check your email to verify your account"
                }
            }
            .addOnFailureListener {
                loginStatus.value = it.message.toString()
            }
    }

}