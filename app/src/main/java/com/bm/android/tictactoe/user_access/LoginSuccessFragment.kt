package com.bm.android.tictactoe.user_access

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.bm.android.tictactoe.R
import com.google.firebase.auth.FirebaseAuth

class LoginSuccessFragment : Fragment()  {
    private val mCallback by lazy {
        context as SignupSuccessFragmentInterface
    }

    interface SignupSuccessFragmentInterface  {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_login_success, container, false)

        Log.i("test", "user is logged in: " + FirebaseAuth.getInstance().currentUser)
        return v
    }
}