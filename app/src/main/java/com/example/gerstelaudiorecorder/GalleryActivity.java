package com.example.gerstelaudiorecorder;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class GalleryActivity extends AppCompatActivity implements OnItemClickListener {

    private ActivityGalleryBinding binding;
    private ArrayList<AudioRecord> records;
    private RecordingAdapter myAdapter;
    private AppDatabase database;
    private TextInputEditText searchInput;
    private boolean allChecked = false;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private ColorStateList enabledColor, disabledColor;

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

        enabledColor=obtainColor(android.R.attr.textColorPrimary);
        disabledColor=obtainColor(android.R.attr.textColorPrimaryDisableOnly);

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
                leaveEditMode();
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

                if (allChecked){
                    enableDelete();
                } else {
                    disableDelete();
                }
                disableRename();
            }
        });
        binding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long numberSelectedRecords = countSelectedRecords();
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage("Are you sure you want to delete "+numberSelectedRecords+" record(s)?");

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AudioRecord [] toDelete = records.stream()
                                .filter(AudioRecord::isChecked)
                                .toArray(AudioRecord[]::new);

                        ExecutorService executorService = Executors.newSingleThreadExecutor();
                        executorService.submit(()->{
                            database.audioRecordDao().delete(toDelete);
                            runOnUiThread(()->{
                                records.removeAll(Arrays.asList(toDelete));
                                myAdapter.notifyDataSetChanged();
                                leaveEditMode();
                            });
                        });
                        executorService.shutdown();
                    }
                })
                        .setNegativeButton("Cancel",null);
                Dialog dialog = builder.create();
                dialog.show();
            }
        });
        binding.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.rename_layout,null);
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                AudioRecord record = records.stream().filter(AudioRecord::isChecked).findAny().get();
                TextInputEditText textInput = (TextInputEditText) dialogView.findViewById(R.id.filenameInput);
                textInput.setText(record.getFilename());

                dialogView.findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String input = textInput.getText().toString();
                        if (input.isEmpty()){
                            Toast.makeText(view.getContext(),"A name is required",Toast.LENGTH_LONG).show();
                        }else{
                            record.setFilename(input);
                            ExecutorService executorService = Executors.newSingleThreadExecutor();
                            executorService.submit(()->{
                                database.audioRecordDao().update(record);
                                runOnUiThread(()->{
                                    myAdapter.notifyItemChanged(records.indexOf(record));
                                    dialog.dismiss();
                                    leaveEditMode();
                                });
                            });
                            executorService.shutdown();
                        }
                    }
                });
                dialogView.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
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

            long numberSelectedRecords = countSelectedRecords();
            switch ((int) numberSelectedRecords){
                case 0:
                    disableRename();
                    disableDelete();
                    break;
                case 1:
                    enableDelete();
                    enableRename();
                    break;
                default:
                    enableDelete();
                    disableRename();
            }
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

            enableDelete();
            enableRename();
        }
    }

    public void leaveEditMode(){
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        binding.editBar.setVisibility(View.GONE);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        for (AudioRecord record : records) {
            record.setChecked(false);
        }
        myAdapter.setEditMode(false);
    }
    public void disableRename(){
        binding.btnEdit.setClickable(false);
        binding.btnEdit.setBackgroundTintList(disabledColor);
        binding.tvEdit.setTextColor(disabledColor);
    }
    public void disableDelete(){
        binding.btnDelete.setClickable(false);
        binding.btnDelete.setBackgroundTintList(disabledColor);
        binding.tvDelete.setTextColor(disabledColor);
    }

    public void enableRename(){
        binding.btnEdit.setClickable(true);
        binding.btnEdit.setBackgroundTintList(enabledColor);
        binding.tvEdit.setTextColor(enabledColor);
    }
    public void enableDelete(){
        binding.btnDelete.setClickable(true);
        binding.btnDelete.setBackgroundTintList(enabledColor);
        binding.tvDelete.setTextColor(enabledColor);
    }

    private ColorStateList obtainColor(int myColor){
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();

        if (theme.resolveAttribute(myColor,typedValue,true)){
            return ResourcesCompat.getColorStateList(
                    getResources(),
                    typedValue.resourceId,
                    theme
            );
        } else{
            return null;
        }
    }
    private long countSelectedRecords(){
        return records.stream()
                .filter(AudioRecord::isChecked)
                .count();
    }
}