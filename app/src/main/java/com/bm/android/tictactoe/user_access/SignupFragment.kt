package com.bm.android.tictactoe.user_access

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bm.android.tictactoe.R
import com.google.firebase.auth.FirebaseAuth

class SignupFragment : Fragment()  {
    private val mCallback by lazy {
        context as SignupFragmentInterface
    }
    private lateinit var mSignupLayout: LinearLayout
    private lateinit var mProgressBar: ProgressBar

    interface SignupFragmentInterface  {
        fun onStartSignupSuccessFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.i("test", "user is logged in (LoginFragment) : " + FirebaseAuth.getInstance().currentUser)
        val v = inflater.inflate(R.layout.fragment_signup, container, false)
        val usernameTextView = v.findViewById<TextView>(R.id.username_input)
        val emailTextView = v.findViewById<TextView>(R.id.email_input)
        val passwordTextView = v.findViewById<TextView>(R.id.password_input)
        val signupButton = v.findViewById<Button>(R.id.signup_button)
        mSignupLayout = v.findViewById(R.id.signup_layout)
        mProgressBar = v.findViewById(R.id.auth_progress_bar)

        val mViewModel = ViewModelProviders.of(activity!!).get(UserAccessViewModel::class.java)
        val signupStatus = mViewModel.getSignupStatus()

        signupStatus.observe(this, Observer {
            if (it == "User was successfully registered")   {
                mCallback.onStartSignupSuccessFragment()
            } else {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
            hideProgressBar()
        })

        signupButton.setOnClickListener {
            val username = usernameTextView.text.toString()
            val password = passwordTextView.text.toString()
            val email = emailTextView.text.toString()

            showProgressBar()
            mViewModel.signupUser(username, email, password)
        }

        return v
    }

    fun showProgressBar()   {
        mProgressBar.visibility = View.VISIBLE
        mSignupLayout.visibility = View.GONE
    }

    fun hideProgressBar()   {
        mProgressBar.visibility = View.GONE
        mSignupLayout.visibility = View.VISIBLE
    }
}