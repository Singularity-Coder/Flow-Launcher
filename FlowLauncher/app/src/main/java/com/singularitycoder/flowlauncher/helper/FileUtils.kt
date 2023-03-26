package com.singularitycoder.flowlauncher.helper

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.OpenableColumns
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.util.*


fun File?.customPath(directory: String?, fileName: String?): String {
    var path = this?.absolutePath

    if (directory != null) {
        path += File.separator + directory
    }

    if (fileName != null) {
        path += File.separator + fileName
    }

    return path ?: ""
}

/** /data/user/0/com.singularitycoder.aniflix/files */
fun Context.internalFilesDir(
    directory: String? = null,
    fileName: String? = null,
): File = File(filesDir.customPath(directory, fileName))

/** /storage/emulated/0/Android/data/com.singularitycoder.aniflix/files */
fun Context.externalFilesDir(
    rootDir: String = "",
    subDir: String? = null,
    fileName: String? = null,
): File = File(getExternalFilesDir(rootDir).customPath(subDir, fileName))

inline fun deleteAllFilesFrom(
    directory: File?,
    withName: String,
    crossinline onDone: () -> Unit = {}
) {
    CoroutineScope(Dispatchers.Default).launch {
        directory?.listFiles()?.filter { it.exists() }?.forEach files@{ it: File? ->
            it ?: return@files
            if (it.name.contains(withName)) {
                if (it.exists()) it.delete()
            }
        }

        withContext(Dispatchers.Main) { onDone.invoke() }
    }
}

// Get path from Uri
// content resolver instance used for firing a query inside the internal sqlite database that contains all file info from android os
// projection is the set of columns u want to fetch from sqlite db
// query returns Cursor instance which is an interface which holds the data returned by the query
// So cursor holds the data and in this case it holds a single file
// cursor.moveToFirst() moves the cursor on first row, in this case only 1 row. with the cursor u can get each column data
// The 2 columns here are OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
// filesDir is the internal storage path
/** Copy file from external to internal storage */
fun Context.readFileFromExternalDbAndWriteFileToInternalDb(inputFileUri: Uri): File? {
    // Get file name and size
    val projection = arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
    val cursor = contentResolver?.query(inputFileUri, projection, null, null, null)?.also {
        it.moveToFirst() // We are in first row of the table now
    }
    val inputFileNamePositionInRow = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
    val inputFileSizePositionInRow = cursor?.getColumnIndex(OpenableColumns.SIZE)
    val inputFileName = cursor?.getString(inputFileNamePositionInRow ?: 0)
    val inputFileSize = cursor?.getLong(inputFileSizePositionInRow ?: 0)

    println(
        """
            Input File name: $inputFileName
            Input File size: $inputFileSize
        """.trimIndent()
    )

    // Copy file to internal storage
    return copyFileToInternalStorage(inputFileUri = inputFileUri, inputFileName = inputFileName ?: "")
}

fun Context.copyFileToInternalStorage(
    inputFileUri: Uri,
    inputCustomPath: String = "",
    inputFileName: String,
): File? {
    return try {
        val outputFile = if (inputCustomPath.isNotBlank()) {
            File(filesDir?.absolutePath + File.separator + inputCustomPath + File.separator + inputFileName) // Place where our input file is copied
        } else {
            File(filesDir?.absolutePath + File.separator + inputFileName) // Place where our input file is copied
        }
        val fileOutputStream = FileOutputStream(outputFile)
        val fileInputStream = contentResolver?.openInputStream(inputFileUri)
        fileOutputStream.write(fileInputStream?.readBytes())
        fileInputStream?.close()
        fileOutputStream.flush()
        fileOutputStream.close()
        outputFile
    } catch (e: IOException) {
        println(e.message)
        null
    }
}

fun Context.isOldStorageReadPermissionGranted(): Boolean {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
}

