package com.vtc3pl.prnapp2024v2;
// PRN MISSMATCH REPORT
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

public class MainActivity11 extends AppCompatActivity {

    private EditText editTextFromDate, editTextToDate;
    private Button showMissmatchReportButton;
    private TableLayout tableLayout;
    private LottieAnimationView lottieAnimationView;
    private Calendar fromCalendar, toCalendar;
    private DatePickerDialog.OnDateSetListener fromDateSetListener, toDateSetListener;
    private String username = "", depo = "", year = "";
    private char firstLetter = 'A'; //this is for CP

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main11);

        // Initialize views
        editTextFromDate = findViewById(R.id.editTextFromDate);
        editTextToDate = findViewById(R.id.editTextToDate);
        showMissmatchReportButton = findViewById(R.id.showMissmatchReportButton);
        tableLayout = findViewById(R.id.tableLayout);
        lottieAnimationView = findViewById(R.id.lottieAnimationView);

        fromCalendar = Calendar.getInstance();
        toCalendar = Calendar.getInstance();

        //Setting the current date as soon as activity loads.
        updateFromDate();
        updateToDate();

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
            depo = intent.getStringExtra("depo");
            year = intent.getStringExtra("year");

            firstLetter = username.charAt(0);
            Log.d("First Letter After : ", String.valueOf(firstLetter));
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
        showMissmatchReportButton.setOnClickListener(new View.OnClickListener() {
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

        String url = (firstLetter == 'C' || firstLetter == 'c') ? "https://vtc3pl.com/cp_prn_missmatch_report_for_prn_app.php" : "https://vtc3pl.com/prn_missmatch_report_for_prn_app.php";

        // Build request
        Request request = new Request.Builder().url(url).post(requestBody).build();

        // Make asynchronous call
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                // Hide the Lottie animation
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showAlert("Connection Failed", "Failed to connect to server.");
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
                    Log.e("Response PRN LIST : ", responseData);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            // Hide the Lottie animation
                            lottieAnimationView.setVisibility(View.GONE);
                            lottieAnimationView.cancelAnimation();

                            if ("0 Result".equals(responseData)) {
                                showAlert("No Records Found", "No records found for " + username);
//                                tableLayout.removeAllViews();
                            } else {
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

            TextView prnNoHeader = createHeaderTextView("PRN Number");
            headerRow.addView(prnNoHeader);

            TextView lrNoHeader = createHeaderTextView("LR Number");
            headerRow.addView(lrNoHeader);

            TextView ConsignorHeader = createHeaderTextView("Customer Name");
            headerRow.addView(ConsignorHeader);

            TextView totalQuantityHeader = createHeaderTextView("Total Quantity");
            headerRow.addView(totalQuantityHeader);

            TextView receivedQuantityHeader = createHeaderTextView("Received Quantity");
            headerRow.addView(receivedQuantityHeader);

            TextView differentQuantityHeader = createHeaderTextView("Different Quantity");
            headerRow.addView(differentQuantityHeader);

            TextView reasonHeader = createHeaderTextView("Reason");
            headerRow.addView(reasonHeader);

            tableLayout.addView(headerRow);

            // Create table rows dynamically
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);

                TableRow tableRow = new TableRow(this);
                TextView textViewSrNo = createTextView(String.valueOf(i + 1) + ") ");
                TextView textViewPRNId = createTextView(jsonObject.getString("TokenId"));
                TextView textViewLRNO = createTextView(jsonObject.getString("LRNO"));
                TextView textViewConsignor = createTextView(jsonObject.getString("Consignor"));
                TextView textViewTotalPkgsNo = createTextView(jsonObject.getString("TotalPkgsNo"));
                TextView textViewRecievedQty = createTextView(jsonObject.getString("RecievedQty"));
                TextView textViewshortstock = createTextView(jsonObject.getString("shortstock"));
                TextView textViewReason = createTextView(jsonObject.getString("Reason"));

                // Add views to the row
                tableRow.addView(textViewSrNo);
                tableRow.addView(textViewPRNId);
                tableRow.addView(textViewLRNO);
                tableRow.addView(textViewConsignor);
                tableRow.addView(textViewTotalPkgsNo);
                tableRow.addView(textViewRecievedQty);
                tableRow.addView(textViewshortstock);
                tableRow.addView(textViewReason);

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