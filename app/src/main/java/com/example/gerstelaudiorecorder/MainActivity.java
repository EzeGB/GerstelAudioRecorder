package com.example.gerstelaudiorecorder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gerstelaudiorecorder.databinding.ActivityMainBinding;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private static final int REQUEST_CODE = 200;
    private String [] permissions =  new String[]{Manifest.permission.RECORD_AUDIO};
    private boolean permissionGranted, isRecording, isPaused = false;

    private MediaRecorder recorder;
    private String dirPath, filename;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        permissionGranted = ActivityCompat.checkSelfPermission(this,permissions[0]) == PackageManager.PERMISSION_GRANTED;
        if (!permissionGranted){
            ActivityCompat.requestPermissions(this, permissions,REQUEST_CODE);
        }

        binding.btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPaused){
                    resumeRecording();
                } else if (isRecording) {
                    pauseRecording();
                } else {
                    startRecording();
                }
            }
        });

        binding.btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recorder.stop();
                isRecording= false;
                isPaused = false;
                binding.btnRecord.setImageResource(R.drawable.ic_record);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE){
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void startRecording (){
        if (!permissionGranted){
            ActivityCompat.requestPermissions(this, permissions,REQUEST_CODE);
        } else {
            //start recording

            recorder = new MediaRecorder();
            dirPath = Objects.requireNonNull(this.getExternalCacheDir()).getAbsolutePath();

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.DD_hh.mm.ss");
            String date = simpleDateFormat.format(new Date());
            filename = "audio_record_"+date;

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioSamplingRate(44100);
            recorder.setAudioEncodingBitRate(320000);
            recorder.setOutputFile(dirPath+filename+".mp3");

            try {
                recorder.prepare();
            } catch (IOException e){}

            recorder.start();
            isRecording = true;
            isPaused = false;
            binding.btnRecord.setImageResource(R.drawable.ic_pause);
        }
    }

    private void pauseRecording() {
        recorder.pause();
        isRecording= false;
        isPaused = true;
        binding.btnRecord.setImageResource(R.drawable.ic_record);
    }

    private void resumeRecording() {
        recorder.resume();
        isRecording= true;
        isPaused = false;
        binding.btnRecord.setImageResource(R.drawable.ic_pause);
    }
}