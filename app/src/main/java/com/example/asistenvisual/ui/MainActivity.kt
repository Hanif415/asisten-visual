package com.example.asistenvisual.ui

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.asistenvisual.databinding.ActivityMainBinding
import com.example.asistenvisual.ui.camera.CameraActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.SplashScreenImage.setOnClickListener {
            if (tts!!.isSpeaking) {
                tts?.stop()
            }
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }

        initTTS()

    }

    private fun initTTS() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts!!.setLanguage(Locale("id", "ID"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "The Language specified is not supported!")
                }
                loud("Selamat datang, saya siap untuk membantu anda sebagai asisten visual anda. Silahkan ketuk layar untuk memulai")
            } else {
                Log.e("TTS", "Initialization Failed!")
            }
        }

    }

    private fun loud(text: String) {
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onRestart() {
        super.onRestart()
        if (tts!!.isSpeaking) {
            tts?.stop()
        }
        loud("Silahkan ketuk layar untuk memulai")
    }
}