package com.vtc3pl.prnapp2024v2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class MainActivity2 extends AppCompatActivity {

    private static final Pattern LR_NUMBER_PATTERN = Pattern.compile("[A-Z]{3,4}[0-9]{10}+");
    private TableLayout tableLayout;
    private EditText lrEditText;
    private Set<String> lrNumbersSet = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);

        TextView showUserNameTextView = findViewById(R.id.showUserNameTextView);
        tableLayout = findViewById(R.id.tableDisplay);
        lrEditText = findViewById(R.id.lrEditText);

        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> addRowToTable());

        Intent intent = getIntent();
        if (intent != null) {
            String username = intent.getStringExtra("username");
            String depo = intent.getStringExtra("depo");
            String year = intent.getStringExtra("year");

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

    private void addRowToTable() {
        String lrNumber = lrEditText.getText().toString().trim();
        if (LR_NUMBER_PATTERN.matcher(lrNumber).matches() && !lrNumber.isEmpty() && !lrNumbersSet.contains(lrNumber)) {
            TableRow newRow = new TableRow(this);
            TextView textView = new TextView(this);
            textView.setText(lrNumber);
            newRow.addView(textView);

            Button deleteButton = new Button(this);
            deleteButton.setText("Delete");
            deleteButton.setOnClickListener(v -> {
                // Remove the row when delete button is clicked
                tableLayout.removeView(newRow);
                // Remove LR number from the set
                lrNumbersSet.remove(lrNumber);
            });
            newRow.addView(deleteButton);

            tableLayout.addView(newRow);

            // Add LR number to the set
            lrNumbersSet.add(lrNumber);
            // Clear the lrEditText after adding the row
            lrEditText.setText("");
        } else {
            // Show Toast message if LR number format is invalid or duplicate
            Toast.makeText(this, "LR number format is invalid or duplicate", Toast.LENGTH_SHORT).show();
        }
    }
}