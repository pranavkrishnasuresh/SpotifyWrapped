package com.example.spotifytutorialtrialrun;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class InviteFriendsActivity extends Activity {
    private Button invitefriendsbackbutton;
    private EditText emailEditText;
    private Button sendRequestButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invitefriends);
        invitefriendsbackbutton = findViewById(R.id.invitefriendsbackbutton);
        emailEditText = findViewById(R.id.email_edit_text);
        sendRequestButton = findViewById(R.id.send_request_button);

        invitefriendsbackbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InviteFriendsActivity.this, FriendsActivity.class);
                startActivity(intent);
            }
        });

        sendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
            }
        });
    }
}