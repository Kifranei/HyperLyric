package com.lidesheng.hyperlyric.root.utils

import android.util.Log
import io.github.libxposed.api.XposedModule

private const val TAG = "HyperLyric"

var globalXposedModule: XposedModule? = null

/** INFO 级别日志，同时输出到 Logcat 和 LSPosed 管理器 */
internal fun xLog(msg: String) {
    Log.i(TAG, msg)
    globalXposedModule?.log(Log.INFO, TAG, msg)
}

/** ERROR 级别日志，同时输出到 Logcat 和 LSPosed 管理器 */
internal fun xLogError(msg: String, e: Throwable? = null) {
    val finalMsg = if (e != null) "$msg: ${e.message}" else msg
    Log.e(TAG, finalMsg)
    globalXposedModule?.log(Log.ERROR, TAG, finalMsg, e)
}

