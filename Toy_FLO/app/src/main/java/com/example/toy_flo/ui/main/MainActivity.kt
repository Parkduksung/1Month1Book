package com.example.toy_flo.ui.main

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.example.toy_flo.R
import com.example.toy_flo.base.BaseActivity
import com.example.toy_flo.databinding.ActivityMainBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.FileInputStream

class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    private val mainViewModel by viewModel<MainViewModel>()

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.viewModel = mainViewModel

        lifecycle.addObserver(mainViewModel)

        mainViewModel.viewStateLiveData.observe(this, { viewState ->
            when (viewState) {
                is MainViewModel.MainViewState.LoadMusic -> {
                    try {
                        mediaPlayer = MediaPlayer().apply {
                            reset()
                            setAudioAttributes(
                                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
                            )
                            setDataSource(viewState.songResponse.file)
                            prepare()
                        }
                        binding.lyrics.text = viewState.songResponse.lyrics
                        binding.album.text = viewState.songResponse.album
                        binding.singer.text = viewState.songResponse.singer
                        binding.title.text = viewState.songResponse.title

                        Glide.with(this).load(viewState.songResponse.image).into(binding.image)

                    } catch (e: Exception) {
                        Log.d("결과", e.toString())
                    }
                }

                is MainViewModel.MainViewState.MusicStart -> {
                    mediaPlayer.start()
                }

                is MainViewModel.MainViewState.MusicPause -> {
                    mediaPlayer.pause()
                }

                is MainViewModel.MainViewState.MusicStop -> {
                    mediaPlayer.stop()
                }

                else -> {
                }
            }
        })


    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(mainViewModel)
    }
}