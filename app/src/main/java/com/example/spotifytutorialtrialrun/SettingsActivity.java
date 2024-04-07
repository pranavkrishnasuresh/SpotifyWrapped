package com.example.spotifytutorialtrialrun;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Request;

public class SettingsActivity extends Activity {
    private Button signoutbutton;
    private Button deleteaccountbutton;
    private Button settingsbackbutton;
    private Button linkbutton;
    private TextView usernameTextView;
    private TextView emailTextView;

    private static final String CLIENT_ID = "c508aeecdc2f4dada9b98f8b7925bde8";
    private static final String REDIRECT_URI = "com.example.spotifytutorialtrialrun://auth";
    private static final int AUTH_TOKEN_REQUEST_CODE = 0x10; // Arbitrary request code


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings); // Make sure you use the correct layout file name here

        // Initialize TextViews
        usernameTextView = findViewById(R.id.spotify_username);
        emailTextView = findViewById(R.id.user_email);

        // Set initial text
        usernameTextView.setText("N/A : Not connected.");
        emailTextView.setText("N/A :Not connected.");

        // Load the Spotify username and email from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("SPOTIFY", 0);
        String spotifyUsername = sharedPreferences.getString("username", "N/A : Not connected.");
        String email = sharedPreferences.getString("email", "N/A : Not connected.");

        // Set the text
        usernameTextView.setText(spotifyUsername);
        emailTextView.setText(email);

        // Initialize buttons
        signoutbutton = findViewById(R.id.signoutbutton);
        deleteaccountbutton = findViewById(R.id.deleteaccountbutton);
        settingsbackbutton = findViewById(R.id.settingsbackbutton);
        linkbutton = findViewById(R.id.linkbutton);

        // Set up click listeners for each button

        settingsbackbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        linkbutton.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN);
                AuthorizationClient.openLoginActivity(SettingsActivity.this, AUTH_TOKEN_REQUEST_CODE, request);
           
            }

        }));

        signoutbutton = findViewById(R.id.signoutbutton);
        signoutbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out from Firebase
                FirebaseAuth.getInstance().signOut();

                // Invalidate the Spotify access token
                invalidateSpotifyAccessToken();

                usernameTextView.setText("N/A : Not connected.");
                emailTextView.setText("N/A : Not connected.");

                // Redirect to the login activity
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    private AuthorizationRequest getAuthenticationRequest(AuthorizationResponse.Type type) {
        return new AuthorizationRequest.Builder(CLIENT_ID, type, REDIRECT_URI)
                .setScopes(new String[]{"user-read-private", "user-read-email"}) // Example scopes
                .build();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTH_TOKEN_REQUEST_CODE) {
            final AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);
            if (response.getType() == AuthorizationResponse.Type.TOKEN) {
                // Use the token to perform API requests or store it in your database
                String accessToken = response.getAccessToken();
                //String refreshToken = getRefreshToken(response.getCode()); // Assuming you have this method
                storeTokenInFirebase(accessToken);
                Toast.makeText(SettingsActivity.this, "Connected!", Toast.LENGTH_SHORT).show();
                // Set Spotify username and email
                // You need to implement getSpotifyUsername() and getEmail() methods
                getSpotifyUsername(response.getAccessToken());
                getEmail();
            }
        }
    }


    private void storeTokenInFirebase(String accessToken) {
        // Get a reference to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Get the current Firebase user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            DatabaseReference myRef = database.getReference("users/" + userId + "/tokens");

            // Create a map to store the tokens
            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", accessToken);

            // Save the user to Firebase
            myRef.setValue(tokens);
        } else {
            Toast.makeText(SettingsActivity.this, "ERROR! Not logged in.",
                    Toast.LENGTH_SHORT).show();
        }
    }
    private OkHttpClient mOkHttpClient = new OkHttpClient();

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private void getSpotifyUsername(final String accessToken) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url("https://api.spotify.com/v1/me")
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .build();

                try (Response response = mOkHttpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        Log.e("SettingsActivity", "Unexpected response code: " + response);
                        throw new IOException("Unexpected code " + response);
                    }

                    // Get the response body
                    String responseBody = response.body().string();

                    // Log the response body
                    Log.d("SettingsActivity", "Response body: " + responseBody);

                    // Parse the response body as JSON
                    JSONObject jsonObject = new JSONObject(responseBody);

                    // Get the Spotify username from the JSON
                    final String spotifyUsername = jsonObject.getString("display_name");
                    // Store the Spotify username in SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("SPOTIFY", 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", spotifyUsername);
                    editor.apply();
                    // Update the UI on the main thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (spotifyUsername != null) {
                                usernameTextView.setText(spotifyUsername);
                            } else {
                                usernameTextView.setText("Error getting Spotify username");
                            }
                            String email = sharedPreferences.getString("email", "N/A : Not connected.");
                            emailTextView.setText(email);
                        }
                    });
                } catch (IOException | JSONException e) {
                    Log.e("SettingsActivity", "Error getting Spotify username", e);
                    e.printStackTrace();
                }
            }
        });
    }

    private void getEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            final String email = user.getEmail();

            // Store the email in SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("SPOTIFY", 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("email", email);
            editor.apply();

            // Update the emailTextView on the main thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (email != null) {
                        emailTextView.setText(email);
                    } else {
                        emailTextView.setText("Error getting email");
                    }
                }
            });
        }
    }
    private void invalidateSpotifyAccessToken() {
        // Get a reference to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Get the current Firebase user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            DatabaseReference myRef = database.getReference("users/" + userId + "/tokens");

            // Create a map to store the tokens
            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", null);

            // Save the user to Firebase
            myRef.setValue(tokens);
        } else {
            Toast.makeText(SettingsActivity.this, "ERROR! Not logged in.",
                    Toast.LENGTH_SHORT).show();
        }
    }
    private void getCurrentUserId(String accessToken) {
        //
    }

    private void storeUserIdAndTokenInFirebase(String userId, String token) {

        //
    }
}
