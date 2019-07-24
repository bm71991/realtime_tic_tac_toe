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

    private var mViewAdapter: BoardAdapter? = null
    private var mViewManager: RecyclerView.LayoutManager? = null
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
        findNewGame()
    }

    private fun observeStartStatus()    {
        mViewModel.getGameStartStatus().observe(this, Observer {
            if (it == mViewModel.START_GAME)    {
                displayBoard()
                initBoard()
            } else {
                Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
            }
            mViewModel.getGameStartStatus().removeObservers(this)
        })
    }

    private fun observeGameplay()   {
        mViewModel.getGameplayStatus().observe(this, Observer {
            displayEndgame(it)
            find_another_game_btn.setOnClickListener {
                mViewModel.clear()
                /* Remove previous observer on liveData if it exists */
                mViewModel.getGameplayStatus().removeObservers(this)
                findNewGame()
            }
        })
    }

    private fun findNewGame()   {
        displayLoading()
        observeStartStatus()
        observeGameplay()
        mViewModel.findGame()
    }

    private fun initBoard() {
        if (mViewAdapter == null)   {
            mViewManager = GridLayoutManager(activity, 3)
            mViewAdapter = BoardAdapter(mViewModel.getBoardPlays(), mViewModel.itemClickHandler)
            board.apply {
                setHasFixedSize(true)
                layoutManager = mViewManager
                adapter = mViewAdapter

                /* Disables animation when a RecyclerView item is changed */
                (board.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
        } else {
            mViewAdapter!!.boardPlays = mViewModel.getBoardPlays()
            mViewAdapter!!.notifyDataSetChanged()
        }
    }

    private fun setGameTitle()  {
        game_title.text = getString(R.string.game_title,
            mViewModel.getUserDisplayName(), mViewModel.getOpponent())
    }


    private fun displayEndgame(endgameStatus:String)    {
        endgame_layout.visibility = View.VISIBLE
        game_title.visibility = View.GONE
        endgame_textview.text = getString(R.string.end_of_game, endgameStatus)
    }

    private fun displayLoading()    {
        game_title.visibility = View.GONE
        endgame_layout.visibility = View.GONE
        board_layout.visibility = View.GONE
        loading_layout.visibility = View.VISIBLE
    }

    private fun displayBoard()  {
        game_title.visibility = View.VISIBLE
        endgame_layout.visibility = View.GONE
        board_layout.visibility = View.VISIBLE
        loading_layout.visibility = View.GONE
        setGameTitle()
    }
}