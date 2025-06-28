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

    private ArrayList<AudioRecord> records;
    private OnItemClickListener listener;
    private boolean editMode = false;
    public RecordingAdapter(ArrayList<AudioRecord> records, OnItemClickListener listener){
        this.records=records;
        this.listener=listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_layout,parent,false);
        return new ViewHolder(view,listener);
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
        holder.tvMeta.setText(String.format("%s %s", record.getDuration(), strDate));

        if (editMode){
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(record.isChecked());
        } else {
            holder.checkBox.setVisibility(View.GONE);
            holder.checkBox.setChecked(false);
        }
    }

    public boolean isEditMode() {
        return editMode;
    }
    public void setEditMode(boolean editMode) {
        if (this.editMode != editMode){
            this.editMode = editMode;
            notifyDataSetChanged();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements  View.OnLongClickListener, View.OnClickListener {
        TextView tvFilename, tvMeta;
        CheckBox checkBox;
        OnItemClickListener listener;
        public ViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            this.listener=listener;

            this.tvFilename=this.itemView.findViewById(R.id.tvFilename);
            this.tvMeta=this.itemView.findViewById(R.id.tvMeta);
            this.checkBox=this.itemView.findViewById(R.id.checkbox);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position!=RecyclerView.NO_POSITION){
                listener.OnItemClickListener(position);
            }
        }
        @Override
        public boolean onLongClick(View view) {
            int position = getAdapterPosition();
            if (position!=RecyclerView.NO_POSITION){
                listener.OnLongItemClickListener(position);
            }
            return true;
        }
    }
}
