package com.example.gerstelaudiorecorder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WaveformView extends View {

    private Paint paint = new Paint();
    private ArrayList<Float> amplitudes = new ArrayList<>();
    private ArrayList<RectF> spikes = new ArrayList<>();
    private float radius, width, distance, screenWidth, screenHeight;
    private int maxSpikes;

    {
        paint.setColor(Color.rgb(244,81,30));
        radius = 8f;
        width = 20f;
        distance = 10f;
        screenWidth=(float) getResources().getDisplayMetrics().widthPixels;
        screenHeight=800f;
        maxSpikes=(int)(screenWidth/(width+distance));
    }

    public WaveformView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void addAmplitude(Float amplitude){
        float normalizedAmplitude = (Math.min(amplitude/20,screenHeight));
        amplitudes.add(normalizedAmplitude);

        spikes.clear();
        //this takes only the last amplitudes registered on the list
        List<Float> rotatingAmplitudes = amplitudes.subList(Math.max(amplitudes.size()-maxSpikes,0),amplitudes.size());
        for (int i=0; i<rotatingAmplitudes.size(); i++){
            float left = screenWidth-i*(width+distance);
            float top = screenHeight/2 - rotatingAmplitudes.get(i)/2;
            float right = left+width;
            float bottom = top + rotatingAmplitudes.get(i);
            spikes.add(new RectF(left,top,right,bottom));
        }

        invalidate();
    }

    public ArrayList<Float> resetAmplitudes(){
        ArrayList<Float> registeredAmplitudes = amplitudes;
        amplitudes.clear();
        spikes.clear();
        invalidate();
        return registeredAmplitudes;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
        for (RectF spike:spikes) {
            canvas.drawRoundRect(spike,radius,radius,paint);
        }
    }
}
