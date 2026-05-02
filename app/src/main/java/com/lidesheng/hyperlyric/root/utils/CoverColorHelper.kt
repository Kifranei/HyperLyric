package com.lidesheng.hyperlyric.root.utils

import android.graphics.Bitmap

object CoverColorHelper {

    private var cachedKey: String? = null
    private var cachedLightColors: IntArray? = null
    private var cachedDarkColors: IntArray? = null

    fun extractColors(bitmap: Bitmap, useGradient: Boolean, songKey: String? = null): Pair<IntArray, IntArray> {
        val key = "${songKey}_${useGradient}"
        if (key == cachedKey && cachedLightColors != null && cachedDarkColors != null) {
            return Pair(cachedLightColors!!, cachedDarkColors!!)
        }

        val result = ColorExtractorImpl.extractThemePalette(bitmap, if (useGradient) 4 else 1)
        val lightColors = result.onWhiteBackground.toIntArray()
        val darkColors = result.onBlackBackground.toIntArray()

        cachedKey = key
        cachedLightColors = lightColors
        cachedDarkColors = darkColors
        return Pair(lightColors, darkColors)
    }

    fun getCachedColors(): Pair<IntArray, IntArray>? {
        val light = cachedLightColors ?: return null
        val dark = cachedDarkColors ?: return null
        return Pair(light, dark)
    }

    fun clearCache() {
        cachedKey = null
        cachedLightColors = null
        cachedDarkColors = null
    }
}
