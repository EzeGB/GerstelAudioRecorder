package com.example.gerstelaudiorecorder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecordingAdapter extends RecyclerView.Adapter<RecordingAdapter.ViewHolder> {

    ArrayList<AudioRecord> records;
    public RecordingAdapter(ArrayList<AudioRecord> records){
        this.records=records;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AudioRecord record = records.get(position);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date(record.getTimeStamp());
        String strDate = sdf.format(date);

        holder.tvFilename.setText(record.getFilename());
        holder.tvMeta.setText(String.format("%s%s", record.getDuration(), strDate));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        View itemView;
        TextView tvFilename, tvMeta;
        CheckBox checkBox;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView=itemView;
            this.tvFilename=this.itemView.findViewById(R.id.tvFilename);
            this.tvMeta=this.itemView.findViewById(R.id.tvMeta);
            this.checkBox=this.itemView.findViewById(R.id.checkbox);
        }
    }
}
