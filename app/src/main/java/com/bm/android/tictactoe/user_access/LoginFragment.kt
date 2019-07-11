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
import android.widget.Button
import android.widget.TextView
import com.bm.android.tictactoe.R
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment()  {
    private val mCallback by lazy {
        context as LoginFragmentInterface
    }
    private lateinit var mSignupLink:TextView

    interface LoginFragmentInterface  {
        fun onStartSignupFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_login, container, false)
        val loginButton = v.findViewById<Button>(R.id.login_button)
        mSignupLink = v.findViewById(R.id.go_to_signup)
        setLinkOnTextView()
        loginButton.setOnClickListener {

        }
        return v
    }

    fun setLinkOnTextView() {
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
}