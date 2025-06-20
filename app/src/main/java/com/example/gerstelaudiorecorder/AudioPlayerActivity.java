package com.example.gerstelaudiorecorder;

import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gerstelaudiorecorder.databinding.ActivityAudioPlayerBinding;

import java.io.IOException;

public class AudioPlayerActivity extends AppCompatActivity {
    ActivityAudioPlayerBinding binding;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding=ActivityAudioPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String filePath = intent.getStringExtra("filePath");
        String fileName = intent.getStringExtra("fileName");

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        playPausePlayer();

        binding.btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPausePlayer();
            }
        });
    }

    private void playPausePlayer(){
        if (!mediaPlayer.isPlaying()){
            mediaPlayer.start();
            binding.btnPlay.setBackground(getDrawable(R.drawable.ic_pause_circle));
        }else{
            mediaPlayer.pause();
            binding.btnPlay.setBackground(getDrawable(R.drawable.ic_play_circle));
        }
    }
}