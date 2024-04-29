package com.vtc3pl.prnapp2024v2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity4 extends AppCompatActivity {

    private String username = "", depo = "", year = "";
    private TextView showUserNameActivityFourTextView;

    private Button createPrnButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main4);

        showUserNameActivityFourTextView = findViewById(R.id.showUserNameActivityFourTextView);
        createPrnButton = findViewById(R.id.createPrnButton);

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
            depo = intent.getStringExtra("depo");
            year = intent.getStringExtra("year");

            // Set the fetched username to the TextView
            if (username != null) {
                String usernameText = getString(R.string.user_name_prefix, username);
                showUserNameActivityFourTextView.setText(usernameText);
            }
        }

        // Set onClickListener for createPrnButton
        createPrnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start MainActivity2 and pass values as extras
                Intent intent = new Intent(MainActivity4.this, MainActivity2.class);
                intent.putExtra("username", username);
                intent.putExtra("depo", depo);
                intent.putExtra("year", year);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}