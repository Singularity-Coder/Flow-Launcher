package com.singularitycoder.flowlauncher.helper

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import com.singularitycoder.flowlauncher.helper.constants.Broadcast
import com.singularitycoder.flowlauncher.helper.constants.IntentKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

// https://github.com/Singularity-Coder/Instant-Android/blob/master/kotlin/AndroidStorageMadness/

// Checksum - expected downloads == actual downloads
// Retry failed downloads twice and quit
// Show failed downloads in a popup
class FileDownloader(
    private val downloadItemsList: List<DownloadItem>,
    private val context: Context,
    private val fileDirectory: String,
    private val downloadTitle: String,
    private val downloadDesc: String,
    private val isOAuth: Boolean = false,
    private val oAuthHeader: String? = "",
    private val oAuthValue: String? = "",
    private val onSuccess: (downloadedItemsList: ArrayList<DownloadItem?>) -> Unit,
    private val onFailure: (downloadedItemsList: ArrayList<DownloadItem?>) -> Unit,
) {

    var downloadedItemsListFromIntent = ArrayList<DownloadItem?>()

    /** This will be called when the downloads are complete in CustomBroadcastReceiver */
    private val downloadCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != Broadcast.DOWNLOAD_COMPLETE) return
            downloadedItemsListFromIntent.add(intent.getParcelableArrayListExtra<DownloadItem>(IntentKey.DOWNLOAD_STATUS)?.first())
            if (downloadItemsList.size == downloadedItemsListFromIntent.size) {
                unregisterReceiver(context)
                onSuccess.invoke(downloadedItemsListFromIntent)
            } else {
                onFailure.invoke(ArrayList())
            }
        }
    }

    init {
        registerReceiver()
        start()
    }

    private fun start() = CoroutineScope(IO).launch {
        if (downloadItemsList.isEmpty()) return@launch
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadItemsList.forEach { downloadItem: DownloadItem ->
            val file = context.externalFilesDir(subDir = fileDirectory, fileName = downloadItem.fileName)
            println("download man file path: ${file.absolutePath}")
            if (file.exists()) return@forEach
            val downloadRequest = DownloadManager.Request(Uri.parse(downloadItem.url)).apply {
                if (isOAuth) addRequestHeader(oAuthHeader, oAuthValue)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
                setTitle(downloadTitle)
                setDescription(downloadDesc)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                setDestinationInExternalFilesDir(context, fileDirectory, downloadItem.fileName)
            }
            delay(3.seconds()) // to imitate ningen n avoid sus. All according to kekaku. Huhahaha
            downloadManager.enqueue(downloadRequest).also { downloadId: Long ->
                println("downloadId: $downloadId")
            }
        }
    }

    private fun registerReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(downloadCompleteReceiver, IntentFilter(Broadcast.DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(downloadCompleteReceiver, IntentFilter(Broadcast.DOWNLOAD_COMPLETE))
        }
    }

    private fun unregisterReceiver(context: Context) {
        context.unregisterReceiver(downloadCompleteReceiver)
    }

    @Parcelize
    data class DownloadItem(
        val url: String,
        val fileName: String,
        val isDownloaded: Boolean = false,
    ) : Parcelable
}