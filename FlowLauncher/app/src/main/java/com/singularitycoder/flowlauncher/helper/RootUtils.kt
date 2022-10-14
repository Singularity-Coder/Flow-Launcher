package com.singularitycoder.flowlauncher.helper

import android.content.Context
import java.io.*
import java.net.DatagramSocket
import java.net.Socket


/**
 * These methods work only on rooted devices. Else they throw error 13. Permission denied.
 * https://stackoverflow.com/questions/6813322/install-uninstall-apks-programmatically-packagemanager-vs-intents
 * */

// https://stackoverflow.com/questions/6813322/install-uninstall-apks-programmatically-packagemanager-vs-intents
fun Context.uninstallAppOnRootedDevice() {
    val shellCmd = """
        rm -r /data/app/$packageName*.apk
        rm -r /data/data/$packageName
        sync
        reboot
        
        """.trimIndent()
    sudo(shellCmd)
}

// https://stackoverflow.com/questions/20932102/execute-shell-command-from-android/26654728#26654728
fun sudo(vararg strings: String) {
    try {
        val su = Runtime.getRuntime().exec("su")
        val outputStream = DataOutputStream(su.outputStream)
        for (s in strings) {
            outputStream.writeBytes(
                """
                    $s
                    
                    """.trimIndent()
            )
            outputStream.flush()
        }
        outputStream.writeBytes("exit\n")
        outputStream.flush()
        try {
            su.waitFor()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        outputStream.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun sudoForResult(vararg strings: String): String? {
    var res: String? = ""
    var outputStream: DataOutputStream? = null
    var response: InputStream? = null
    try {
        val su = Runtime.getRuntime().exec("su")
        outputStream = DataOutputStream(su.outputStream)
        response = su.inputStream
        for (s in strings) {
            outputStream.writeBytes(
                """
                    $s
                    
                    """.trimIndent()
            )
            outputStream.flush()
        }
        outputStream.writeBytes("exit\n")
        outputStream.flush()
        try {
            su.waitFor()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        res = readFully(response)
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        closeSilently(outputStream, response)
    }
    return res
}

@Throws(IOException::class)
fun readFully(`is`: InputStream?): String? {
    val baos = ByteArrayOutputStream()
    val buffer = ByteArray(1024)
    var length = 0
    while (`is`!!.read(buffer).also { length = it } != -1) {
        baos.write(buffer, 0, length)
    }
    return baos.toString("UTF-8")
}

fun closeSilently(vararg xs: Any?) {
    // Note: on Android API levels prior to 19 Socket does not implement Closeable
    for (x in xs) {
        if (x != null) {
            try {
                println("closing: $x")
                if (x is Closeable) {
                    x.close()
                } else if (x is Socket) {
                    x.close()
                } else if (x is DatagramSocket) {
                    x.close()
                } else {
                    println("cannot close: $x")
                    throw RuntimeException("cannot close $x")
                }
            } catch (e: Throwable) {
                println(e)
            }
        }
    }
}

// https://stackoverflow.com/questions/20932102/execute-shell-command-from-android/26654728#26654728
fun runShellCommand() {
    try {
        val su = Runtime.getRuntime().exec("su")
        val outputStream = DataOutputStream(su.outputStream)
        outputStream.writeBytes("screenrecord --time-limit 10 /sdcard/MyVideo.mp4\n")
        outputStream.flush()
        outputStream.writeBytes("exit\n")
        outputStream.flush()
        su.waitFor()
    } catch (e: IOException) {
        throw Exception(e)
    } catch (e: InterruptedException) {
        throw Exception(e)
    }
}