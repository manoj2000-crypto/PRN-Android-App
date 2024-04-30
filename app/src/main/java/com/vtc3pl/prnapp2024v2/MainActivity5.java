package com.vtc3pl.prnapp2024v2;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity5 extends AppCompatActivity {

    private EditText editTextFromDate, editTextToDate;
    private Button checkButton;
    private TableLayout tableLayout;
    private Calendar fromCalendar, toCalendar;
    private DatePickerDialog.OnDateSetListener fromDateSetListener, toDateSetListener;

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

        fromCalendar = Calendar.getInstance();
        toCalendar = Calendar.getInstance();

        // Initialize date set listeners
        fromDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                fromCalendar.set(Calendar.YEAR, year);
                fromCalendar.set(Calendar.MONTH, month);
                fromCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateFromDate();
            }
        };

        toDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                toCalendar.set(Calendar.YEAR, year);
                toCalendar.set(Calendar.MONTH, month);
                toCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateToDate();
            }
        };

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

    public void showDatePickerDialogFromDate(View v) {
        new DatePickerDialog(this, fromDateSetListener, fromCalendar.get(Calendar.YEAR), fromCalendar.get(Calendar.MONTH), fromCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void showDatePickerDialogToDate(View v) {
        new DatePickerDialog(this, toDateSetListener, toCalendar.get(Calendar.YEAR), toCalendar.get(Calendar.MONTH), toCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateFromDate() {
        String dateFormat = "yyyy/MM/dd";
        editTextFromDate.setText(android.text.format.DateFormat.format(dateFormat, fromCalendar));
    }

    private void updateToDate() {
        String dateFormat = "yyyy/MM/dd";
        editTextToDate.setText(android.text.format.DateFormat.format(dateFormat, toCalendar));
    }

    private void sendPostData(final String fromDate, final String toDate) {
        OkHttpClient client = new OkHttpClient();

        // Build request body
        RequestBody requestBody = new FormBody.Builder().add("fromDate", fromDate).add("toDate", toDate).build();

        // Build request
        Request request = new Request.Builder().url("https://vtc3pl.com/fetch_prnnumber_only_prn_app.php").post(requestBody).build();

        // Make asynchronous call
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                ResponseBody responseBody = response.body();
                if (responseBody != null) {

                    final String responseData = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            processJSONResponse(responseData);
                        }
                    });
                } else {
                    Log.e("Response Error", "Response body is null");
                }
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
                String srNo = String.valueOf(i + 1) + ") ";
                textViewSrNo.setText(srNo);

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
            Log.e("JSON Exception: ", "MainActivity5(tableLayout) ", e);
        }
    }
}