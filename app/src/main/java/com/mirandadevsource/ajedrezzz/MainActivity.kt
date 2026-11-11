package com.mirandadevsource.ajedrezzz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mirandadevsource.ajedrezzz.domain.storage.SharedPrefsGameStorage
import com.mirandadevsource.ajedrezzz.ui.game.AppScreen
import com.mirandadevsource.ajedrezzz.ui.game.ChessGameScreen
import com.mirandadevsource.ajedrezzz.ui.game.ChessViewModel
import com.mirandadevsource.ajedrezzz.ui.menu.MainMenuScreen
import com.mirandadevsource.ajedrezzz.ui.theme.AjedrezzzTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AjedrezzzTheme {
                val context = LocalContext.current
                val chessViewModel: ChessViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            val storage = SharedPrefsGameStorage(context.applicationContext)
                            return ChessViewModel(gameStorage = storage) as T
                        }
                    }
                )
                val uiState by chessViewModel.uiState.collectAsStateWithLifecycle()

                when (uiState.currentScreen) {
                    AppScreen.MAIN_MENU -> {
                        MainMenuScreen(
                            state = uiState,
                            onEvent = chessViewModel::onEvent
                        )
                    }
                    AppScreen.GAME -> {
                        ChessGameScreen(viewModel = chessViewModel)
                    }
                }
            }
        }
    }
}
