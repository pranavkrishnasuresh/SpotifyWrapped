package com.example.spotifytutorialtrialrun;

        import android.app.Activity;
        import android.content.Intent;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ListView;

public class FriendRequestsActivity extends Activity {
    private ListView friendRequestsList;
    private Button friendrequestsbackbutton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friendrequests);
        friendrequestsbackbutton = findViewById(R.id.friendrequestsbackbutton);
        friendrequestsbackbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendRequestsActivity.this, FriendsActivity.class);
                startActivity(intent);
            }
        });
    }
}