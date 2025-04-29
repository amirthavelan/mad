package com.example.amirapplication

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class MediaActivity : AppCompatActivity() {
    private lateinit var videoView: VideoView
    private lateinit var btnPlay: Button
    private lateinit var btnPause: Button
    private lateinit var btnStop: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media)

        videoView = findViewById(R.id.videoView)
        btnPlay = findViewById(R.id.btnPlay)
        btnPause = findViewById(R.id.btnPause)
        btnStop = findViewById(R.id.btnStop)

        val videoUri = Uri.parse("android.resource://${packageName}/${R.raw.samplevideo}")
        videoView.setVideoURI(videoUri)

        btnPlay.setOnClickListener {
            videoView.start()
        }

        btnPause.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
            }
        }

        btnStop.setOnClickListener {
            videoView.stopPlayback()
            videoView.setVideoURI(videoUri) // reset for replay
        }
    }
}
