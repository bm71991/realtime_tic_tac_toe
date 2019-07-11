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

class SignupSuccessFragment : Fragment()  {
    private val mCallback by lazy {
        context as SignupSuccessFragmentInterface
    }

    interface SignupSuccessFragmentInterface  {
        fun onStartLoginFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.i("test", "user is logged in: " + FirebaseAuth.getInstance().currentUser)
        val v = inflater.inflate(R.layout.fragment_signup_success, container, false)
        val backToLoginButton = v.findViewById<Button>(R.id.back_to_login_button)

        backToLoginButton.setOnClickListener {
            mCallback.onStartLoginFragment()
        }
        return v
    }
}
