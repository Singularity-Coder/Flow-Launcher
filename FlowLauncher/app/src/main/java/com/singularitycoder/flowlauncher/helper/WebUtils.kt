package com.singularitycoder.flowlauncher.helper

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.WorkerThread
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Credit: Arunkumar
 * https://github.com/arunkumar9t2/lynket-browser
 * */

private const val ACCEPT = "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5"

// We will spoof as an iPad so that websites properly expose their shortcut icon. Even Google.com
// does not provide bigger icons when we go as Android.
private const val USER_AGENT = "Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5376e Safari/8536.25"


@WorkerThread
suspend fun htmlString(url: String): String? {
    return try {
        val urlConnection: HttpURLConnection = createUrlConnection(url, 10_000)
        val result = suspendCoroutine<String?> { continuation: Continuation<String?> ->
            urlConnection.instanceFollowRedirects = true
            val encoding = urlConnection.contentEncoding
            val inputStream: InputStream = if (encoding != null && encoding.equals("gzip", ignoreCase = true)) {
                GZIPInputStream(urlConnection.inputStream)
            } else if (encoding != null && encoding.equals("deflate", ignoreCase = true)) {
                InflaterInputStream(urlConnection.inputStream, Inflater(true))
            } else {
                urlConnection.inputStream
            }
            val enc: String = Converter.extractEncoding(urlConnection.contentType)
            val result = Converter(url).grabStringFromInputStream(inputStream, enc)
            urlConnection.disconnect()
            println("htmlStringhtmlString: $result")
            continuation.resume(result)
        }
        result
    } catch (e: Exception) {
        println("exccccc: $e")
        null
    }
}

/**
 * Provides a [HttpURLConnection] instance for the given url and timeout
 *
 * @param urlAsStr Url to create a connection for.
 * @param timeout  Timeout
 * @return [HttpURLConnection] instance.
 * @throws IOException
 */
@Throws(IOException::class)
private suspend fun createUrlConnection(urlAsStr: String, timeout: Int): HttpURLConnection {
    val urlConnection = suspendCoroutine<HttpURLConnection> { continuation: Continuation<HttpURLConnection> ->
        val url = URL(urlAsStr)
        //using proxy may increase latency
        val urlConnection = url.openConnection() as HttpURLConnection
        urlConnection.setRequestProperty("User-Agent", USER_AGENT)
        urlConnection.setRequestProperty("Accept", ACCEPT)
        // suggest respond to be gzipped or deflated (which is just another compression)
        // http://stackoverflow.com/q/3932117
        urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate")
        urlConnection.connectTimeout = timeout
        urlConnection.readTimeout = timeout
        continuation.resume(urlConnection)
    }
    return urlConnection
}

private class Converter {
    private var maxBytes = 1000000 / 2
    private var encoding: String? = null
    private var url: String? = null

    internal constructor(urlOnlyHint: String?) {
        url = urlOnlyHint
    }

    internal constructor() {}

    fun setMaxBytes(maxBytes: Int): Converter {
        this.maxBytes = maxBytes
        return this
    }

    fun getEncoding(): String {
        return if (encoding == null) "" else encoding!!.lowercase(Locale.getDefault())
    }

    fun grabStringFromInputStream(`is`: InputStream?): String {
        return grabStringFromInputStream(`is`, maxBytes, encoding)
    }

    fun grabStringFromInputStream(`is`: InputStream?, encoding: String?): String {
        return grabStringFromInputStream(`is`, maxBytes, encoding)
    }

    /**
     * reads bytes off the string and returns a string
     *
     * @param is
     * @param maxBytes The max bytes that we want to read from the input stream
     * @return String
     */
    fun grabStringFromInputStream(`is`: InputStream?, maxBytes: Int, encoding: String?): String {
        this.encoding = encoding
        // Http 1.1. standard is iso-8859-1 not utf8 :(
        // but we force utf-8 as youtube assumes it ;)
        if (this.encoding == null || this.encoding!!.isEmpty()) this.encoding = UTF8
        try {
            BufferedInputStream(`is`, K2).use { `in` ->
                val output = ByteArrayOutputStream()

                // detect encoding with the help of meta tag
                try {
                    `in`.mark(K2 * 2)
                    var tmpEncoding = detectCharset("charset=", output, `in`, this.encoding)
                    if (tmpEncoding != null) this.encoding = tmpEncoding else {
                        println("no charset found in first stage")
                        // detect with the help of xml beginning ala encoding="charset"
                        tmpEncoding = detectCharset("encoding=", output, `in`, this.encoding)
                        if (tmpEncoding != null) this.encoding = tmpEncoding else println("no charset found in second stage")
                    }
                    if (!Charset.isSupported(this.encoding)) throw UnsupportedEncodingException(this.encoding)
                } catch (e: UnsupportedEncodingException) {
                    println("Using default encoding:$encoding encoding $url")
                    this.encoding = UTF8
                }

                // SocketException: Connection reset
                // IOException: missing CR    => problem on server (probably some xml character thing?)
                // IOException: Premature EOF => socket unexpectly closed from server
                var bytesRead = output.size()
                val arr = ByteArray(K2)
                while (true) {
                    if (bytesRead >= maxBytes) {
                        println("Maxbyte of $maxBytes exceeded! Maybe html is now broken but try it nevertheless. Url: $url")
                        break
                    }
                    val n = `in`.read(arr)
                    if (n < 0) break
                    bytesRead += n
                    output.write(arr, 0, n)
                }
                return output.toString(this.encoding.toString())
            }
        } catch (e: IOException) {
            println("$e url: $url")
        }
        return ""
    }

