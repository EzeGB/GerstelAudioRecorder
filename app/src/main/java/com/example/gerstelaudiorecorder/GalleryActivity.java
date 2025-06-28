package com.example.gerstelaudiorecorder;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Query;
import androidx.room.Room;

import com.example.gerstelaudiorecorder.databinding.ActivityGalleryBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GalleryActivity extends AppCompatActivity implements OnItemClickListener {

    private ActivityGalleryBinding binding;
    private ArrayList<AudioRecord> records;
    private RecordingAdapter myAdapter;
    private AppDatabase database;
    private TextInputEditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
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

        searchInput = binding.searchInput;
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String query = charSequence.toString();
                searchDatabase(query);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void searchDatabase(String query) {
        records.clear();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(()->{
            List<AudioRecord> queryResult = database.audioRecordDao().searchDatabase(query);
            records.addAll(queryResult);
        });
        executorService.shutdown();

        runOnUiThread(() -> myAdapter.notifyDataSetChanged());
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

        if (myAdapter.isEditMode()){
            AudioRecord selectedRecord = records.get(position);
            selectedRecord.setChecked(!selectedRecord.isChecked());
            myAdapter.notifyItemChanged(position);
        } else {
            Intent intent = new Intent(this, AudioPlayerActivity.class);
            intent.putExtra("filePath",audioRecord.getFilePath());
            intent.putExtra("fileName",audioRecord.getFilename());
            startActivity(intent);
        }
    }

    @Override
    public void OnLongItemClickListener(int position) {
        myAdapter.setEditMode(true);
        AudioRecord selectedRecord = records.get(position);
        selectedRecord.setChecked(!selectedRecord.isChecked());
        myAdapter.notifyItemChanged(position);
    }
}