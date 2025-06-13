package com.example.gerstelaudiorecorder;

import android.app.SharedElementCallback;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface AudioRecordDao {
    @Query("SELECT * FROM audioRecords")
    public List<AudioRecord> getAll();

    @Insert
    public void insert(AudioRecord... audioRecord);

    @Delete
    public void delete(AudioRecord audioRecord);

    @Delete
    public void delete(AudioRecord[] audioRecords);

    @Update
    public void update(AudioRecord audioRecord);
}
