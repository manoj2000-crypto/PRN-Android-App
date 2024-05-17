package com.vtc3pl.prnapp2024v2;
// LR Number pending for PRN

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity8 extends AppCompatActivity {

    private TextView textViewFromDateActivityEight, textViewToDateActivityEight;
    private EditText editTextFromDateActivityEight, editTextToDateActivityEight;
    private Calendar fromCalendar, toCalendar;
    private DatePickerDialog.OnDateSetListener fromDateSetListener, toDateSetListener;
    private Button searchButtonActivityEight;
    private String username = "", depo = "", year = "";
    private TableLayout tableLayoutActivityEight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main8);

        textViewFromDateActivityEight = findViewById(R.id.textViewFromDateActivityEight);
        editTextFromDateActivityEight = findViewById(R.id.editTextFromDateActivityEight);

        textViewToDateActivityEight = findViewById(R.id.textViewToDateActivityEight);
        editTextToDateActivityEight = findViewById(R.id.editTextToDateActivityEight);

        searchButtonActivityEight = findViewById(R.id.searchButtonActivityEight);

        tableLayoutActivityEight = findViewById(R.id.tableLayoutActivityEight);

        fromCalendar = Calendar.getInstance();
        toCalendar = Calendar.getInstance();

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
            depo = intent.getStringExtra("depo");
            year = intent.getStringExtra("year");
        }

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

        searchButtonActivityEight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OkHttpClient client = new OkHttpClient();

                String url = "https://vtc3pl.com/fetch_pending_lrno_for_prn.php";
                String fromDate = editTextFromDateActivityEight.getText().toString().trim();
                String toDate = editTextToDateActivityEight.getText().toString().trim();

                Log.e("usernme", username);
                Log.e("depo", depo);
                Log.e("year", year);

                FormBody.Builder formBuilder = new FormBody.Builder();
//                formBuilder.add("username", username);
                formBuilder.add("depo", depo);
//                formBuilder.add("year", year);
                formBuilder.add("fromDate", fromDate);
                formBuilder.add("toDate", toDate);

                Request request = new Request.Builder().url(url).post(formBuilder.build()).build();

                // Make the request asynchronously
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e("onFailure", String.valueOf(e));
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            // Parse the response and display it in a table format
                            String responseData = response.body().string();
                            try {
                                JSONArray jsonArray = new JSONArray(responseData);
                                Log.e("response on success", String.valueOf(jsonArray));
                                // Process JSON array and display data in table format
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Process JSON array and display data in table format
                                        displayDataInTable(jsonArray);
                                    }
                                });
                            } catch (JSONException e) {
                                Log.e("onResponse Expetion : ", String.valueOf(e));
                                Log.d("Response : ", responseData);
                                e.printStackTrace();
                            }
                        } else {
                            // Handle unsuccessful response
                            Log.e("Error", "Unsuccessful response: " + response);
                        }
                    }
                });
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
        editTextFromDateActivityEight.setText(android.text.format.DateFormat.format(dateFormat, fromCalendar));
    }

    private void updateToDate() {
        String dateFormat = "yyyy/MM/dd";
        editTextToDateActivityEight.setText(android.text.format.DateFormat.format(dateFormat, toCalendar));
    }

    private void displayDataInTable(JSONArray jsonArray) {
        // Clear existing table rows
        tableLayoutActivityEight.removeAllViews();

        // Create table headers
        TableRow headerRow = new TableRow(MainActivity8.this);
        headerRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView srNoHeader = createHeaderTextView("SrNo");
        headerRow.addView(srNoHeader);

        TextView lrNoHeader = createHeaderTextView("LR No");
        headerRow.addView(lrNoHeader);

        TextView lrDateHeader = createHeaderTextView("LR Date");
        headerRow.addView(lrDateHeader);

        TextView payBasidHeader = createHeaderTextView("Pay Basis");
        headerRow.addView(payBasidHeader);

        TextView customerNameHeader = createHeaderTextView("Customer Name");
        headerRow.addView(customerNameHeader);

        TextView quantityHeader = createHeaderTextView("Quantity");
        headerRow.addView(quantityHeader);

        tableLayoutActivityEight.addView(headerRow);

        // Iterate through JSON array and add rows to the table
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                final String lRNO = jsonObject.getString("LRNO");
                String lRDate = jsonObject.getString("LRDate");
                String payBasis = jsonObject.getString("PayBasis");
                String consignor = jsonObject.getString("Consignor");
                String quantity = jsonObject.getString("PkgsNo");

                TableRow row = new TableRow(MainActivity8.this);
                row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                TextView srNo = createTextView(String.valueOf(i + 1));
                row.addView(srNo);

                TextView lR_NO = createTextView(lRNO);
                row.addView(lR_NO);

                TextView lR_Date = createTextView(lRDate);
                row.addView(lR_Date);

                TextView pay_Basis = createTextView(payBasis);
                row.addView(pay_Basis);

                TextView consignor_name = createTextView(consignor);
                row.addView(consignor_name);

                TextView quantityNo = createTextView(quantity);
                row.addView(quantityNo);

                tableLayoutActivityEight.addView(row);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("tableCreation Excep: ", String.valueOf(e));
            }
        }
    }

    private TextView createHeaderTextView(String text) {
        TextView textView = new TextView(MainActivity8.this);
        textView.setText(text);
        textView.setPadding(10, 10, 10, 10); // Padding
        return textView;
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(MainActivity8.this);
        textView.setText(text);
        textView.setPadding(10, 10, 10, 10); // Padding
        return textView;
    }
}