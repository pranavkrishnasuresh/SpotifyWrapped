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
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.NotNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Request;


public class WrappedActivity extends Activity {

    private Button wrappedbackbutton;
    private RecyclerView artistRecyclerView;
    private RecyclerView songRecyclerView;
    private TextView dateTextView;
    private String token;
    private MediaPlayer mediaPlayer;
    private List<Artist> topArtists = new ArrayList<>();
    private List<Song> topSongs = new ArrayList<>();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wrapped);

        Log.d("on create", "works");
        initFirebase();
        SettingsActivity.getAccessToken(new SettingsActivity.FirebaseCallback() {
            public void onCallback(String value) {
                token = value;
                Log.d("TokenUpdate", "Token updated: " + token);
            }
        });

        SettingsActivity.getRefreshToken(new SettingsActivity.FirebaseCallback() {
            public void onCallback(String value) {
                refreshToken = value;
            }
        });

        wrappedbackbutton = findViewById(R.id.wrappedbackbutton);
        dateTextView = findViewById(R.id.dateTextView);
        artistRecyclerView = findViewById(R.id.artistRecyclerView);
        songRecyclerView = findViewById(R.id.songRecyclerView);

        topArtists.add(new Artist("No Artists Found", "https://static.thenounproject.com/png/1077596-200.png"));
        topSongs.add(new Song("No Songs Found", "https://static.thenounproject.com/png/1077596-200.png", "android.resource://com.example.spotifytutorialtrialrun/" + R.raw.silence));
        LinearLayoutManager artistLayoutManager = new LinearLayoutManager(this);
        artistRecyclerView.setLayoutManager(artistLayoutManager);

        LinearLayoutManager songLayoutManager = new LinearLayoutManager(this);
        songRecyclerView.setLayoutManager(songLayoutManager);


        artistRecyclerView.setAdapter(new RecyclerView.Adapter() {
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wrappedartistitems, parent, false);
                return new RecyclerView.ViewHolder(view) {
                };
            }

            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                TextView nameTextView = holder.itemView.findViewById(R.id.nameTextView);
                ImageView imageView = holder.itemView.findViewById(R.id.imageView);

                Artist artist = topArtists.get(position);
                nameTextView.setText(artist.getName());

                if (artist.getImageUrl() != null) {
                    Glide.with(holder.itemView.getContext())
                            .load(artist.getImageUrl())
                            .into(imageView);
                } else {
                    Glide.with(holder.itemView.getContext())
                            .load("default_image_url")
                            .into(imageView);
                }
            }
            public int getItemCount() {
                return topArtists.size();
            }
        });

        songRecyclerView.setAdapter(new RecyclerView.Adapter() {
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wrappedsongitems, parent, false);
                return new RecyclerView.ViewHolder(view) {
                };
            }

            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                TextView nameTextView = holder.itemView.findViewById(R.id.nameTextView);
                ImageView imageView = holder.itemView.findViewById(R.id.imageView);

                Song song = topSongs.get(position);
                nameTextView.setText(song.getName());

                if (song.getImageUrl() != null) {
                    Glide.with(holder.itemView.getContext())
                            .load(song.getImageUrl())
                            .into(imageView);
                } else {
                    Glide.with(holder.itemView.getContext())
                            .load("default_image_url")
                            .into(imageView);
                }
            }
            public int getItemCount() {
                return topSongs.size();
            }
        });

        //fetchUserData();
        //keeping this commented bc it seems like its breaking something

        saveWrappedData();

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MMMM d, yyyy", java.util.Locale.getDefault());
        String currentDate = dateFormat.format(calendar.getTime());



        dateTextView.setText(currentDate);

        wrappedbackbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                Intent intent = new Intent(WrappedActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

    }


    private void initFirebase() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).child("tokens").get().addOnCompleteListener(task -> {
            Map<String, String> tokens = (Map<String, String>) task.getResult().getValue();
            token = tokens.get("accessToken");
            String refreshToken = tokens.get("refreshToken");
            fetchUserData();
            if (token == null) {
                    //SettingsActivity.refreshAccessToken(refreshToken);
                    //Commented out bc we need to fix
                }

        });
    }

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final String CLIENT_ID = "c508aeecdc2f4dada9b98f8b7925bde8";
    private static final String CLIENT_SECRET = "ec41420420774792b035e6e37871c3de";
    private String refreshToken;

    private void refreshAccessToken() {
        SettingsActivity.getRefreshToken(new SettingsActivity.FirebaseCallback() {
            public void onCallback(String value) {
                refreshToken = value;
                executorService.execute(new Runnable() {
                    public void run() {
                        OkHttpClient client = new OkHttpClient();
                        RequestBody formBody = new FormBody.Builder()
                                .add("grant_type", "refresh_token")
                                .add("refresh_token", refreshToken)
                                .add("client_id", CLIENT_ID)
                                .add("client_secret", CLIENT_SECRET)
                                .build();
                        Request request = new Request.Builder()
                                .url("https://accounts.spotify.com/api/token")
                                .post(formBody)
                                .build();
                        try (Response response = client.newCall(request).execute()) {
                            String body = response.body().string();
                            JSONObject jsonObject = new JSONObject(body);
                            String newAccessToken = jsonObject.getString("access_token");
                            token = newAccessToken;
                            storeNewAccessTokenInFirebase(newAccessToken);
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void storeNewAccessTokenInFirebase(String newAccessToken) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference myRef = database.getReference("users/" + userId + "/tokens");
            myRef.child("accessToken").setValue(newAccessToken);
        } else {
            Toast.makeText(WrappedActivity.this, "Error: Not logged in.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchUserData() {
        fetchTopArtists();
        fetchTopSongs();
        testToken();
    }

    private boolean fetchedArtists = false;
    private boolean fetchedSongs = false;

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private void fetchTopArtists() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/artists?time_range=short_term&limit=3&offset=0")
                .get()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("SpotifyAPI", "Error fetching top artists", e);
            }


            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("SpotifyAPI", "Request failed: " + response.code() + ", " + response.message() + ", " + response.body().string());
                    return;
                }

                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray items = jsonObject.getJSONArray("items");

                    topArtists.clear();
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        String artistName = item.getString("name");
                        String imageUrl = item.getJSONArray("images").getJSONObject(0).getString("url");
                        topArtists.add(new Artist(artistName, imageUrl));
                    }

                    runOnUiThread(() -> {
                        artistRecyclerView.getAdapter().notifyDataSetChanged();
                        fetchedArtists = true;
                        if (fetchedSongs) {
                            saveWrappedData();
                        }
                    });
                } catch (JSONException e) {
                    Log.e("SpotifyAPI", "Error parsing JSON", e);
                }
            }
        });
    }

    private void fetchTopSongs() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/tracks?time_range=short_term&limit=3&offset=0")
                .addHeader("Authorization", "Bearer " + token)
                .build();
        executor.execute(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray items = jsonObject.getJSONArray("items");

                    topSongs.clear();
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        String songName = item.getString("name");
                        String imageUrl = item.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url");
                        String previewUrl;
                        previewUrl = item.getString("preview_url");
                        topSongs.add(new Song(songName, imageUrl, previewUrl));
                        Log.d("Top Songs", topSongs.toString());
                    }

                    Log.d("Top Songs", topSongs.toString());

                    runOnUiThread(() -> {
                        songRecyclerView.getAdapter().notifyDataSetChanged();
                        fetchedSongs = true;
                        if (fetchedArtists) {
                            saveWrappedData();
                        }
                       try{
                           if (!topSongs.isEmpty()) {
                               playSong(topSongs.get(0).getPreviewUrl());
                               Toast.makeText(WrappedActivity.this, "Playing Preview - User has Premium", Toast.LENGTH_LONG).show();
                           }
                       } catch (Exception e) {
                            Toast.makeText(WrappedActivity.this, "No Preview - User doesn't have Premium", Toast.LENGTH_LONG).show();
                        }


                    });
                } else if (response.code() == 401) {
                    refreshAccessToken();
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void testToken() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        executor.execute(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    Log.d("TokenTest", "Token is valid");
                } else {
                    Log.d("TokenTest", "Token is not valid: " + response.message());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    private void saveWrappedData() {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Map<String, Object> wrappedData = new HashMap<>();
        wrappedData.put("favoriteArtists", topArtists);
        wrappedData.put("favoriteSongs", topSongs);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference wrappedRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(userId)
                    .child("wrappeds")
                    .child(currentDate);
            wrappedRef.setValue(wrappedData).addOnCompleteListener(task -> {
                if (!(task.isSuccessful())) {
                    Toast.makeText(WrappedActivity.this, "Failed to save wrapped data.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(WrappedActivity.this, "Not logged in.", Toast.LENGTH_SHORT).show();
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
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> mediaPlayer.release());
        } catch (IOException e) {
            Log.e("Error", "Error  playing audio", e);
        }
    }
}


