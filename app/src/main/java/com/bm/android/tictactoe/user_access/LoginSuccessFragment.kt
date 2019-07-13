package com.bm.android.tictactoe.user_access

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.bm.android.tictactoe.R
import com.bm.android.tictactoe.game.GameViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginSuccessFragment : Fragment()  {
    private val mCallback by lazy {
        context as LoginSuccessFragmentInterface
    }

    interface LoginSuccessFragmentInterface  {
        fun onStartGameFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_login_success, container, false)
        val mViewModel = ViewModelProviders.of(activity!!).get(GameViewModel::class.java)
        mViewModel.findGame()

        return v
    }
}