// https://stackoverflow.com/questions/15662258/how-to-save-a-bitmap-on-internal-storage
fun Bitmap?.saveToInternalStorage(
    fileName: String,
    fileDir: String,
) {
//    val root: String = Environment.getExternalStorageDirectory().absolutePath
    val directory = File(fileDir).also {
        if (it.exists().not()) it.mkdirs()
    }
    val file = File(/* parent = */ directory, /* child = */ fileName).also {
        if (it.exists().not()) it.createNewFile() else return
    }
    try {
        val out = FileOutputStream(file)
        this?.compress(Bitmap.CompressFormat.JPEG, 90, out)
        out.flush()
        out.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun deleteBitmapFromInternalStorage(
    fileName: String,
    fileDir: String,
) {
    val directory = File(fileDir).also {
        if (it.exists().not()) return
    }
    File(/* parent = */ directory, /* child = */ fileName).also {
        if (it.exists()) it.delete()
    }
}

fun File?.toBitmap(): Bitmap? {
    return BitmapFactory.decodeFile(this?.absolutePath)
}

fun Context.getHomeLayoutBlurredImageFileDir(): String {
    return "${filesDir.absolutePath}/common_images"
}

/** Checks if a volume containing external storage is available for read and write. */
fun isExternalStorageWritable(): Boolean {
    return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}

/** Checks if a volume containing external storage is available to at least read. */
fun isExternalStorageReadable(): Boolean {
    return Environment.getExternalStorageState() in setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
}

fun Cursor.isStatusSuccessful(): Boolean {
    val columnStatus = this.getColumnIndex(DownloadManager.COLUMN_STATUS)
    return this.getInt(columnStatus) == DownloadManager.STATUS_SUCCESSFUL
}

fun Cursor.fileName(): String {
    val columnTitle = this.getColumnIndex(DownloadManager.COLUMN_TITLE)
    return this.getString(columnTitle)
}

fun Cursor.uriString(): String {
    val columnUri = this.getColumnIndex(DownloadManager.COLUMN_URI)
    return this.getString(columnUri)
}

fun Cursor.localUriString(): String {
    val columnLocalUri = this.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
    return this.getString(columnLocalUri)
}

fun prepareCustomName(
    url: String,
    prefix: String,
): String {
    if (url.isBlank() || prefix.isBlank()) return "file_${UUID.randomUUID()}".sanitize()
    val validExtensionsList = listOf(".mp4", ".jpg", ".jpeg", ".gif", ".png")
    val extension = ".${url.substringAfterLast(delimiter = ".")}".toLowCase().trim()
    val validExtension = if (validExtensionsList.contains(extension)) extension else ".png"
    return prefix.sanitize() + "_" +
            url.substringAfterLast(delimiter = "/")
                .substringBeforeLast(delimiter = ".")
                .toLowCase()
                .sanitize() + validExtension
}

/**
 * The idea is to replace all special characters with underscores
 * 48 to 57 are ASCII characters of numbers from 0 to 1
 * 97 to 122 are ASCII characters of lowercase alphabets from a to z
 * https://www.w3schools.com/charsets/ref_html_ascii.asp
 * */
fun String?.sanitize(): String {
    if (this.isNullOrBlank()) return ""
    var sanitizedString = ""
    val range0to9 = '0'.code..'9'.code
    val rangeLowerCaseAtoZ = 'a'.code..'z'.code
    this.forEachIndexed { index: Int, char: Char ->
        if (char.code !in range0to9 && char.code !in rangeLowerCaseAtoZ) {
            if (sanitizedString.lastOrNull() != '_' && this.lastIndex != index) {
                sanitizedString += "_"
            }
        } else {
            sanitizedString += char
        }
    }
    return sanitizedString
}

// https://github.com/LineageOS/android_packages_apps_Jelly
fun readStringFromStream(
    inputStream: InputStream,
    encoding: String
): String {
    val reader = BufferedReader(InputStreamReader(inputStream, encoding))
    val result = StringBuilder()
    var line: String?
    while (reader.readLine().also { line = it } != null) {
        result.append(line)
    }
    return result.toString()
}

@RequiresApi(Build.VERSION_CODES.O)
fun Context.availableStorageSpace(
    storageType: StorageType = StorageType.INTERNAL,
): Long {
    val internalStorage = filesDir
    val externalStorage = getExternalFilesDir("") ?: File("")
    val storageManager = applicationContext.getSystemService<StorageManager>() ?: return 0L
    val appSpecificInternalDirUuid: UUID = storageManager.getUuidForPath(if (storageType == StorageType.INTERNAL) internalStorage else externalStorage)
    return storageManager.getAllocatableBytes(appSpecificInternalDirUuid) // Available Bytes
}

enum class StorageType {
    INTERNAL, EXTERNAL
}