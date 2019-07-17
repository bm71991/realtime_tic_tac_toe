package com.bm.android.tictactoe.game

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bm.android.tictactoe.R
import kotlinx.android.synthetic.main.fragment_game.*

class GameFragment : Fragment()  {
//    private val mCallback by lazy {
//        context as GameFragmentInterface
//    }
//
//    interface GameFragmentInterface  {
//
//    }
    private lateinit var mViewAdapter: RecyclerView.Adapter<*>
    private lateinit var mViewManager: RecyclerView.LayoutManager
    private val mViewModel by lazy {
        ViewModelProviders.of(activity!!).get(GameViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_game, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBoard()
        setGameTitle()
    }

    private fun initBoard() {
        mViewManager = GridLayoutManager(activity, 3)
        mViewAdapter = BoardAdapter(mViewModel.getBoardPlays(), mViewModel.itemClickHandler)
        board.apply {
            setHasFixedSize(true)
            layoutManager = mViewManager
            adapter = mViewAdapter
        }
    }

    private fun setGameTitle()  {
        game_title.text = getString(R.string.game_title,
            mViewModel.getUserDisplayName(), mViewModel.getOpponent())
        Log.i("test", "allowed to make move: ${mViewModel.allowedToMakeMove}")
    }
}