package com.bm.android.tictactoe.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bm.android.tictactoe.R

class GameFragment : Fragment()  {
//    private val mCallback by lazy {
//        context as SignupSuccessFragmentInterface
//    }
//
//    interface SignupSuccessFragmentInterface  {
//
//    }

    private val mViewModel by lazy  {
        ViewModelProviders.of(activity!!).get(GameViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_game, container, false)
        val opponentText = v.findViewById<TextView>(R.id.opponent_name)
        opponentText.text = "opponent: ${mViewModel.opponent}"

        return v
    }


}