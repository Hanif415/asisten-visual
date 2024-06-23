package com.example.asistenvisual.ui.chat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.asistenvisual.R
import com.example.asistenvisual.databinding.ActivityChatBinding
import java.util.Locale

class ChatActivity : AppCompatActivity() {

    private val chatViewModel: ChatViewModel by viewModels()

    private lateinit var binding: ActivityChatBinding

    private val recordAudioRequestCode = 1
    private lateinit var speechRecognizer: SpeechRecognizer

    private var mp: MediaPlayer? = null
    private var mp2: MediaPlayer? = null

    private var imageUrl: String = ""

    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkPermission()
        }

        mp = MediaPlayer.create(this, R.raw.button1)
        mp2 = MediaPlayer.create(this, R.raw.button2)
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        initTTS()

        initSpeechToText()

        // Retrieve the string extra from the Intent
        imageUrl = intent.getStringExtra("EXTRA_URL").toString()

        chatViewModel.predict(imageUrl, "gambar apakah itu?")

        chatViewModel.starting.observe(this) {
            loud("Memulai")
        }

        chatViewModel.processing.observe(this) {
            loud("Memproses")
        }

        chatViewModel.failed.observe(this) {
            loud("Terjadi kesalahan sistem. Ketuk layar untuk bertanya")
        }

        chatViewModel.result.observe(this) { result ->
            loud("$result. Silahkan ketuk layar untuk bertanya")
        }
    }

    private fun initSpeechToText() {
        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE, Locale("id", "ID").toString()
        )

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(bundle: Bundle) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(bytes: ByteArray) {}
            override fun onEndOfSpeech() {}
            override fun onError(i: Int) {
                loud("terjadi kesalahan, tolong ketuk kembali layar untuk bertanya")
            }

            override fun onResults(bundle: Bundle) {
                val data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                binding.ivMic.setImageResource(R.drawable.baseline_mic_none_24)
                mp2?.start()

                chatViewModel.predict(imageUrl, data?.get(0) ?: "gambar apakah itu?")
            }

            override fun onPartialResults(bundle: Bundle) {}
            override fun onEvent(i: Int, bundle: Bundle) {}
        })

        binding.mainView.setOnClickListener {
            if (tts.isSpeaking) {
                tts.stop()
            }
            speechRecognizer.startListening(speechRecognizerIntent)
            binding.ivMic.setImageResource(R.drawable.baseline_mic_24)
            mp?.start()
        }
    }

    private fun initTTS() {

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale("id", "ID"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "The Language specified is not supported!")
                }
            } else {
                Log.e("TTS", "Initialization Failed!")
            }
        }
    }

    private fun loud(text: String) {
        val speakThread = Thread {
            if (tts.isSpeaking) {
                tts.stop()
            }
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
        speakThread.start()
    }

    private fun checkPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.RECORD_AUDIO), recordAudioRequestCode
        )
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        mp?.release()
        mp2?.release()
        super.onDestroy()
    }
}
