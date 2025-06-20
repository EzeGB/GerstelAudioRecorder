package com.example.gerstelaudiorecorder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GalleryActivity extends AppCompatActivity implements OnItemClickListener {

    private ArrayList<AudioRecord> records;
    private RecordingAdapter myAdapter;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gallery);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        records = new ArrayList<>();
        database = Room.databaseBuilder(this,AppDatabase.class,"audioRecords").build();
        myAdapter = new RecordingAdapter(records,this);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchAll();
    }

    private void fetchAll(){
        records.clear();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(()->{
            List<AudioRecord> queryResult = database.audioRecordDao().getAll();
            records.addAll(queryResult);
        });
        executorService.shutdown();
        myAdapter.notifyDataSetChanged();
    }

    @Override
    public void OnItemClickListener(int position) {
        AudioRecord audioRecord = records.get(position);
        Intent intent = new Intent(this, AudioPlayerActivity.class);

        intent.putExtra("filePath",audioRecord.getFilePath());
        intent.putExtra("fileName",audioRecord.getFilename());
        startActivity(intent);
    }

    @Override
    public void OnLongItemClickListener(int position) {
        Toast.makeText(this,"Long Click",Toast.LENGTH_SHORT).show();
    }
}