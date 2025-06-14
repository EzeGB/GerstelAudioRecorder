package com.example.gerstelaudiorecorder;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "audioRecords")
public class AudioRecord {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @Ignore
    private boolean isChecked = false;
    private String filename,filePath,duration,ampsPath;
    private Long timeStamp;

    //boilerplate
    public AudioRecord(String filename, String filePath, String duration, String ampsPath, Long timeStamp) {
        this.filename = filename;
        this.filePath = filePath;
        this.duration = duration;
        this.ampsPath = ampsPath;
        this.timeStamp = timeStamp;
    }
    public String getFilename() {
        return filename;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }
    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public String getDuration() {
        return duration;
    }
    public void setDuration(String duration) {
        this.duration = duration;
    }
    public String getAmpsPath() {
        return ampsPath;
    }
    public void setAmpsPath(String ampsPath) {
        this.ampsPath = ampsPath;
    }
    public Long getTimeStamp() {
        return timeStamp;
    }
    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
}
