package com.vtc3pl.prnapp2024v2;
// LR Number pending for PRN

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity8 extends AppCompatActivity {

    private TextView textViewFromDateActivityEight, textViewToDateActivityEight, contractPartyTextViewActivityEight;
    private EditText editTextFromDateActivityEight, editTextToDateActivityEight, contractPartyEditTextActivityEight, contractPartyFinalEditTextActivityEight;
    private Spinner contractPartySpinnerActivityEight;
    private Calendar fromCalendar, toCalendar;
    private DatePickerDialog.OnDateSetListener fromDateSetListener, toDateSetListener;
    private Button searchButtonActivityEight;
    private String username = "", depo = "", year = "";
    private TableLayout tableLayoutActivityEight;
    private LottieAnimationView lottieAnimationView;
    private List<String> contractPartiesList;
    private ArrayAdapter<String> spinnerAdapter;
    private Handler handlerSecond = new Handler();
    private Runnable updateSpinnerRunnable;
    private boolean isSpinnerSelected = false;
    private String[] parts = {""};
    private String contractParty = "";
    private String contractPartyCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main8);

        textViewFromDateActivityEight = findViewById(R.id.textViewFromDateActivityEight);
        editTextFromDateActivityEight = findViewById(R.id.editTextFromDateActivityEight);

        textViewToDateActivityEight = findViewById(R.id.textViewToDateActivityEight);
        editTextToDateActivityEight = findViewById(R.id.editTextToDateActivityEight);

        contractPartyTextViewActivityEight = findViewById(R.id.contractPartyTextViewActivityEight);
        contractPartyEditTextActivityEight = findViewById(R.id.contractPartyEditTextActivityEight);
        contractPartyFinalEditTextActivityEight = findViewById(R.id.contractPartyFinalEditTextActivityEight);
        contractPartyFinalEditTextActivityEight.setEnabled(false);

        contractPartySpinnerActivityEight = findViewById(R.id.contractPartySpinnerActivityEight);

        searchButtonActivityEight = findViewById(R.id.searchButtonActivityEight);

        tableLayoutActivityEight = findViewById(R.id.tableLayoutActivityEight);
        lottieAnimationView = findViewById(R.id.lottieAnimationView);

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

        contractPartiesList = new ArrayList<>();
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, contractPartiesList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        contractPartySpinnerActivityEight.setAdapter(spinnerAdapter);

        contractPartyEditTextActivityEight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (updateSpinnerRunnable != null) {
                    handlerSecond.removeCallbacks(updateSpinnerRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isSpinnerSelected) { // Only fetch if not a spinner selection
                    String searchText = s.toString().trim();
                    if (!searchText.isEmpty()) {
                        updateSpinnerRunnable = new Runnable() {
                            @Override
                            public void run() {
                                // Call the method to update the contract party list
                                fetchContractParties(searchText);
                            }
                        };
                        // Post the update with a slight delay to debounce
                        handlerSecond.postDelayed(updateSpinnerRunnable, 300);
                    }
                }
                isSpinnerSelected = false; // Reset the flag
            }
        });

        contractPartySpinnerActivityEight.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e("Spinner", "onItemSelected called");

                String selectedContractParty = (String) parent.getItemAtPosition(position);

                Log.e("Selected ContractParty:", selectedContractParty);

                isSpinnerSelected = true; // Set the flag before updating the EditText
                contractPartyFinalEditTextActivityEight.setText(selectedContractParty);
                //contractPartiesList.clear();
                //spinnerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Not needed
            }
        });

        searchButtonActivityEight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Show the Lottie animation
                lottieAnimationView.setVisibility(View.VISIBLE);
                lottieAnimationView.playAnimation();

                OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                String url = "https://vtc3pl.com/fetch_pending_lrno_for_prn.php";
                String fromDate = editTextFromDateActivityEight.getText().toString().trim();
                String toDate = editTextToDateActivityEight.getText().toString().trim();

                String contractPartyText = contractPartyFinalEditTextActivityEight.getText().toString().trim();
                Log.e("WholeContractPartyText", contractPartyText);
                parts = contractPartyText.split(":");
                contractParty = parts.length >= 2 ? parts[1].trim() : "";
                Log.e("OnlyContractParty", contractParty);
                contractPartyCode = parts.length >= 2 ? parts[0].trim() : "";
                Log.e("OnlyContractPartyCode", contractPartyCode);

                Log.e("usernme", username);
                Log.e("depo", depo);
                Log.e("year", year);

                FormBody.Builder formBuilder = new FormBody.Builder();
