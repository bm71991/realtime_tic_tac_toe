package com.bm.android.tictactoe.game

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
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

    interface DataChangeInterface {
        fun notifyAdapterOfChange(position:Int)
    }

    private lateinit var mViewAdapter: RecyclerView.Adapter<*>
    private lateinit var mViewManager: RecyclerView.LayoutManager
    private val mViewModel by lazy {
        ViewModelProviders.of(activity!!).get(GameViewModel::class.java)
            .also {
                it.dataChangeCallback = object : DataChangeInterface {
                    override fun notifyAdapterOfChange(position: Int) {
                        board.adapter?.notifyItemChanged(position)
                    }
                }
            }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_game, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBoard()
        setGameTitle()
        mViewModel.getGameplayStatus().observe(this, Observer {
            endgame_layout.visibility = View.VISIBLE
            game_title.visibility = View.GONE
            endgame_textview.text = getString(R.string.end_of_game, it)
            find_another_game_btn.setOnClickListener {

            }
        })
    }

    private fun initBoard() {
        mViewManager = GridLayoutManager(activity, 3)
        mViewAdapter = BoardAdapter(mViewModel.getBoardPlays(), mViewModel.itemClickHandler)
        board.apply {
            setHasFixedSize(true)
            layoutManager = mViewManager
            adapter = mViewAdapter
        }
        /* Disables animation when a RecyclerView item is changed */
        (board.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }

    private fun setGameTitle()  {
        game_title.text = getString(R.string.game_title,
            mViewModel.getUserDisplayName(), mViewModel.getOpponent())
    }
}