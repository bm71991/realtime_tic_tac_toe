package com.bm.android.tictactoe.user_access

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bm.android.tictactoe.R
import com.bm.android.tictactoe.repositories.GameRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment()  {
    private val mCallback by lazy {
        context as LoginFragmentInterface
    }
    private lateinit var mSignupLink:TextView
    private lateinit var mLoginLayout: LinearLayout
    private lateinit var mProgressBar: ProgressBar

    interface LoginFragmentInterface  {
        fun onStartSignupFragment()
        fun onStartLoginSuccessFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mViewModel = ViewModelProviders.of(activity!!).get(UserAccessViewModel::class.java)
        val loginStatus = mViewModel.getLoginStatus()
        val v = inflater.inflate(R.layout.fragment_login, container, false)
        val loginButton = v.findViewById<Button>(R.id.login_button)
        val usernameTextView = v.findViewById<TextView>(R.id.username_input)
        val passwordTextView = v.findViewById<TextView>(R.id.password_input)
        mLoginLayout = v.findViewById(R.id.login_layout)
        mProgressBar = v.findViewById(R.id.auth_progress_bar)


        Log.i("test", "user is logged in: " + FirebaseAuth.getInstance().currentUser)
        mSignupLink = v.findViewById(R.id.go_to_signup)
        setLinkOnTextView()

        loginButton.setOnClickListener {
            val username = usernameTextView.text.toString()
            val password = passwordTextView.text.toString()

            showProgressBar()
            mViewModel.loginUser(username, password)
        }

        loginStatus.observe(this, Observer {
            if (it == "Login was successful")   {
                mCallback.onStartLoginSuccessFragment()
            } else {
                Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
            }
            hideProgressBar()
        })

        return v
    }

    private fun setLinkOnTextView() {
        val spannableString = SpannableString(getString(R.string.go_to_signup))
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                mCallback.onStartSignupFragment()
            }
        }
        spannableString.setSpan(clickableSpan, 31, 35,
             Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        mSignupLink.text = spannableString
        mSignupLink.highlightColor = Color.TRANSPARENT
        mSignupLink.movementMethod = LinkMovementMethod.getInstance()
    }

    fun showProgressBar()   {
        mProgressBar.visibility = View.VISIBLE
        mLoginLayout.visibility = View.GONE
    }

    fun hideProgressBar()   {
        mProgressBar.visibility = View.GONE
        mLoginLayout.visibility = View.VISIBLE
    }
}