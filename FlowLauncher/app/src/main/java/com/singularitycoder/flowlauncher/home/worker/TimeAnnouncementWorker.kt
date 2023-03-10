package com.singularitycoder.flowlauncher.home.worker

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.singularitycoder.flowlauncher.helper.DateType
import com.singularitycoder.flowlauncher.helper.constants.KEY_IS_WORK_COMPLETE
import com.singularitycoder.flowlauncher.helper.timeNow
import com.singularitycoder.flowlauncher.helper.toTimeOfType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.util.*

// TODO replace with time receiver when it works
class TimeAnnouncementWorker(val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    private var textToSpeech: TextToSpeech? = null

    override suspend fun doWork(): Result = withContext(IO) {
        try {
            initTextToSpeech()

            val time = timeNow toTimeOfType DateType.h_mm_a
            val hours = time.substringBefore(":")
            val minutes = time.substringAfter(":").substringBefore(" ")
            val dayPeriod = time.substringAfter(" ")
            val timeToAnnounce = "It's $hours $minutes $dayPeriod"

            withContext(Dispatchers.Main) {
                startTextToSpeech(textToSpeak = timeToAnnounce)
            }
            Result.success(sendResult(isWorkComplete = true))
        } catch (e: Exception) {
            println("Exception: $e")
            Result.failure()
        }
    }

    private fun sendResult(isWorkComplete: Boolean): Data = Data.Builder()
        .putBoolean(KEY_IS_WORK_COMPLETE, isWorkComplete)
        .build()

    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status: Int ->
            if (status == TextToSpeech.SUCCESS) {
                val result: Int? = textToSpeech?.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    println("Language not supported for Text-to-Speech!")
                }
            }
        }
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {
                println("Started reading $utteranceId")
            }

            override fun onDone(utteranceId: String) {
                println("Finished reading $utteranceId")
            }

            override fun onError(utteranceId: String) {
                println("Error reading $utteranceId")
            }
        })
    }

    private fun startTextToSpeech(textToSpeak: String) {
        val params = Bundle().apply { putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, textToSpeak /* utteranceId = */) }
        textToSpeech?.apply {
            speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params, textToSpeak)
            playSilentUtterance(1000, TextToSpeech.QUEUE_ADD, textToSpeak) // Stay silent for 1000 ms
        }
    }
}