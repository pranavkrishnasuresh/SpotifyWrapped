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
    private Button changepasswordbutton;
    private TextView usernameTextView;
    private TextView emailTextView;


    private static final String CLIENT_ID = "c508aeecdc2f4dada9b98f8b7925bde8";
    private static final String CLIENT_SECRET = "ec41420420774792b035e6e37871c3de";
    private static final String REDIRECT_URI = "com.example.spotifytutorialtrialrun://auth";
    private static final int AUTH_TOKEN_REQUEST_CODE = 0x10;
    private String token;
    private String refreshToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        usernameTextView = findViewById(R.id.spotify_username);
        emailTextView = findViewById(R.id.user_email);

        usernameTextView.setText("N/A : Not connected.");
        emailTextView.setText("N/A :Not connected.");

        getAccessToken(new FirebaseCallback() {
            public void onCallback(String accessToken) {
                if (accessToken != null) {
                    getSpotifyUsername(accessToken);
                } else {
                    Log.e("SettingsActivity", "Error getting access token from Firebase");
                }
            }
        });


        SharedPreferences sharedPreferences = getSharedPreferences("SPOTIFY", 0);
        String spotifyUsername = sharedPreferences.getString("username", "N/A : Not connected.");
        String email = sharedPreferences.getString("email", "N/A : Not connected.");
        emailTextView.setText(email);

        FirebaseUser user1 = FirebaseAuth.getInstance().getCurrentUser();
        if (user1 != null) {
            String email1 = user1.getEmail();
            emailTextView.setText(email1);
        } else {
            emailTextView.setText("N/A : Not connected.");
        }



        usernameTextView.setText(spotifyUsername);

        signoutbutton = findViewById(R.id.signoutbutton);
        deleteaccountbutton = findViewById(R.id.deleteaccountbutton);
        settingsbackbutton = findViewById(R.id.settingsbackbutton);
        linkbutton = findViewById(R.id.linkbutton);
        changepasswordbutton = findViewById(R.id.changepasswordbutton);

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

        signoutbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                invalidateSpotifyAccessToken();

                usernameTextView.setText("N/A : Not connected.");
                emailTextView.setText("N/A : Not connected.");

                SharedPreferences sharedPreferences = getSharedPreferences("SPOTIFY", 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        deleteaccountbutton.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
                userRef.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //deletes account
                        user.delete().addOnCompleteListener(deleteTask -> {
                            if (deleteTask.isSuccessful()) {
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                Toast.makeText(SettingsActivity.this, "Failed to delete account.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(SettingsActivity.this, "Failed to delete account.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        changepasswordbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
            }
        });

    }
    private AuthorizationRequest getAuthenticationRequest(AuthorizationResponse.Type type) {
        return new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.CODE, REDIRECT_URI)
                .setScopes(new String[]{"user-read-private", "user-read-email", "user-top-read"})
                .build();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTH_TOKEN_REQUEST_CODE) {
            final AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);
            if (response.getType() == AuthorizationResponse.Type.CODE) {
                String authCode = response.getCode();
                exchangeCodeForToken(authCode);
                Toast.makeText(SettingsActivity.this, "Connected!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void exchangeCodeForToken(String authCode) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                RequestBody formBody = new FormBody.Builder()
                        .add("grant_type", "authorization_code")
                        .add("code", authCode)
                        .add("redirect_uri", REDIRECT_URI)
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
                    String accessToken = jsonObject.getString("access_token");
                    String refreshToken = jsonObject.getString("refresh_token");
                    storeTokenInFirebase(accessToken, refreshToken);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void storeTokenInFirebase(String accessToken, String refreshToken) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        Log.d("storeTokenInFirebase", "Access Token: " + accessToken);
        Log.d("storeTokenInFirebase", "Refresh Token: " + refreshToken);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            DatabaseReference myRef = database.getReference("users/" + userId + "/tokens");
            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", accessToken);
            tokens.put("refreshToken", refreshToken);
            myRef.setValue(tokens)
                    .addOnSuccessListener(aVoid -> Log.d("FirebaseWrite", "Tokens successfully written to Firebase."))
                    .addOnFailureListener(e -> Log.e("FirebaseWrite", "Failed to write tokens to Firebase.", e));

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

                    String responseBody = response.body().string();
                    Log.d("SettingsActivity", "Response body: " + responseBody);

                    JSONObject jsonObject = new JSONObject(responseBody);
                    final String spotifyUsername = jsonObject.getString("display_name");

                    SharedPreferences sharedPreferences = getSharedPreferences("SPOTIFY", 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", spotifyUsername);
                    editor.apply();
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

    //not used right now since we need to fix some things first. this should refresh the token though
    private void refreshAccessToken(String refreshToken) {
        executorService.execute(new Runnable() {
            @Override
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
                    storeNewAccessTokenInFirebase(newAccessToken);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
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
            Toast.makeText(SettingsActivity.this, "ERROR! Not logged in.",
                    Toast.LENGTH_SHORT).show();
        }
    }
    private void invalidateSpotifyAccessToken() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference myRef = database.getReference("users/" + userId + "/tokens");
            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", null);
            myRef.child("accessToken").setValue(null);
        } else {
            Toast.makeText(SettingsActivity.this, "ERROR! Not logged in.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public interface FirebaseCallback {
        void onCallback(String value);
    }
    public static void getRefreshToken(FirebaseCallback firebaseCallback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("users/" + userId + "/tokens/refreshToken");

            myRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String refreshToken = String.valueOf(task.getResult().getValue());
                    firebaseCallback.onCallback(refreshToken);
                } else {
                    Log.e("Firebase", "Error getting refresh token", task.getException());
                }
            });
        } else {
            Log.e("SettingsActivity", "ERROR! Not logged in.");
        }
    }

    public static void getAccessToken(FirebaseCallback firebaseCallback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("users/" + userId + "/tokens/accessToken");

            myRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String accessToken = String.valueOf(task.getResult().getValue());
                    firebaseCallback.onCallback(accessToken);
                } else {
                    Log.e("Firebase", "Error getting access token", task.getException());
                }
            });
        } else {
            Log.e("SettingsActivity", "ERROR! Not logged in.");
        }
    }
}
