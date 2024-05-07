package com.example.spotifytutorialtrialrun;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends Activity {
    private List<Wrapped> wrappedList = new ArrayList<>();
    private Button historybackbutton;
    private WrappedHistoryAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);
        RecyclerView recyclerView = findViewById(R.id.historyrecyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WrappedHistoryAdapter(this, wrappedList);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            loadWrappedData(user.getUid());
        } else {
            Toast.makeText(HistoryActivity.this, "ERROR! Not logged in.", Toast.LENGTH_SHORT).show();
        }
        recyclerView.setAdapter(adapter);
        historybackbutton = findViewById(R.id.historybackbutton);
        historybackbutton.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, HomeActivity.class);
            startActivity(intent);
        });
    }

    private void loadWrappedData(String userId) {

        DatabaseReference wrappedRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("wrappeds");

        wrappedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Wrapped> wrappedList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Wrapped wrapped = snapshot.getValue(Wrapped.class);
                    if (wrapped != null) {
                        String dateKey = snapshot.getKey();
                        wrapped.setDate(dateKey);
                        wrappedList.add(wrapped);
                    }
                }
                if (!wrappedList.isEmpty()) {
                    adapter.updateData(wrappedList);
                } else {
                    Log.d("Test", "The wrapped list is empty.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Test", "Database error: " + databaseError.getMessage());
            }
        });
    }

}
