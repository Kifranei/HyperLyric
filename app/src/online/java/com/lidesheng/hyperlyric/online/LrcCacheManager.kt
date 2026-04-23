package com.lidesheng.hyperlyric.online

import android.content.Context
import com.lidesheng.hyperlyric.ui.utils.Constants as UIConstants
import com.lidesheng.hyperlyric.service.Constants as ServiceConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

object LrcCacheManager {

    private const val CACHE_DIR_NAME = "online_lyrics"

    private fun getCacheDir(context: Context): File {
        val dir = File(context.cacheDir, CACHE_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun generateCacheKey(title: String, artist: String): String {
        val cleanTitle = title.replace(Regex("\\(.*?\\)|\\[.*?]|\\{.*?\\}"), "").trim().lowercase()
        val cleanArtist = artist.replace(Regex("\\(.*?\\)|\\[.*?]|\\{.*?\\}"), "").trim().lowercase()
        val input = "${cleanTitle}_$cleanArtist"
        
        val md = MessageDigest.getInstance("MD5")
        val digested = md.digest(input.toByteArray())
        return digested.joinToString("") { "%02x".format(it) }
    }

    suspend fun getLyricFromCache(context: Context, title: String, artist: String): String? = withContext(Dispatchers.IO) {
        val key = generateCacheKey(title, artist)
        val file = File(getCacheDir(context), "$key.lrc")
        if (file.exists() && file.isFile) {
            file.setLastModified(System.currentTimeMillis())
            return@withContext file.readText()
        }
        null
    }

    suspend fun saveLyricToCache(context: Context, title: String, artist: String, lrcContent: String) = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(UIConstants.PREF_NAME, Context.MODE_PRIVATE)
        val limit = prefs.getInt(ServiceConstants.KEY_ONLINE_LYRIC_CACHE_LIMIT, ServiceConstants.DEFAULT_ONLINE_LYRIC_CACHE_LIMIT)
        
        if (limit == 0) return@withContext

        val key = generateCacheKey(title, artist)
        val file = File(getCacheDir(context), "$key.lrc")
        file.writeText(lrcContent)
 
        cleanupCacheIfNecessary(context, limit)
    }

    private fun cleanupCacheIfNecessary(context: Context, limit: Int) {
        val dir = getCacheDir(context)
        val files = dir.listFiles() ?: return
 
        if (limit < 0) return
        
        if (files.size > limit) {
            val sortedFiles = files.sortedBy { it.lastModified() }
            val numToDelete = files.size - limit
            for (i in 0 until numToDelete) {
                sortedFiles[i].delete()
            }
        }
    }
}
