package jp.kaleidot725.sample

import android.Manifest.permission.RECORD_AUDIO
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val recognizeSpeechIntent : Intent get() =  Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        this.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        this.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, applicationContext.packageName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestRecordAudioPermissions()

        val recognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext)
        recognizer.setRecognitionListener(createRecognitionListenerStringStream {
            recognize_text_view.text = it
        })
        recognize_button.setOnClickListener {
            recognizer.startListening(recognizeSpeechIntent)
        }
    }

    private fun requestRecordAudioPermissions() {
        val granted = ContextCompat.checkSelfPermission(this, RECORD_AUDIO)
        if (granted == PackageManager.PERMISSION_GRANTED) {
            return
        }
        ActivityCompat.requestPermissions(this, arrayOf(RECORD_AUDIO), PERMISSIONS_RECORD_AUDIO)
    }

    companion object {
        private const val PERMISSIONS_RECORD_AUDIO = 1000
    }
}
