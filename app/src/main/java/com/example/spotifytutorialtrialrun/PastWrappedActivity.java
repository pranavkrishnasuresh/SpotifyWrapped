package com.example.spotifytutorialtrialrun;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

public class PastWrappedActivity extends Activity {
    private TextView dateTextView;
    private RecyclerView artistRecyclerView;
    private RecyclerView songRecyclerView;
    private MediaPlayer mediaPlayer;
    private Button pastwrappedbackbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pastwrapped);

        dateTextView = findViewById(R.id.pastDateTextView);
        artistRecyclerView = findViewById(R.id.pastArtistRecyclerView);
        songRecyclerView = findViewById(R.id.pastSongRecyclerView);
        pastwrappedbackbutton = findViewById(R.id.pastwrappedbackbutton);

        String date = getIntent().getStringExtra("date");
        loadWrappedData(date);

        pastwrappedbackbutton.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
            }
            Intent intent = new Intent(PastWrappedActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
    }

    private void loadWrappedData(String date) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            DatabaseReference wrappedRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(userId)
                    .child("wrappeds")
                    .child(date);

            wrappedRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Wrapped wrapped = dataSnapshot.getValue(Wrapped.class);
                    if (wrapped != null) {
                        dateTextView.setText(wrapped.getDate());
                        artistRecyclerView.setLayoutManager(new LinearLayoutManager(PastWrappedActivity.this));
                        artistRecyclerView.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                            @NonNull
                            @Override
                            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pastwrappedartistitems, parent, false);
                                return new RecyclerView.ViewHolder(view) {};
                            }

                            @Override
                            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                                TextView artistNameTextView = holder.itemView.findViewById(R.id.pastartistnameTextView);
                                ImageView artistImageView = holder.itemView.findViewById(R.id.pastartistimageView);
                                Artist artist = wrapped.getFavoriteArtists().get(position);

                                artistNameTextView.setText(artist.getName());
                                if (artist.getImageUrl() != null && !artist.getImageUrl().isEmpty()) {
                                    Glide.with(PastWrappedActivity.this).load(artist.getImageUrl()).into(artistImageView);
                                }}

                            @Override
                            public int getItemCount() {
                                return wrapped.getFavoriteArtists().size();
                            }
                        });
                        songRecyclerView.setLayoutManager(new LinearLayoutManager(PastWrappedActivity.this));
                        songRecyclerView.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                            @NonNull
                            @Override
                            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pastwrappedsongitems, parent, false);
                                return new RecyclerView.ViewHolder(view) {};
                            }

                            @Override
                            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                                Song song = wrapped.getFavoriteSongs().get(position);
                                TextView songNameTextView = holder.itemView.findViewById(R.id.pastsongnameTextView);
                                ImageView songImageView = holder.itemView.findViewById(R.id.pastsongimageView);

                                songNameTextView.setText(song.getName());
                                if (song.getImageUrl() != null && !song.getImageUrl().isEmpty()) {
                                    Glide.with(holder.itemView.getContext()).load(song.getImageUrl()).into(songImageView);
                                }
                            }

                            @Override
                            public int getItemCount() {
                                return wrapped.getFavoriteSongs().size();
                            }
                        });
                        try {
                            if (!wrapped.getFavoriteSongs().isEmpty()) {
                                playSong(wrapped.getFavoriteSongs().get(0).getPreviewUrl());
                                Toast.makeText(PastWrappedActivity.this, "Playing Preview - User has Premium", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(PastWrappedActivity.this, "No Preview - User doesn't have Premium", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(PastWrappedActivity.this, "Failed to load wrapped data.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(PastWrappedActivity.this, "Not logged in.", Toast.LENGTH_SHORT).show();
        }

    }
    private void playSong(String url) {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(url);
            Log.d("MediaPlayer", "Data source set: " + url);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> mediaPlayer.release());
        } catch (IOException e) {
            Log.e("MediaPlayer1", "Error setting data source or playing audio", e);
        }
    }
}