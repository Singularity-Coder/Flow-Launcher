package com.singularitycoder.flowlauncher.today.worker

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.singularitycoder.flowlauncher.helper.db.FlowDatabase
import com.singularitycoder.flowlauncher.today.dao.TrendingTweetDao
import com.singularitycoder.flowlauncher.helper.constants.KEY_IS_WORK_COMPLETE
import com.singularitycoder.flowlauncher.today.model.TrendingTweet
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URL


class TrendingTweetsWorker(val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    var twitterTrendingHtml: String? = null

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DbEntryPoint {
        fun db(): FlowDatabase
    }

    override suspend fun doWork(): Result {
        return withContext(IO) {
            val appContext = context.applicationContext ?: throw IllegalStateException()
            val dbEntryPoint = EntryPointAccessors.fromApplication(appContext, DbEntryPoint::class.java)
            val dao = dbEntryPoint.db().trendingTweetsDao()

            try {
                scrapeTwitterForTrendingTweetsFromWebView(dao)
                Result.success(sendResult(isWorkComplete = true))
            } catch (e: Exception) {
                if (e is HttpStatusException) println("Error status: ${e.statusCode}")
                println("Exception: $e")
                Result.failure()
            }
        }
    }

    // Twitter load in webview with the below settings. Now all we need to do is somehow save the page locally and extract text
    // https://stackoverflow.com/questions/2376471/how-do-i-get-the-web-page-contents-from-a-webview
    @SuppressLint("SetJavaScriptEnabled")
    private suspend fun scrapeTwitterForTrendingTweetsFromWebView(dao: TrendingTweetDao) {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            addJavascriptInterface(TwitterJavaScriptInterface(), "HTMLOUT") // Register a new JavaScript interface called HTMLOUT
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    // This call inject JavaScript into the page which just finished loading.
//                    loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');")
                    loadUrl("javascript:window.HTMLOUT.processHTML($twitterTrendingHtml);")
                }
            }
            loadUrl("https://twitter.com/explore/tabs/trending")
        }
        Jsoup.parse(twitterTrendingHtml).apply {
            dao.deleteAll()
            val elementList = getElementsByClass("css-1dbjc4n r-16y2uox r-bnwqim") // This is the list item class and not the entire list class
            println("sizeeee: " + elementList.size)
            for (i in 0..elementList.size) {
                val hashTag = getElementsByClass("css-901oao css-16my406 r-poiln3 r-bcqeeo r-qvutc0").eq(i).text()
                val tweetCount = getElementsByClass("css-901oao css-16my406 r-poiln3 r-bcqeeo r-qvutc0").eq(i).text()
                val link = "https://twitter.com/search?q=%23$hashTag&src=trend_click&vertical=trends"

                println(
                    """
                       hashTag: $hashTag
                       tweetCount: $tweetCount
                       link: $link
                    """.trimIndent()
                )

                if (hashTag.isNullOrBlank()) continue

                dao.insert(
                    TrendingTweet(
                        hashTag = hashTag,
                        tweetCount = tweetCount,
                        link = link
                    )
                )
            }
        }
    }

    /* An instance of this class will be registered as a JavaScript interface */
    inner class TwitterJavaScriptInterface {
        @JavascriptInterface
        fun processHTML(html: String?) {
            twitterTrendingHtml = html
            println("twitterTrendingHtml: $twitterTrendingHtml")
        }
    }

    // https://jsoup.org/cookbook/extracting-data/selector-syntax
    // https://www.twilio.com/blog/working-with-html-on-the-web-java-jsoup
    // https://stackoverflow.com/questions/7488872/page-content-is-loaded-with-javascript-and-jsoup-doesnt-see-it
    // https://github.com/HtmlUnit/htmlunit-android
    // The problem with standard approach is that twitter doesnt run on browsers without javascript support
    @SuppressLint("SetJavaScriptEnabled")
    private suspend fun scrapeTwitterForTrendingTweets(dao: TrendingTweetDao) {
        // load page using HTML Unit and fire scripts
        val webClient = WebClient().apply {
            options.isJavaScriptEnabled = true
            options.isCssEnabled = false
            options.isUseInsecureSSL = true
            options.isThrowExceptionOnFailingStatusCode = false
            cookieManager.isCookiesEnabled = true
            ajaxController = NicelyResynchronizingAjaxController()
            // Wait time
            waitForBackgroundJavaScript(15000)
            options.isThrowExceptionOnScriptError = false
        }
        val twitterWebPage: HtmlPage = webClient.getPage(URL("https://twitter.com/explore/tabs/trending"))
        // convert page to generated HTML and convert to document
        Jsoup.parse(twitterWebPage.asXml()).apply {
            printWebPage()
            dao.deleteAll()
            val elementList = getElementsByClass("css-1dbjc4n r-16y2uox r-bnwqim") // This is the list item class and not the entire list class
            println("sizeeee: " + elementList.size)
            for (i in 0..elementList.size) {
                val hashTag = getElementsByClass("css-901oao css-16my406 r-poiln3 r-bcqeeo r-qvutc0").eq(i).text()
                val tweetCount = getElementsByClass("css-901oao css-16my406 r-poiln3 r-bcqeeo r-qvutc0").eq(i).text()
                val link = "https://twitter.com/search?q=%23$hashTag&src=trend_click&vertical=trends"

                println(
                    """
                       hashTag: $hashTag
                       tweetCount: $tweetCount
                       link: $link
                    """.trimIndent()
                )

                if (hashTag.isNullOrBlank()) continue

                dao.insert(
                    TrendingTweet(
                        hashTag = hashTag,
                        tweetCount = tweetCount,
                        link = link
                    )
                )
            }
        }
    }

    private fun Document.printWebPage() {
        body().childNodes().forEach { node ->
            println("dafasgasgdsgsad: $node")
        }
        body().childNodes().forEach(System.out::println)
    }

    private fun sendResult(isWorkComplete: Boolean): Data = Data.Builder()
        .putBoolean(KEY_IS_WORK_COMPLETE, isWorkComplete)
        .build()
}