    fun grabHeadTag(`is`: InputStream, encoding: String?): String {
        this.encoding = encoding
        // Http 1.1. standard is iso-8859-1 not utf8 :(
        // but we force utf-8 as youtube assumes it ;)
        if (this.encoding == null || this.encoding!!.isEmpty()) this.encoding = UTF8
        val headTagContents = StringBuilder()
        try {
            InputStreamReader(`is`, encoding).use { inputStreamReader ->
                BufferedReader(inputStreamReader).use { bufferedReader ->
                    var temp: String
                    var insideHeadTag = false
                    while (bufferedReader.readLine().also { temp = it } != null) {
                        if (temp.contains("<head")) {
                            insideHeadTag = true
                        }
                        if (insideHeadTag) {
                            headTagContents.append(temp)
                        }
                        if (temp.contains("</head>")) {
                            // Exit
                            break
                        }
                    }
                }
            }
        } catch (e: IOException) {
            println(e)
        }
        return headTagContents.toString()
    }

    /**
     * This method detects the charset even if the first call only returns some
     * bytes. It will read until 4K bytes are reached and then try to determine
     * the encoding
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun detectCharset(
        key: String, bos: ByteArrayOutputStream, `in`: BufferedInputStream,
        enc: String?
    ): String? {
        // Grab better encoding from stream
        val arr = ByteArray(K2)
        var nSum = 0
        while (nSum < K2) {
            val n = `in`.read(arr)
            if (n < 0) break
            nSum += n
            bos.write(arr, 0, n)
        }
        val str = bos.toString(enc)
        var encIndex = str.indexOf(key)
        val clength = key.length
        if (encIndex > 0) {
            val startChar = str[encIndex + clength]
            var lastEncIndex: Int
            if (startChar == '\'') // if we have charset='something'
                lastEncIndex = str.indexOf("'", ++encIndex + clength) else if (startChar == '\"') // if we have charset="something"
                lastEncIndex = str.indexOf("\"", ++encIndex + clength) else {
                // if we have "text/html; charset=utf-8"
                var first = str.indexOf("\"", encIndex + clength)
                if (first < 0) first = Int.MAX_VALUE

                // or "text/html; charset=utf-8 "
                var sec = str.indexOf(" ", encIndex + clength)
                if (sec < 0) sec = Int.MAX_VALUE
                lastEncIndex = Math.min(first, sec)

                // or "text/html; charset=utf-8 '
                val third = str.indexOf("'", encIndex + clength)
                if (third > 0) lastEncIndex = Math.min(lastEncIndex, third)
            }

            // re-read byte array with different encoding
            // assume that the encoding string cannot be greater than 40 chars
            if (lastEncIndex > encIndex + clength && lastEncIndex < encIndex + clength + 40) {
                val tmpEnc = encodingCleanup(str.substring(encIndex + clength, lastEncIndex))
                try {
                    `in`.reset()
                    bos.reset()
                    return tmpEnc
                } catch (ex: IOException) {
                    println("$enc Couldn't reset stream to re-read with new encoding")
                }
            }
        }
        return null
    }

    companion object {
        const val UTF8 = "UTF-8"
        const val ISO = "ISO-8859-1"
        const val K2 = 2048

        /**
         * Tries to extract type of encoding for the given content type.
         *
         * @param contentType Content type gotten from [HttpURLConnection.getContentType]
         * @return
         */
        fun extractEncoding(contentType: String?): String {
            val values: Array<String?> = contentType?.split(";".toRegex())?.toTypedArray() ?: arrayOfNulls(0)
            var charset = ""
            var value: String? = ""
            for (singleValue in values) {
                value = singleValue
                value = value?.trim { it <= ' ' }?.lowercase(Locale.getDefault())
                if (value?.startsWith("charset=") == true) charset = value.substring("charset=".length)
            }
            // http1.1 says ISO-8859-1 is the default charset
            if (charset.isEmpty()) charset = ISO
            return charset
        }

        fun encodingCleanup(str: String): String {
            val sb = StringBuilder()
            var startedWithCorrectString = false
            for (i in 0 until str.length) {
                val c = str[i]
                if (Character.isDigit(c) || Character.isLetter(c) || c == '-' || c == '_') {
                    startedWithCorrectString = true
                    sb.append(c)
                    continue
                }
                if (startedWithCorrectString) break
            }
            return sb.toString().trim { it <= ' ' }
        }
    }
}

// https://stackoverflow.com/questions/18504404/get-the-webarchive-that-by-webview-savewebarchive
fun WebView.loadLocallyArchivedWebsite(archiveFilePath: String, fileName: String) {
    loadDataWithBaseURL(null, "$archiveFilePath/$fileName.mhtml", "application/x-webarchive-xml", "UTF-8", null);
}

fun Context.getRealUrlFromWebView(
    url: String,
    onRealUrlReady: (url: String) -> Unit,
) {
    WebView(this).apply {
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, realUrl: String) {
                println("real url: $realUrl")
                onRealUrlReady.invoke(realUrl)
            }
        }
        loadUrl(url)
    }
}

fun Context.showWebPage(url: String) {
    CustomTabsIntent.Builder().build().launchUrl(this, Uri.parse(url))
}

// https://stackoverflow.com/questions/12013416/is-there-any-way-in-android-to-force-open-a-link-to-open-in-chrome
fun Activity.searchWithChrome(query: String) {
    val sanitizedQuery = query.replaceFirst("for", "").trim().replace(" ", "+")
    val url = "https://www.google.com/search?q=$sanitizedQuery"
    val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        setPackage("com.android.chrome")
    }
    try {
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    } catch (ex: ActivityNotFoundException) {
        // If Chrome not installed
        intent.setPackage(null)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
}

fun Activity.openWithChrome(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        setPackage("com.android.chrome")
    }
    try {
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    } catch (ex: ActivityNotFoundException) {
        // If Chrome not installed
        intent.setPackage(null)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
}