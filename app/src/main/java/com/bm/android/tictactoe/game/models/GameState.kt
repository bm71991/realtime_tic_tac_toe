package com.bm.android.tictactoe.game.models

class GameState {
    var boardPlays = arrayListOf("", "", "", "", "", "", "", "", "")
    var gameOver = false
    var turnCount = 0
    var opponent = ""
    var gameId:String = ""
    var playerLetter:String = ""
}