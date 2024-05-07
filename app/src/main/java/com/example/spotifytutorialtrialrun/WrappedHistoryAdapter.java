package com.example.spotifytutorialtrialrun;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WrappedHistoryAdapter extends RecyclerView.Adapter<WrappedHistoryAdapter.MyViewHolder> {

    private List<Wrapped> myData;
    private Context context;

    public WrappedHistoryAdapter(Context context, List<Wrapped> myData) {
        this.context = context;
        this.myData = myData;
    }

    @NonNull
    @Override
    public WrappedHistoryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wrappedhistoryitem, parent, false);
        return new WrappedHistoryAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull WrappedHistoryAdapter.MyViewHolder holder, int position) {
        Wrapped data = myData.get(position);
        if (data.getDate() != null) {
            holder.itemButton.setText(data.getDate());
        } else {
            holder.itemButton.setText("No date available");
        }
        Log.d("WrappedHistoryAdapter", "Date value: " + data.getDate());
        holder.itemButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, PastWrappedActivity.class);
            intent.putExtra("date", data.getDate());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return myData.size();
    }

    public void updateData(List<Wrapped> newData) {
        myData.clear();
        myData.addAll(newData);
        Log.d("Test", "New data: " + newData.toString());
        notifyDataSetChanged();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        Button itemButton;

        public MyViewHolder(View itemView) {
            super(itemView);
            itemButton = itemView.findViewById(R.id.item_button);
        }
    }
}
