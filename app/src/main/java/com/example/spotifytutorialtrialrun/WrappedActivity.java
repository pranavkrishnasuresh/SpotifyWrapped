package com.example.spotifytutorialtrialrun;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.Request;


public class WrappedActivity extends Activity {

    private Button wrappedbackbutton;
    private RecyclerView artistRecyclerView;
    private RecyclerView songRecyclerView;
    private TextView dateTextView;
    private String token;
    private List<String> topArtists = new ArrayList<>();
    private List<String> topSongs = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wrapped);

        // Initialize Firebase and Spotify clients
        initFirebase();

        wrappedbackbutton = findViewById(R.id.wrappedbackbutton);
        dateTextView = findViewById(R.id.dateTextView);
        artistRecyclerView = findViewById(R.id.artistRecyclerView);
        songRecyclerView = findViewById(R.id.songRecyclerView);

        artistRecyclerView.setAdapter(new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wrappedartistitems, parent, false);
                return new RecyclerView.ViewHolder(view) {};
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                TextView nameTextView = holder.itemView.findViewById(R.id.nameTextView);
                nameTextView.setText(topArtists.get(position));
            }

            @Override
            public int getItemCount() {
                return topArtists.size();
            }
        });

        songRecyclerView.setAdapter(new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wrappedsongitems, parent, false);
                return new RecyclerView.ViewHolder(view) {};
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                TextView nameTextView = holder.itemView.findViewById(R.id.nameTextView);
                nameTextView.setText(topSongs.get(position));
            }

            @Override
            public int getItemCount() {
                return topSongs.size();
            }
        });

        // Fetch and display user's Spotify data
        fetchUserData();

        // Get the current date
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        String currentDate = dateFormat.format(calendar.getTime());

        // Set the current date in dateTextView
        dateTextView.setText(currentDate);

        wrappedbackbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WrappedActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initFirebase() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        // Fetch the user's token from the database
        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).child("token").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                token = String.valueOf(task.getResult().getValue());
                // Fetch and display user's Spotify data
                fetchUserData();
            }
        });
    }

    private void fetchUserData() {
        fetchTopArtists();
        fetchTopSongs();
    }



    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private void fetchTopArtists() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/artists") // replace with your URL
                .addHeader("Authorization", "Bearer " + token) // replace with your token
                .build();
        executor.execute(() -> {
            try (Response response = client.newCall(request).execute()) {
                JSONObject jsonObject = new JSONObject(response.body().string());
                JSONArray items = jsonObject.getJSONArray("items");

                topArtists.clear();
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    topArtists.add(item.getString("name"));
                }

                runOnUiThread(() -> {
                    artistRecyclerView.getAdapter().notifyDataSetChanged();
                });
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void fetchTopSongs() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/tracks")
                .addHeader("Authorization", "Bearer " + token) // replace with your token
                .build();
        executor.execute(() -> {
            try (Response response = client.newCall(request).execute()) {
                JSONObject jsonObject = new JSONObject(response.body().string());
                JSONArray items = jsonObject.getJSONArray("items");

                topSongs.clear();
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    topSongs.add(item.getString("name"));
                }

                runOnUiThread(() -> {
                    songRecyclerView.getAdapter().notifyDataSetChanged();
                });
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        });
    }
}