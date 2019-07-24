package com.bm.android.tictactoe.game

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bm.android.tictactoe.R
import com.bm.android.tictactoe.game.views.GridViewItem

class BoardAdapter(var boardPlays:ArrayList<String>, private val itemClickCallback:ItemClickInterface) :
    RecyclerView.Adapter<BoardAdapter.BoardItemViewHolder>() {
    interface ItemClickInterface    {
        fun onClickItem(position:Int, gridItem: GridViewItem, adapter:BoardAdapter)
    }

    class BoardItemViewHolder(val gridItem: GridViewItem) : RecyclerView.ViewHolder(gridItem)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): BoardItemViewHolder {
        val boardItemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.grid_item, parent, false) as GridViewItem
        return BoardItemViewHolder(boardItemView)
    }

    override fun onBindViewHolder(holder: BoardItemViewHolder, position: Int) {
        val gridItem = holder.gridItem

        gridItem.setBackgroundResource(getBorderId(position))
        gridItem.text = boardPlays[position]
        gridItem.setOnClickListener {
            itemClickCallback.onClickItem(position, gridItem, this)
        }
    }

    fun resetBoard(boardPlays:ArrayList<String>)    {
        this.boardPlays = boardPlays
    }

    override fun getItemCount() = boardPlays.size

    private fun getBorderId(position:Int) = when (position) {
        0,1,3,4 -> R.drawable.bottom_right_border
        2,5 ->  R.drawable.bottom_border
        6,7 ->  R.drawable.right_border
        else -> R.drawable.no_border
    }
}



