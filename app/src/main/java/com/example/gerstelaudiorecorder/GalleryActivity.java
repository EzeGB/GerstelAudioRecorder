package com.example.gerstelaudiorecorder;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Query;
import androidx.room.Room;

import com.example.gerstelaudiorecorder.databinding.ActivityGalleryBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
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
    private boolean allChecked = false;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;

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

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetGallery);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

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

        binding.btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                binding.editBar.setVisibility(View.GONE);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

                for (AudioRecord record : records) {
                    record.setChecked(false);
                }
                myAdapter.setEditMode(false);
            }
        });
        binding.btnSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allChecked = !allChecked;
                for (AudioRecord record : records){
                    record.setChecked(allChecked);
                }
                myAdapter.notifyDataSetChanged();
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

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        if (myAdapter.isEditMode() && binding.editBar.getVisibility() == View.GONE){
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
            binding.editBar.setVisibility(View.VISIBLE);
        }
    }
}