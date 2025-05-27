package com.example.gerstelaudiorecorder;

import android.os.Handler;
import android.os.Looper;

import androidx.core.os.HandlerCompat;

import java.lang.reflect.Method;

public class Timer{

    interface OnTimerTickListener {
        public void onTimerTick(String formatedDuration, long duration);
    }
    private Handler handler = HandlerCompat.createAsync(Looper.getMainLooper());
    private Runnable runnable;
    private OnTimerTickListener listener;
    private long duration = 0L;
    private long delay = 10L;

    {
        runnable = ()-> {
            duration+=delay;
            handler.postDelayed(runnable,delay);
            listener.onTimerTick(format(),duration);
    };
    }

    public Timer (OnTimerTickListener listener){
        this.listener = listener;
    }

    public void start(){
        handler.postDelayed(runnable,delay);
    }
    public void pause(){
        handler.removeCallbacks(runnable);
    }
    public void stop(){
        handler.removeCallbacks(runnable);
        duration = 0L;
    }
    public String format (){
        long milSecs=extractTime(0L,1,1);
        long secs=extractTime(milSecs,60,100);
        long mins=extractTime(milSecs+secs,60*60,100*60);
        long hours=extractTime(milSecs+secs+mins,24*60*60,60*60*100);
        String formattedTime;
        if (hours>0){
            formattedTime=String.format("%02d:%02d:%02d.%02d",hours,mins,secs,milSecs);
        }
        else {
            formattedTime=String.format("%02d:%02d.%02d",mins,secs,milSecs);
        }
        return formattedTime;
    }
    private long extractTime(long residual,int modMultiplier, int conversionMultiplier){
        long result = ((duration-residual)%(modMultiplier*1000))/(conversionMultiplier*10);
        return result;
    }
}