//                formBuilder.add("username", username);
                formBuilder.add("depo", depo);
//                formBuilder.add("year", year);
                formBuilder.add("fromDate", fromDate);
                formBuilder.add("toDate", toDate);
                formBuilder.add("contractParty", contractParty);

                Request request = new Request.Builder().url(url).post(formBuilder.build()).build();

                // Make the request asynchronously
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e("onFailure", String.valueOf(e));
                        e.printStackTrace();
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
                        if (response.isSuccessful()) {
                            // Parse the response and display it in a table format
                            ResponseBody body = response.body();
                            if (body != null) {
                                String responseData = body.string();
                                try {
                                    JSONArray jsonArray = new JSONArray(responseData);
                                    Log.e("response on success", String.valueOf(jsonArray));
                                    // Process JSON array and display data in table format
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Hide the Lottie animation
                                            lottieAnimationView.setVisibility(View.GONE);
                                            lottieAnimationView.cancelAnimation();
                                            // Process JSON array and display data in table format
                                            displayDataInTable(jsonArray);
                                        }
                                    });
                                } catch (JSONException e) {
                                    Log.e("onResponse Expetion : ", String.valueOf(e));
                                    Log.d("Response : ", responseData);
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            lottieAnimationView.setVisibility(View.GONE);
                                            lottieAnimationView.cancelAnimation();
                                        }
                                    });
                                }
                            } else {
                                Log.e("Response Error", "Response body is null");
                                runOnUiThread(() -> {
                                    Toast.makeText(MainActivity8.this, "Empty response body", Toast.LENGTH_SHORT).show();
                                    lottieAnimationView.setVisibility(View.GONE);
                                    lottieAnimationView.cancelAnimation();
                                });
                            }
                        } else {
                            // Handle unsuccessful response
                            Log.e("Error", "Unsuccessful response: " + response);
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

    private void fetchContractParties(String input) {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        RequestBody formBody = new FormBody.Builder().add("depo", depo).add("contractParty", input).build();

        Request request = new Request.Builder().url("https://vtc3pl.com/fetch_contract_party_prn_app.php").post(formBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity8.this, "Failed to fetch contract parties", Toast.LENGTH_SHORT).show();
                });
//                if (e instanceof SocketTimeoutException) {
//                    Log.e("SocketTimeoutException", "Read timed out");
//                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        String responseBody = body.string();
                        try {
                            JSONArray jsonArray = new JSONArray(responseBody);
                            List<String> contractParties = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String contractParty = jsonObject.getString("CustCode") + " : " + jsonObject.getString("CustName") + " : " + jsonObject.getString("IndType");
                                contractParties.add(contractParty);
                            }

                            runOnUiThread(() -> {
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity8.this, android.R.layout.simple_spinner_item, contractParties);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                contractPartySpinnerActivityEight.setAdapter(adapter);
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity8.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        Log.e("Response Error", "Response body is null");
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity8.this, "Empty response body", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity8.this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void displayDataInTable(JSONArray jsonArray) {
        // Clear existing table rows
        tableLayoutActivityEight.removeAllViews();

        // Check if jsonArray is empty
        if (jsonArray.length() == 0) {
            showAlertDialog("No Data", "Data not found");
            return;
        }

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

    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(MainActivity8.this).setTitle(title).setMessage(message).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Dismiss the dialog
                contractPartyEditTextActivityEight.setText("");
                contractPartyFinalEditTextActivityEight.setText("");
                contractPartySpinnerActivityEight.setSelection(0);
            }
        }).setIcon(android.R.drawable.ic_dialog_alert).show();
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