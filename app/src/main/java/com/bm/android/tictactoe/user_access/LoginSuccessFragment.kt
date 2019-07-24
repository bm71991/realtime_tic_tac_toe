package com.bm.android.tictactoe.user_access

import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bm.android.tictactoe.R
import com.bm.android.tictactoe.game.GameViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_login_success.*

class LoginSuccessFragment : Fragment()  {
    private val mCallback by lazy {
        context as LoginSuccessFragmentInterface
    }

    interface LoginSuccessFragmentInterface  {
        fun onStartGameFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login_success, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val mViewModel = ViewModelProviders.of(activity!!).get(GameViewModel::class.java)

//        mViewModel.getGameStartStatus().observe(this, Observer {
//            if (it == mViewModel.START_GAME)    {
//                mCallback.onStartGameFragment()
//            } else {
//                Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
//            }
//        })

        find_game_button.setOnClickListener {
            mCallback.onStartGameFragment()
        }
//        find_game_button.setOnClickListener {
//            displayLoading()
//            mViewModel.findGame()
//        }
    }
//
//    private fun displayLoading()    {
//        login_success_layout.visibility = View.GONE
//        loading_layout.visibility = View.VISIBLE
//    }

}