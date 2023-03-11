package com.singularitycoder.flowlauncher.helper.broadcastReceivers

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.os.bundleOf
import com.singularitycoder.flowlauncher.helper.*
import com.singularitycoder.flowlauncher.helper.constants.Broadcast
import com.singularitycoder.flowlauncher.helper.constants.IntentExtra
import com.singularitycoder.flowlauncher.helper.constants.IntentKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class CustomBroadcastReceiver : BroadcastReceiver() {

    /** Data sent to [HomeFragment] */
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                clearDownloadsAfterReboot(context)
            }
            DownloadManager.ACTION_DOWNLOAD_COMPLETE -> {
                doOnDownloadComplete(context, intent)
            }
            else -> {
                val packageName = intent.data?.encodedSchemeSpecificPart
                val bundle = bundleOf(IntentKey.PACKAGE_NAME to packageName)
                context.sendCustomBroadcast(action = intent.action, bundle = bundle)
            }
        }
    }

    @SuppressLint("Range")
    private fun clearDownloadsAfterReboot(context: Context) = CoroutineScope(IO).launch {
        println("Cancelling all queued downloads of download manager since device rebooted!")
        delay(3000) // Added delay to make sure download has started
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = (DownloadManager.STATUS_FAILED or DownloadManager.STATUS_PENDING or DownloadManager.STATUS_RUNNING or DownloadManager.STATUS_PAUSED).toLong()
        val query = DownloadManager.Query().also { it: DownloadManager.Query ->
            it.setFilterById(downloadId)
        }
        val cursor = downloadManager.query(query) ?: return@launch
        try {
            while (cursor.moveToNext()) {
                downloadManager.remove(cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID)))
            }
        } catch (e: Exception) {
            println(e)
        }
        cursor.close()
    }

    @SuppressLint("Range")
    private fun doOnDownloadComplete(context: Context, intent: Intent) = CoroutineScope(IO).launch {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().also { it: DownloadManager.Query ->
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
            it.setFilterById(downloadId)
        }
        val downloadStatusIntent = Intent(Broadcast.DOWNLOAD_COMPLETE)
        val cursor = downloadManager.query(query) ?: return@launch

        try {
            if (!cursor.moveToFirst()) return@launch

            val fileName = cursor.fileName()
            val uriString = cursor.uriString()
            val localUriString = cursor.localUriString()
            val columnStatus = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)

            println("""
                    fileName: $fileName
                    uriString: $uriString
                    localUriString: $localUriString
                """.trimIndent())

            when (cursor.getInt(columnStatus)) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    println("$fileName download successful")
                    val downloadUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI))
                    val downloadFileLocalUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                    if (downloadFileLocalUri != null) {
                        val uri = Uri.parse(downloadFileLocalUri) ?: Uri.EMPTY
                        val file = File(uri.path ?: "")
                        val downloadItem = FileDownloader.DownloadItem(url = "", fileName = "", isDownloaded = true)
                        downloadStatusIntent.putParcelableArrayListExtra(IntentKey.DOWNLOAD_STATUS, ArrayList<FileDownloader.DownloadItem>().apply { add(downloadItem) })
                        context.sendBroadcast(downloadStatusIntent)
                    }
                }
                DownloadManager.STATUS_PAUSED -> println("$fileName download paused")
                DownloadManager.STATUS_PENDING -> println("$fileName download pending")
                DownloadManager.STATUS_RUNNING -> println("$fileName download running")
                DownloadManager.STATUS_FAILED -> println("$fileName download failed")
                else -> println("Unknown error ${cursor.getInt(columnStatus)} for $fileName")
            }

            cursor.close()
        } catch (e: Exception) {
            println(e.message)
        }
    }
}