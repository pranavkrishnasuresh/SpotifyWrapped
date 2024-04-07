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
        setContentView(R.layout.homescreen); // Make sure you use the correct layout file name here

        // Initialize buttons
        friendsButton = findViewById(R.id.friendsbutton);
        historyButton = findViewById(R.id.historybutton);
        gameButton = findViewById(R.id.gamebutton);
        settingsButton = findViewById(R.id.settingsbutton);
        wrappedButton = findViewById(R.id.wrappedbutton);

        // Set up click listeners for each button
        // Commented out for now, uncomment and fill in with appropriate logic when needed
        /*
        friendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Handle friends button click
            }
        });

        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Handle history button click
            }
        });

        gameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Handle game button click
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