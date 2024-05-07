package com.example.spotifytutorialtrialrun;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText newPassword;
    private Button changeButton, backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changepassword);

        newPassword = findViewById(R.id.newpassword);
        changeButton = findViewById(R.id.changebutton);
        backButton = findViewById(R.id.changepasswordbackbutton);

        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPass = newPassword.getText().toString();
                if (!newPass.isEmpty()) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    user.updatePassword(newPass).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ChangePasswordActivity.this, "Password updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ChangePasswordActivity.this, "Password update failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(ChangePasswordActivity.this, "Please enter new password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChangePasswordActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }
}