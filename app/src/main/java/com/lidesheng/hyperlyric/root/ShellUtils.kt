package com.lidesheng.hyperlyric.root

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ShellUtils {

    suspend fun restartSystemUI(): Boolean {
        // 尝试重启 SystemUI
        val success = execRootCmdSilent("pkill -9 com.android.systemui || killall -9 com.android.systemui")
        if (success) {
            android.os.Process.killProcess(android.os.Process.myPid())
        }
        return success
    }

    suspend fun execRootCmdSilent(cmd: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
                val exitCode = process.waitFor()
                return@withContext exitCode == 0
            } catch (_: Exception) {
                return@withContext false
            }
        }
    }

}
