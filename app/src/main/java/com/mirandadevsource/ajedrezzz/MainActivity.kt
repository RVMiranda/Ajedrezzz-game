package com.mirandadevsource.ajedrezzz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mirandadevsource.ajedrezzz.ui.game.ChessGameScreen
import com.mirandadevsource.ajedrezzz.ui.game.ChessViewModel
import com.mirandadevsource.ajedrezzz.ui.theme.AjedrezzzTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AjedrezzzTheme {
                val chessViewModel: ChessViewModel = viewModel()
                ChessGameScreen(viewModel = chessViewModel)
            }
        }
    }
}
