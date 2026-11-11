package com.mirandadevsource.ajedrezzz.domain.storage

import android.content.Context
import com.mirandadevsource.ajedrezzz.domain.ai.BotDifficulty
import com.mirandadevsource.ajedrezzz.domain.model.MoveLogItem
import com.mirandadevsource.ajedrezzz.ui.game.GameMode
import org.json.JSONArray
import org.json.JSONObject

interface GameStorage {
    fun saveGame(state: SavedGameState)
    fun loadGame(): SavedGameState?
    fun clearGame()
    fun hasSavedGame(): Boolean
}

class InMemoryGameStorage(private var state: SavedGameState? = null) : GameStorage {
    override fun saveGame(state: SavedGameState) { this.state = state }
    override fun loadGame(): SavedGameState? = state
    override fun clearGame() { state = null }
    override fun hasSavedGame(): Boolean = state != null
}

class SharedPrefsGameStorage(context: Context) : GameStorage {
    private val prefs = context.getSharedPreferences("ajedrezzz_game_prefs", Context.MODE_PRIVATE)

    override fun saveGame(state: SavedGameState) {
        val json = JSONObject().apply {
            put("fen", state.fen)
            put("gameMode", state.gameMode.name)
            put("botDifficulty", state.botDifficulty.name)
            put("isFlipped", state.isFlipped)
            put("isAutoFlipEnabled", state.isAutoFlipEnabled)
            put("showThreats", state.showThreats)
            put("timestamp", state.timestamp)

            val historyArray = JSONArray()
            state.moveHistory.forEach { item ->
                val itemObj = JSONObject().apply {
                    put("num", item.moveNumber)
                    put("w", item.whiteMove)
                    if (item.blackMove != null) {
                        put("b", item.blackMove)
                    }
                }
                historyArray.put(itemObj)
            }
            put("history", historyArray)
        }

        prefs.edit().putString(KEY_SUSPENDED_GAME, json.toString()).apply()
    }

    override fun loadGame(): SavedGameState? {
        val jsonStr = prefs.getString(KEY_SUSPENDED_GAME, null) ?: return null
        return try {
            val json = JSONObject(jsonStr)
            val fen = json.getString("fen")
            val gameMode = GameMode.valueOf(json.optString("gameMode", GameMode.PASS_AND_PLAY.name))
            val botDifficulty = BotDifficulty.valueOf(json.optString("botDifficulty", BotDifficulty.EASY.name))
            val isFlipped = json.optBoolean("isFlipped", false)
            val isAutoFlipEnabled = json.optBoolean("isAutoFlipEnabled", false)
            val showThreats = json.optBoolean("showThreats", false)
            val timestamp = json.optLong("timestamp", System.currentTimeMillis())

            val history = mutableListOf<MoveLogItem>()
            val historyArray = json.optJSONArray("history")
            if (historyArray != null) {
                for (i in 0 until historyArray.length()) {
                    val itemObj = historyArray.getJSONObject(i)
                    val num = itemObj.getInt("num")
                    val w = itemObj.getString("w")
                    val b = if (itemObj.has("b")) itemObj.getString("b") else null
                    history.add(MoveLogItem(num, w, b))
                }
            }

            SavedGameState(
                fen = fen,
                gameMode = gameMode,
                botDifficulty = botDifficulty,
                moveHistory = history,
                isFlipped = isFlipped,
                isAutoFlipEnabled = isAutoFlipEnabled,
                showThreats = showThreats,
                timestamp = timestamp
            )
        } catch (e: Exception) {
            null
        }
    }

    override fun clearGame() {
        prefs.edit().remove(KEY_SUSPENDED_GAME).apply()
    }

    override fun hasSavedGame(): Boolean {
        return prefs.contains(KEY_SUSPENDED_GAME)
    }

    companion object {
        private const val KEY_SUSPENDED_GAME = "key_suspended_game"
    }
}
