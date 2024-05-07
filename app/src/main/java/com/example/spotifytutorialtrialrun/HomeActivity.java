package com.example.spotifytutorialtrialrun;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    // Define buttons as member variables
    private Button friendsButton;
    private Button historyButton;
    private Button gameButton;
    private Button settingsButton;
    private Button wrappedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homescreen);

        friendsButton = findViewById(R.id.friendsbutton);
        historyButton = findViewById(R.id.historybutton);
        gameButton = findViewById(R.id.gamebutton);
        settingsButton = findViewById(R.id.settingsbutton);
        wrappedButton = findViewById(R.id.wrappedbutton);

        friendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, FriendsActivity.class);
                startActivity(intent);
            }
        });

        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });
        /*
        gameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // we can uncomment this when the game is ready
            }
        });

         */
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        wrappedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, WrappedActivity.class);
                startActivity(intent);
            }
        });
    }
}