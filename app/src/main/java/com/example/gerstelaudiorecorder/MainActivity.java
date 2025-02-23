package com.example.gerstelaudiorecorder;

import android.Manifest;
import android.content.pm.PackageManager;
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

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private static final int REQUEST_CODE = 200;
    private String [] permissions =  new String[]{Manifest.permission.RECORD_AUDIO};
    private boolean permissionGranted = false;
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
                startRecording();
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

        }
    }
}