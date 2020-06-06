package jp.kaleidot725.sample

import android.os.Bundle
import android.speech.RecognitionListener
import android.util.Log

fun createRecognitionListenerStringStream(onResult : (String)-> Unit) : RecognitionListener {
    return object : RecognitionListener {
        // The sound level in the audio stream has changed.
        override fun onRmsChanged(rmsdB: Float) {}

        // Called when the endpointer is ready for the user to start speaking.
        override fun onReadyForSpeech(params: Bundle) {
            onResult("onReadyForSpeech")
        }

        // More sound has been received.
        override fun onBufferReceived(buffer: ByteArray) {
            onResult("onBufferReceived")
        }

        // Called when partial recognition results are available.
        override fun onPartialResults(partialResults: Bundle) {
            onResult("onPartialResults")
        }

        // Reserved for adding future events.
        override fun onEvent(eventType: Int, params: Bundle) {
            onResult("onEvent")
        }

        // The user has started to speak.
        override fun onBeginningOfSpeech() {
            onResult("onBeginningOfSpeech")
        }

        // Called after the user stops speaking.
        override fun onEndOfSpeech() {
            onResult("onEndOfSpeech")
        }

        // A network or recognition error occurred.
        override fun onError(error: Int) {
            onResult("onError")
        }

        // Called when recognition results are ready.
        override fun onResults(results: Bundle) {
            val stringArray = results.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION);
            onResult("onResults " + stringArray.toString())
        }
    }
}
