package com.vtc3pl.prnapp2024v2;
// PRN LIST PAGE

import android.app.DatePickerDialog;
import android.content.Intent;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

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
    private LottieAnimationView lottieAnimationView;
    private Calendar fromCalendar, toCalendar;
    private DatePickerDialog.OnDateSetListener fromDateSetListener, toDateSetListener;
    private String username = "", depo = "", year = "";

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
        lottieAnimationView = findViewById(R.id.lottieAnimationView);

        fromCalendar = Calendar.getInstance();
        toCalendar = Calendar.getInstance();

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
            depo = intent.getStringExtra("depo");
            year = intent.getStringExtra("year");
        }

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

                // Show the Lottie animation
                lottieAnimationView.setVisibility(View.VISIBLE);
                lottieAnimationView.playAnimation();

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
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        // Build request body
        RequestBody requestBody = new FormBody.Builder().add("fromDate", fromDate).add("toDate", toDate).add("username", username).build();

        // Build request
        Request request = new Request.Builder().url("https://vtc3pl.com/fetch_prnnumber_only_prn_app.php").post(requestBody).build();

        // Make asynchronous call
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                // Hide the Lottie animation
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lottieAnimationView.setVisibility(View.GONE);
                        lottieAnimationView.cancelAnimation();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                ResponseBody responseBody = response.body();
                Log.e("Response Body PRN LIST:", String.valueOf(responseBody));
                if (responseBody != null) {
                    final String responseData = response.body().string();
                    Log.e("Response PRN LIST : " , responseData);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            // Hide the Lottie animation
                            lottieAnimationView.setVisibility(View.GONE);
                            lottieAnimationView.cancelAnimation();

                            if ("0 Result".equals(responseData)) {
                                showAlert("No Records Found", "No records found for " + username);
//                                tableLayout.removeAllViews();
                            }else {
                                processJSONResponse(responseData);
                            }
                        }
                    });
                } else {
                    Log.e("Response Error", "Response body is null");
                    // Hide the Lottie animation
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lottieAnimationView.setVisibility(View.GONE);
                            lottieAnimationView.cancelAnimation();
                        }
                    });
                }
            }
        });
    }

    private void processJSONResponse(String jsonResponse) {
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);

            // Clear previous table data
            tableLayout.removeAllViews();

            // Create table headers
            TableRow headerRow = new TableRow(this);
            headerRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            TextView srNoHeader = createHeaderTextView("SrNo");
            headerRow.addView(srNoHeader);

            TextView prnIdHeader = createHeaderTextView("PRN No");
            headerRow.addView(prnIdHeader);

            TextView prnDateHeader = createHeaderTextView("Date");
            headerRow.addView(prnDateHeader);

            TextView vehicleNoHeader = createHeaderTextView("Vehicle No");
            headerRow.addView(vehicleNoHeader);

            TextView quantityHeader = createHeaderTextView("Quantity");
            headerRow.addView(quantityHeader);

            tableLayout.addView(headerRow);

            // Create table rows dynamically
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);

                TableRow tableRow = new TableRow(this);
                TextView textViewSrNo = createTextView(String.valueOf(i + 1) + ") ");
                TextView textViewPRNId = createTextView(jsonObject.getString("PRNId"));
                TextView textViewPRNDate = createTextView(jsonObject.getString("PRNDate"));
                TextView textViewVehicleNo = createTextView(jsonObject.getString("VehicleNo"));
                TextView textViewQuantity = createTextView(jsonObject.getString("Quantity"));

                // Add views to the row
                tableRow.addView(textViewSrNo);
                tableRow.addView(textViewPRNId);
                tableRow.addView(textViewPRNDate);
                tableRow.addView(textViewVehicleNo);
                tableRow.addView(textViewQuantity);

                // Add row to the table
                tableLayout.addView(tableRow);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON Exception: ", "MainActivity5(tableLayout) ", e);
        }
    }

    private TextView createHeaderTextView(String headerText) {
        TextView headerTextView = new TextView(this);
        headerTextView.setText(headerText);
        headerTextView.setPadding(10, 10, 10, 10);
        return headerTextView;
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(10, 10, 10, 10); // Same padding as headerTextView
        return textView;
    }

    private void showAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        builder.show();
    }
}