package com.vtc3pl.prnapp2024v2;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity3 extends AppCompatActivity {

    private final Handler handler = new Handler();
    private TextView showUserNameTextViewActivityThree;
    private EditText editTextFromDateActivityThree, editTextToDateActivityThree, vehicleNumberEditText , contractPartyEditText;
    private Calendar fromCalendar, toCalendar;
    private DatePickerDialog.OnDateSetListener fromDateSetListener, toDateSetListener;
    private String username = "", depo = "", year = "";
    private List<String> contractPartiesList , vehicleNumberList;
    private ArrayAdapter<String> spinnerAdapter, vehicleNumberSpinnerAdapter;
    private Spinner contractPartySpinner, vehicleNumberSpinner;
    private Runnable runnable;

    private boolean isItemSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main3);

        showUserNameTextViewActivityThree = findViewById(R.id.showUserNameTextViewActivityThree);
        editTextFromDateActivityThree = findViewById(R.id.editTextFromDateActivityThree);
        editTextToDateActivityThree = findViewById(R.id.editTextToDateActivityThree);

        contractPartyEditText = findViewById(R.id.contractPartyEditText);
        vehicleNumberEditText = findViewById(R.id.vehicleNumberEditText);

        contractPartySpinner = findViewById(R.id.contractPartySpinner);
        vehicleNumberSpinner = findViewById(R.id.vehicleNumberSpinner);

        fromCalendar = Calendar.getInstance();
        toCalendar = Calendar.getInstance();

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
            depo = intent.getStringExtra("depo");
            year = intent.getStringExtra("year");

            // Set the fetched username to the TextView
            if (username != null) {
                String usernameText = getString(R.string.user_name_prefix, username);
                showUserNameTextViewActivityThree.setText(usernameText);
            }
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
        contractPartySpinner.setAdapter(spinnerAdapter);

        contractPartyEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Fetch contract parties based on the text entered
                fetchContractPartiesWithDelay(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        contractPartySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // Check if the item selection was initiated programmatically
                if (!isItemSelected) {
                    // Clear the spinner selection
                    contractPartySpinner.setSelection(0);
                } else {
                    // Set flag to false since this selection was initiated by the user
                    isItemSelected = false;

                    // Clear the contract parties list
                    contractPartiesList.clear();

                    // Notify the spinner adapter that the data set has changed
                    spinnerAdapter.notifyDataSetChanged();
                }

                // Set the selected contract party in the edit text
                contractPartyEditText.setText(contractPartiesList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Not needed
            }
        });


        vehicleNumberList = new ArrayList<>();
        vehicleNumberSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, contractPartiesList);
        vehicleNumberSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vehicleNumberSpinner.setAdapter(vehicleNumberSpinnerAdapter);

        vehicleNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Fetch contract parties based on the text entered
//                fetchContractPartiesWithDelay(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        vehicleNumberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // Check if the item selection was initiated programmatically
                if (!isItemSelected) {
                    // Clear the spinner selection
                    vehicleNumberSpinner.setSelection(0);
                } else {
                    // Set flag to false since this selection was initiated by the user
                    isItemSelected = false;

                    // Clear the contract parties list
                    vehicleNumberList.clear();

                    // Notify the spinner adapter that the data set has changed
                    vehicleNumberSpinnerAdapter.notifyDataSetChanged();
                }

                // Set the selected contract party in the edit text
                vehicleNumberEditText.setText(vehicleNumberList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Not needed
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            try {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            } catch (IllegalArgumentException e) {
                Log.e("IllegalArgumentExcep : ", "Must pass in a valid surface control if only instrument surface");
            }
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
        editTextFromDateActivityThree.setText(android.text.format.DateFormat.format(dateFormat, fromCalendar));
    }

    private void updateToDate() {
        String dateFormat = "yyyy/MM/dd";
        editTextToDateActivityThree.setText(android.text.format.DateFormat.format(dateFormat, toCalendar));
    }

    private void fetchContractPartiesWithDelay(String input) {
        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }
        runnable = new Runnable() {
            @Override
            public void run() {
                fetchContractParties(input);
            }
        };
        handler.postDelayed(runnable, 500); // Delay of 500 milliseconds
    }

    private void fetchVehicleNumbersWithDelay(String input) {
        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }
        runnable = new Runnable() {
            @Override
            public void run() {
                fetchVehicleNumbers(input);
            }
        };
        handler.postDelayed(runnable, 500); // Delay of 500 milliseconds
    }

    private void fetchContractParties(String input) {
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("depo", depo)
                .add("contractParty", input)
                .build();

        Request request = new Request.Builder()
                .url("https://vtc3pl.com/fetch_contract_party_prn_app.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (e instanceof SocketTimeoutException) {
                    Log.e("SocketTimeoutException", "Read timed out");
                } else {
                    e.printStackTrace();
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        final String responseData = body.string();
                        Log.d("OnResponse", responseData);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                handleResponse(responseData);
                            }
                        });
                    } else {
                        Log.e("Response Error", "Response body is null");
                    }
                }
            }
        });
    }

    private void handleResponse(String responseData) {
        try {
            JSONArray jsonArray = new JSONArray(responseData);
            Log.d("JSON handleResponse()", String.valueOf(jsonArray));

            // Create a temporary list to store contract parties
            List<String> tempContractPartiesList = new ArrayList<>();

            // Iterate through the JSON array and add contract parties to the temporary list
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String contractParty = jsonObject.getString("CustCode") + " : " +
                        jsonObject.getString("CustName") + " : " +
                        jsonObject.getString("IndType");
                tempContractPartiesList.add(contractParty);
            }

            // Clear the existing list and add all items from the temporary list
//            contractPartiesList.clear();
            contractPartiesList.addAll(tempContractPartiesList);

            // Notify the spinner adapter that the data set has changed
            spinnerAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void fetchVehicleNumbers(String input) {
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("depo", depo)
                .add("contractParty", input)
                .build();

        Request request = new Request.Builder()
                .url("https://vtc3pl.com/vehnum.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (e instanceof SocketTimeoutException) {
                    Log.e("SocketTimeoutException", "Read timed out");
                } else {
                    e.printStackTrace();
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        final String responseData = body.string();
                        Log.d("OnResponse", responseData);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                handleResponseForVehicleNumbers(responseData);
                            }
                        });
                    } else {
                        Log.e("Response Error", "Response body is null");
                    }
                }
            }
        });
    }

    private void handleResponseForVehicleNumbers(String responseData) {
        try {
            JSONArray jsonArray = new JSONArray(responseData);
            Log.d("JSON handleResponse()", String.valueOf(jsonArray));

            // Create a temporary list to store contract parties
            List<String> tempVehicleNumberList = new ArrayList<>();

            // Iterate through the JSON array and add contract parties to the temporary list
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String contractParty = jsonObject.getString("CustCode") + " : " +
                        jsonObject.getString("CustName") + " : " +
                        jsonObject.getString("IndType");
                tempVehicleNumberList.add(contractParty);
            }

            vehicleNumberList.addAll(tempVehicleNumberList);

            vehicleNumberSpinnerAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}