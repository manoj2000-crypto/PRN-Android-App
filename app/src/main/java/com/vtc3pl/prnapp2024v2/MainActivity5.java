package com.vtc3pl.prnapp2024v2;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity5 extends AppCompatActivity {

    private EditText editTextFromDate, editTextToDate;
    private Button checkButton;
    private TableLayout tableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main5);

        // Initialize views
        editTextFromDate = findViewById(R.id.editTextFromDate);
        editTextToDate = findViewById(R.id.editTextToDate);
        checkButton = findViewById(R.id.checkButton);
        tableLayout = findViewById(R.id.tableLayout);

        // Set OnClickListener for the checkButton
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get fromDate and toDate values
                String fromDate = editTextFromDate.getText().toString();
                String toDate = editTextToDate.getText().toString();

                sendPostData(fromDate, toDate);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void sendPostData(final String fromDate, final String toDate) {
        OkHttpClient client = new OkHttpClient();

        // Build request body
        RequestBody requestBody = new FormBody.Builder()
                .add("fromDate", fromDate)
                .add("toDate", toDate)
                .build();

        // Build request
        Request request = new Request.Builder()
                .url("http://your_domain.com/fetch_prnnumber_only_prn_app.php")
                .post(requestBody)
                .build();

        // Make asynchronous call
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                final String responseData = response.body().string();

                // Process JSON response on the main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        processJSONResponse(responseData);
                    }
                });
            }
        });
    }

    private void processJSONResponse(String jsonResponse) {
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);

            // Clear previous table data
            tableLayout.removeAllViews();

            // Create table rows dynamically
            for (int i = 0; i < jsonArray.length(); i++) {
                TableRow tableRow = new TableRow(this);
                TextView textViewSrNo = new TextView(this);
                TextView textViewPRNId = new TextView(this);

                // Set Sr No
                textViewSrNo.setText(String.valueOf(i + 1));

                // Set PRNId
                textViewPRNId.setText(jsonArray.getString(i));

                // Add views to the row
                tableRow.addView(textViewSrNo);
                tableRow.addView(textViewPRNId);

                // Add row to the table
                tableLayout.addView(tableRow);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}