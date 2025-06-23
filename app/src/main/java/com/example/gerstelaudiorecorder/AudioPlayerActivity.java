package com.example.gerstelaudiorecorder;

import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.SeekBar;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gerstelaudiorecorder.databinding.ActivityAudioPlayerBinding;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Objects;

public class AudioPlayerActivity extends AppCompatActivity {
    ActivityAudioPlayerBinding binding;
    private MediaPlayer mediaPlayer;
    Handler seekBarHandler;
    Runnable seekBarRunnable;
    Long seekBarDelay = 500L;
    int jumpValue = 5000;
    float playbackSpeed = 1.0f;

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

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                customOnBackPressed();
            }
        };
        getOnBackPressedDispatcher().addCallback(this,callback);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        binding.tvFilename.setText(fileName);

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        binding.tvTrackDuration.setText(dateFormat(mediaPlayer.getDuration()));

        seekBarHandler = new Handler(Looper.getMainLooper());
        seekBarRunnable = new Runnable() {
            @Override
            public void run() {
                binding.seekBar.setProgress(mediaPlayer.getCurrentPosition());
                binding.tvTrackProgress.setText(dateFormat(mediaPlayer.getCurrentPosition()));
                seekBarHandler.postDelayed(seekBarRunnable,seekBarDelay);
            }
        };

        binding.btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPausePlayer();
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                binding.btnPlay.setBackground(getDrawable(R.drawable.ic_play_circle));
                seekBarHandler.removeCallbacks(seekBarRunnable);
            }
        });
        binding.btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+jumpValue);
                binding.seekBar.setProgress(binding.seekBar.getProgress()+jumpValue);
            }
        });
        binding.btnBackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-jumpValue);
                binding.seekBar.setProgress(binding.seekBar.getProgress()-jumpValue);
            }
        });
        binding.chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playbackSpeed!=2.0f){
                    playbackSpeed+=0.5f;
                    jumpValue+=2500;
                }else{
                    playbackSpeed=0.5f;
                    jumpValue=2500;
                }
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(playbackSpeed));
                binding.chip.setText(String.format("x %s", playbackSpeed));
            }
        });
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b){
                    mediaPlayer.seekTo(i);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        playPausePlayer();
        binding.seekBar.setMax(mediaPlayer.getDuration());
    }

    private void playPausePlayer(){
        if (!mediaPlayer.isPlaying()){
            mediaPlayer.start();
            binding.btnPlay.setBackground(getDrawable(R.drawable.ic_pause_circle));
            seekBarHandler.postDelayed(seekBarRunnable,seekBarDelay);
        }else{
            mediaPlayer.pause();
            binding.btnPlay.setBackground(getDrawable(R.drawable.ic_play_circle));
            seekBarHandler.removeCallbacks(seekBarRunnable);
        }
    }
    private void customOnBackPressed(){
        mediaPlayer.stop();
        mediaPlayer.release();
        seekBarHandler.removeCallbacks(seekBarRunnable);
        finish();
    }
    private String dateFormat(int duration){
        int d = duration /1000;
        int s = d%60;
        int m = d/60%60;
        int h = ((d-m*60)/360);

        NumberFormat f = new DecimalFormat("00");
        String str = m+":"+f.format(s);

        if (h>0){
            str = h+":"+str;
        }
        return str;
    }
}