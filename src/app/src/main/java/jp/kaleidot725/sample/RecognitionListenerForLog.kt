package jp.kaleidot725.sample

import android.os.Bundle
import android.speech.RecognitionListener
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

fun createRecognitionListenerForLog(tag: String) : RecognitionListener {
    return object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle) {
            Log.v(tag, "onReadyForSpeech")
        }

        override fun onRmsChanged(rmsdB: Float) {
            Log.v(tag, "onRmsChanged")
        }

        override fun onBufferReceived(buffer: ByteArray) {
            Log.v(tag, "onBufferReceived")
        }

        override fun onPartialResults(partialResults: Bundle) {
            Log.v(tag, "onPartialResults")
        }

        override fun onEvent(eventType: Int, params: Bundle) {
            Log.v(tag, "onEvent")
        }

        override fun onBeginningOfSpeech() {
            Log.v(tag, "onBeginningOfSpeech")
        }

        override fun onEndOfSpeech() {
            Log.v(tag, "onEndOfSpeech")
        }

        override fun onError(error: Int) {
            Log.v(tag, "onError")
        }

        override fun onResults(results: Bundle) {
            val string = results.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION);
            Log.v(tag, "onResults " + string.toString())
        }
    }
}
