package jp.kaleidot725.sample

import android.os.Bundle
import android.speech.RecognitionListener
import android.util.Log

fun createRecognitionListenerStringStream(onResult : (String)-> Unit) : RecognitionListener {
    return object : RecognitionListener {
        override fun onRmsChanged(rmsdB: Float) {}

        override fun onReadyForSpeech(params: Bundle) {
            onResult("onReadyForSpeech")
        }

        override fun onBufferReceived(buffer: ByteArray) {
            onResult("onBufferReceived")
        }

        override fun onPartialResults(partialResults: Bundle) {
            onResult("onPartialResults")
        }

        override fun onEvent(eventType: Int, params: Bundle) {
            onResult("onEvent")
        }

        override fun onBeginningOfSpeech() {
            onResult("onBeginningOfSpeech")
        }

        override fun onEndOfSpeech() {
            onResult("onEndOfSpeech")
        }

        override fun onError(error: Int) {
            onResult("onError")
        }

        override fun onResults(results: Bundle) {
            val stringArray = results.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION);
            onResult("onResults " + stringArray.toString())
        }
    }
}
