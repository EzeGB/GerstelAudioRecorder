package com.example.gerstelaudiorecorder;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.*;

import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import com.example.gerstelaudiorecorder.databinding.ActivityMainBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlinx.coroutines.GlobalScope;

public class MainActivity extends AppCompatActivity implements Timer.OnTimerTickListener {
    ActivityMainBinding binding;
    private static final int REQUEST_CODE = 200;
    private String [] permissions =  new String[]{Manifest.permission.RECORD_AUDIO};
    private boolean permissionGranted, isRecording, isPaused = false;
    private MediaRecorder recorder;
    private String dirPath, filename;
    private String duration;
    private File currentFile;
    private ArrayList<Float> registeredAmplitudes;
    private Timer timer;
    private Vibrator vibrator;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private AppDatabase database;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        permissionGranted = ActivityCompat.checkSelfPermission(this,permissions[0]) == PackageManager.PERMISSION_GRANTED;
        if (!permissionGranted){
            ActivityCompat.requestPermissions(this, permissions,REQUEST_CODE);
        }

        database = Room.databaseBuilder(
                this,
                AppDatabase.class,
                "audioRecords"
        ).build();

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.bottomSheet);
        bottomSheetBehavior.setPeekHeight(0);
        bottomSheetBehavior.setState(STATE_COLLAPSED);
        binding.bottomSheet.bottomSheet.setVisibility(GONE);

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
                binding.bottomSheet.bottomSheet.setVisibility(VISIBLE);

                binding.bottomSheetBG.setVisibility(VISIBLE);
                binding.bottomSheet.filenameInput.setText(filename);
                bottomSheetBehavior.setState(STATE_EXPANDED);
            }
        });
        binding.btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, GalleryActivity.class));
            }
        });
        binding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean deleted;
                stopRecording();
                deleted = deleteRecording();
                if (deleted){
                    Toast.makeText(MainActivity.this,"Record deleted", LENGTH_SHORT).show();
                }
            }
        });

        binding.btnDelete.setEnabled(false);
        binding.btnDelete.setImageResource(R.drawable.ic_delete_disabled);

        binding.bottomSheet.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteRecording();
                dismissBottomSheet();
            }
        });
        binding.bottomSheet.btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissBottomSheet();
                saveRecording();
                Toast.makeText(MainActivity.this,"Record saved", LENGTH_SHORT).show();
            }
        });
        binding.bottomSheetBG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.bottomSheet.btnCancel.callOnClick();
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
            dirPath = Objects.requireNonNull(this.getExternalCacheDir()).getAbsolutePath();
            currentFile = new File(dirPath+createFilename()+".mp3");

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioSamplingRate(44100);
            recorder.setAudioEncodingBitRate(320000);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                recorder.setOutputFile(currentFile);
            }

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
    private void saveRecording(){
        String newFileName = binding.bottomSheet.filenameInput.getText().toString();
        if (!newFileName.equals(filename)){
            File newFile = new File(dirPath+newFileName+".mp3");
            currentFile.renameTo(newFile);
        }

        String filePath = dirPath+newFileName+".mp3";
        Long timestamp = (new Date()).getTime();
        String ampsPath = dirPath+newFileName;

        try {
            FileOutputStream fos = new FileOutputStream(ampsPath);
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(registeredAmplitudes);
            fos.close();
            out.close();
        } catch (IOException e){}

        AudioRecord record = new AudioRecord(newFileName,filePath,duration,ampsPath,timestamp);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(()->{
            database.audioRecordDao().insert(record);
        });
        executorService.shutdown();
    }
    private boolean deleteRecording(){
        return currentFile.delete();
    }

    private String createFilename(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.DD_hh.mm.ss");
        String date = simpleDateFormat.format(new Date());
        filename = "audio_record_"+date;
        return filename;
    }
    private void dismissBottomSheet(){
        binding.bottomSheet.bottomSheet.setVisibility(GONE);
        binding.bottomSheetBG.setVisibility(GONE);
        hideKeyboard(binding.bottomSheet.filenameInput);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(()->{
            bottomSheetBehavior.setState(STATE_COLLAPSED);
        },100);
    }

    @Override
    public void onTimerTick(String formatedDuration,long duration) {
        binding.tvTimer.setText(formatedDuration);
        if (duration%100==0){
            binding.waveforView.addAmplitude((float) recorder.getMaxAmplitude());
        }
        this.duration = formatedDuration.substring(0,formatedDuration.length()-3);
    }
    public void hideKeyboard(View view){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),0);
    }
}