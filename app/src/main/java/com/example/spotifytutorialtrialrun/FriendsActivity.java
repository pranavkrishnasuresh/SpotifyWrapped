package com.example.spotifytutorialtrialrun;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;



public class FriendsActivity extends Activity {
    private Button friendsbackbutton;
    private Button invitefriendsbutton;
    private Button friendrequestsbutton;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends);
        friendsbackbutton = findViewById(R.id.friendsbackbutton);
        invitefriendsbutton = findViewById(R.id.invitefriendsbutton);
        friendrequestsbutton = findViewById(R.id.friendrequestsbutton);
        friendsbackbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendsActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
        invitefriendsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendsActivity.this, InviteFriendsActivity.class);
                startActivity(intent);
            }
        });
        friendrequestsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendsActivity.this, FriendRequestsActivity.class);
                startActivity(intent);
            }
        });
    }
}

