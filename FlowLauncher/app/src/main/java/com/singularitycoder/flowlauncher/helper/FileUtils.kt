package com.singularitycoder.flowlauncher.helper

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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
        directory?.listFiles()?.forEach files@{ it: File? ->
            it ?: return@files
            if (it.name.contains(withName)) {
                if (it.exists()) it.delete()
            }
        }

        withContext(Dispatchers.Main) { onDone.invoke() }
    }
}
