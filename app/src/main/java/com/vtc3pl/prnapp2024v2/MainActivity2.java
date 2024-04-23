package com.vtc3pl.prnapp2024v2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);

        Intent intent = getIntent();
        if (intent != null) {
            String username = intent.getStringExtra("username");
            String depo = intent.getStringExtra("depo");
            String year = intent.getStringExtra("year");

            TextView showUserNameTextView = findViewById(R.id.showUserNameTextView);

            // Set the fetched username to the TextView
            if (username != null) {
                showUserNameTextView.setText("User name: " + username);
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}