package com.lidesheng.hyperlyric.service
import com.lidesheng.hyperlyric.Constants


import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.content.edit

class LyricTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    private fun updateTileState() {
        val prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(Constants.KEY_ENABLE_DYNAMIC_ISLAND, Constants.DEFAULT_ENABLE_DYNAMIC_ISLAND)
        
        val tile = qsTile ?: return
        tile.label = "HyperLyric媒体信息监听"
        if (isEnabled) {
            tile.state = Tile.STATE_ACTIVE
        } else {
            tile.state = Tile.STATE_INACTIVE
        }
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        val prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(Constants.KEY_ENABLE_DYNAMIC_ISLAND, Constants.DEFAULT_ENABLE_DYNAMIC_ISLAND)
        val nextState = !isEnabled
        
        prefs.edit { putBoolean(Constants.KEY_ENABLE_DYNAMIC_ISLAND, nextState) }
        
        val intent = Intent(this, ForegroundLyricService::class.java).apply {
            action = if (nextState) ACTION_RESUME_TOGGLED else ACTION_PAUSE_TOGGLED
        }
        startService(intent)
        
        updateTileState()
    }

    companion object {
        const val ACTION_PAUSE_TOGGLED = "com.lidesheng.hyperlyric.ACTION_PAUSE_TOGGLED"
        const val ACTION_RESUME_TOGGLED = "com.lidesheng.hyperlyric.ACTION_RESUME_TOGGLED"
    }
}
