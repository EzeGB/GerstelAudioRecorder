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
    List<AudioRecord> getAll();

    @Query("SELECT * FROM audioRecords WHERE filename LIKE '%'||:query||'%'")
    List<AudioRecord> searchDatabase(String query);

    @Insert
    void insert(AudioRecord... audioRecord);

    @Delete
    void delete(AudioRecord audioRecord);

    @Delete
    void delete(AudioRecord[] audioRecords);

    @Update
    void update(AudioRecord audioRecord);
}
