package com.bm.android.tictactoe.game.models

data class Game(val players:ArrayList<String>, var status:String,
                var winner:String, var currentTurn:String)