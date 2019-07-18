package com.bm.android.tictactoe.game.models

/***********************************************
 * Used in GameViewModel to organize fields
 * pertaining to the game state into one
 * class.
 * ********************************************/
class GameState {
    var boardPlays = arrayListOf("", "", "", "", "", "", "", "", "")
    var gameOver = false
    var turnCount = 0
    var opponent = ""
    var gameId:String = ""
    var playerLetter:String = ""

    private fun getPlayerCount(boardIndexes:ArrayList<Int>, boardPlays: ArrayList<String>): Int {
        //counts the number of Xs or Os for a certain list of indexes on the game board
        return arrayOf(
            boardPlays[boardIndexes[0]],
            boardPlays[boardIndexes[1]],
            boardPlays[boardIndexes[2]])
            .count  {it == playerLetter}
    }

    fun playerHasWon():Boolean {
        //contains lists of all possible board indexes from which a player could win the game
        val indexesToCheck = arrayListOf(
            arrayListOf(0,1,2),
            arrayListOf(3,4,5),
            arrayListOf(6,7,8),
            arrayListOf(0,4,8),
            arrayListOf(2,4,6),
            arrayListOf(0,3,6),
            arrayListOf(1,4,7),
            arrayListOf(2,5,8)
        )
        /* check each possible win combination: if there are 3 of the player's letter in any
         of the combinations, that player has won the game
         */
        indexesToCheck.forEach  {
            if (getPlayerCount(it, boardPlays) == 3)    {
                return true
            }
        }
        return false
    }
}