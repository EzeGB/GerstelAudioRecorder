package com.example.gerstelaudiorecorder;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.*;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gerstelaudiorecorder.databinding.ActivityMainBinding;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements Timer.OnTimerTickListener {
    ActivityMainBinding binding;
    private static final int REQUEST_CODE = 200;
    private String [] permissions =  new String[]{Manifest.permission.RECORD_AUDIO};
    private boolean permissionGranted, isRecording, isPaused = false;

    private MediaRecorder recorder;
    private String dirPath, filename;
    private ArrayList<Float> registeredAmplitudes;
    private Timer timer;
    private Vibrator vibrator;
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

        timer = new Timer(this);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50,VibrationEffect.DEFAULT_AMPLITUDE));
                }
            }
        });
        binding.btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRecording();
                Toast.makeText(MainActivity.this,"Record saved", LENGTH_SHORT).show();
            }
        });
        binding.btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,"List Button", LENGTH_SHORT).show();
            }
        });
        binding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteRecording();
                Toast.makeText(MainActivity.this,"Record deleted", LENGTH_SHORT).show();
            }
        });

        binding.btnDelete.setEnabled(false);
        binding.btnDelete.setImageResource(R.drawable.ic_delete_disabled);
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

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioSamplingRate(44100);
            recorder.setAudioEncodingBitRate(320000);
            recorder.setOutputFile(dirPath+createFilename()+".mp3");

            try {
                recorder.prepare();
            } catch (IOException e){}

            recorder.start();
            timer.start();
            isRecording = true;
            isPaused = false;

            binding.btnRecord.setImageResource(R.drawable.ic_pause);
            binding.btnList.setVisibility(GONE);
            binding.btnDone.setVisibility(VISIBLE);
            binding.btnDone.setEnabled(true);
            binding.btnDelete.setEnabled(true);
            binding.btnDelete.setImageResource(R.drawable.ic_delete);
        }
    }
    private void pauseRecording() {
        recorder.pause();
        timer.pause();
        isRecording= false;
        isPaused = true;
        binding.btnRecord.setImageResource(R.drawable.ic_record);
    }
    private void resumeRecording() {
        recorder.resume();
        timer.start();
        isRecording= true;
        isPaused = false;
        binding.btnRecord.setImageResource(R.drawable.ic_pause);
    }
    private void stopRecording(){
        recorder.stop();
        recorder.release();
        timer.stop();
        isRecording= false;
        isPaused = false;

        binding.tvTimer.setText("00:00.00");
        registeredAmplitudes = binding.waveforView.resetAmplitudes();

        binding.btnRecord.setImageResource(R.drawable.ic_record);
        binding.btnList.setVisibility(VISIBLE);
        binding.btnDone.setVisibility(GONE);
        binding.btnDone.setEnabled(false);
        binding.btnDelete.setEnabled(false);
        binding.btnDelete.setImageResource(R.drawable.ic_delete_disabled);
    }
    private void deleteRecording(){
        stopRecording();
        File fileDeleted = new File(dirPath+createFilename()+".mp3");
        fileDeleted.deleteOnExit();
    }

    private String createFilename(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.DD_hh.mm.ss");
        String date = simpleDateFormat.format(new Date());
        filename = "audio_record_"+date;
        return filename;
    }
    @Override
    public void onTimerTick(String formatedDuration,long duration) {
        binding.tvTimer.setText(formatedDuration);
        if (duration%100==0){
            binding.waveforView.addAmplitude((float) recorder.getMaxAmplitude());
        }
    }
}