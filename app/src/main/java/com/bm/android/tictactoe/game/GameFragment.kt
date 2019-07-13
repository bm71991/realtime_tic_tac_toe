package com.bm.android.tictactoe.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bm.android.tictactoe.R

class GameFragment : Fragment()  {
//    private val mCallback by lazy {
//        context as SignupSuccessFragmentInterface
//    }
//
//    interface SignupSuccessFragmentInterface  {
//
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_game, container, false)

        return v
    }
}