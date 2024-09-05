package com.vtc3pl.prnapp2024v2;
//Company wise PRN Create Page

import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity3 extends AppCompatActivity {

    private final Handler handler = new Handler();
    private double totalBoxWeightFromAllLRNO = 0, totalBoxQtyFromAllLRNO = 0, totalBagWeightFromAllLRNO = 0, totalBagQtyFromAllLRNO = 0;
    private TextView showUserNameTextViewActivityThree;
    private EditText editTextFromDateActivityThree, editTextToDateActivityThree, vehicleNumberEditText, contractPartyEditText, contractPartyFinalEditText, hamaliAmountEditTextActivityThree, deductionAmountEditTextActivityThree, amountPaidToHVendorEditTextActivityThree, totalBoxQtyEditTextActivityThree, totalBagWeightEditTextActivityThree;
    private Button getLRNOButton, createPRN;
    private Calendar fromCalendar, toCalendar;
    private DatePickerDialog.OnDateSetListener fromDateSetListener, toDateSetListener;
    private String username = "", depo = "", year = "", custName = "";
    private List<String> contractPartiesList, vehicleNumberList;
    private ArrayAdapter<String> spinnerAdapter, vehicleNumberSpinnerAdapter;
    private Spinner contractPartySpinner, vehicleNumberSpinner, hamaliVendorNameSpinnerActivityThree, hamaliTypeSpinnerActivityThree;
    private Runnable runnable;
    //    private boolean isItemSelected = false;
    private boolean isSpinnerSelected = false;
    private Set<String> selectedLRNOs = new HashSet<>();
    private String selectedHamaliVendor = "";
    private String selectedHamaliType = "";
    private double amountPaidToHVendor, deductionAmount;

    private String[] parts = {""};
    private String contractParty = "";
    private String contractPartyCode = "";
    private TableLayout tableLayout;

    private Handler handlerSecond = new Handler();
    private Runnable updateSpinnerRunnable;

    private LottieAnimationView lottieAnimationView;
    private View blockingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main3);

        getLRNOButton = findViewById(R.id.getLRNOButton);
        createPRN = findViewById(R.id.createPRN);
        createPRN.setOnClickListener(v -> submitDataToServer());

        tableLayout = findViewById(R.id.tableLayout);

        showUserNameTextViewActivityThree = findViewById(R.id.showUserNameTextViewActivityThree);
        editTextFromDateActivityThree = findViewById(R.id.editTextFromDateActivityThree);
        editTextToDateActivityThree = findViewById(R.id.editTextToDateActivityThree);

        contractPartyEditText = findViewById(R.id.contractPartyEditText);
        contractPartyFinalEditText = findViewById(R.id.contractPartyFinalEditText);
        contractPartyFinalEditText.setEnabled(false);

        vehicleNumberEditText = findViewById(R.id.vehicleNumberEditText);
        vehicleNumberEditText.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        hamaliAmountEditTextActivityThree = findViewById(R.id.hamaliAmountEditTextActivityThree);
        hamaliAmountEditTextActivityThree.setEnabled(false);

        deductionAmountEditTextActivityThree = findViewById(R.id.deductionAmountEditTextActivityThree);
        amountPaidToHVendorEditTextActivityThree = findViewById(R.id.amountPaidToHVendorEditTextActivityThree);
        amountPaidToHVendorEditTextActivityThree.setEnabled(false);

        totalBoxQtyEditTextActivityThree = findViewById(R.id.totalBoxQtyEditTextActivityThree);
        totalBoxQtyEditTextActivityThree.setEnabled(false);

        totalBagWeightEditTextActivityThree = findViewById(R.id.totalBagWeightEditTextActivityThree);
        totalBagWeightEditTextActivityThree.setEnabled(false);

        contractPartySpinner = findViewById(R.id.contractPartySpinner);
        vehicleNumberSpinner = findViewById(R.id.vehicleNumberSpinner);
        hamaliVendorNameSpinnerActivityThree = findViewById(R.id.hamaliVendorNameSpinnerActivityThree);
        hamaliTypeSpinnerActivityThree = findViewById(R.id.hamaliTypeSpinnerActivityThree);

        lottieAnimationView = findViewById(R.id.lottieAnimationView);
        blockingView = findViewById(R.id.blockingView);

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

        fromDateSetListener = (view, year, month, dayOfMonth) -> {
            fromCalendar.set(Calendar.YEAR, year);
            fromCalendar.set(Calendar.MONTH, month);
            fromCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateFromDate();
        };

        toDateSetListener = (view, year, month, dayOfMonth) -> {
            toCalendar.set(Calendar.YEAR, year);
            toCalendar.set(Calendar.MONTH, month);
            toCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateToDate();
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
                if (updateSpinnerRunnable != null) {
                    handlerSecond.removeCallbacks(updateSpinnerRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isSpinnerSelected) { // Only fetch if not a spinner selection
                    String searchText = s.toString().trim();
                    if (!searchText.isEmpty()) {
                        updateSpinnerRunnable = () -> {
                            // Call the method to update the contract party list
                            fetchContractParties(searchText);
                        };
                        // Post the update with a slight delay to debounce
                        handlerSecond.postDelayed(updateSpinnerRunnable, 300);
                    }
                }
                isSpinnerSelected = false; // Reset the flag
            }
        });

        contractPartySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e("Spinner", "onItemSelected called");

                String selectedContractParty = (String) parent.getItemAtPosition(position);

                Log.e("Selected ContractParty:", selectedContractParty);

                isSpinnerSelected = true; // Set the flag before updating the EditText
                contractPartyFinalEditText.setText(selectedContractParty);
                //contractPartiesList.clear();
                //spinnerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Not needed
            }
        });

        vehicleNumberList = new ArrayList<>();
        vehicleNumberSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, vehicleNumberList);
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
                fetchVehicleNumbersWithDelay(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        vehicleNumberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                vehicleNumberEditText.setText(vehicleNumberList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Not needed
            }
        });

        fetchHvendors();

        hamaliVendorNameSpinnerActivityThree.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedVendor = parent.getItemAtPosition(position).toString();
                if (selectedVendor.equals("No hamali Vendor")) {
                    hamaliAmountEditTextActivityThree.setText("0.0");
                    hamaliAmountEditTextActivityThree.setEnabled(false);

                    deductionAmountEditTextActivityThree.setText("0.0");

                    amountPaidToHVendorEditTextActivityThree.setText("0.0");
                    amountPaidToHVendorEditTextActivityThree.setEnabled(false);
                } else {
                    // If user select any other value then calculate,
                    deductionAmountEditTextActivityThree.setEnabled(true);
                    calculateHamali();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing if nothing is selected
            }
        });

        hamaliTypeSpinnerActivityThree.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calculateHamali();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing if nothing is selected
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

    private void fetchVehicleNumbersWithDelay(String input) {
        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }
        runnable = () -> fetchVehicleNumbers(input);
        handler.postDelayed(runnable, 500); // Delay of 500 milliseconds
    }

    private void fetchContractParties(String input) {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        RequestBody formBody = new FormBody.Builder().add("depo", depo).add("contractParty", input).build();

        Request request = new Request.Builder().url("https://vtc3pl.com/fetch_contract_party_prn_app.php").post(formBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity3.this, "Failed to fetch contract parties", Toast.LENGTH_SHORT).show());
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
//                                contractParties.add(jsonArray.getString(i));
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String contractParty = jsonObject.getString("CustCode") + " : " + jsonObject.getString("CustName") + " : " + jsonObject.getString("IndType");
                                contractParties.add(contractParty);
//                tempContractPartiesList.add(contractParty);
                            }

                            runOnUiThread(() -> {
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity3.this, android.R.layout.simple_spinner_item, contractParties);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                contractPartySpinner.setAdapter(adapter);
                            });
                        } catch (JSONException e) {
                            runOnUiThread(() -> showAlert("JSON Error ", "Error parsing JSON response"));
                        }
                    } else {
                        runOnUiThread(() -> showAlert("Empty Response ", "Empty response body"));
                    }
                } else {
                    runOnUiThread(() -> showAlert("Server Error ", "Server error: " + response.code()));
                }
            }
        });
    }

    private void fetchVehicleNumbers(String input) {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://vtc3pl.com/vehnum.php").newBuilder();
        urlBuilder.addQueryParameter("term", input);
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (e instanceof SocketTimeoutException) {
                    showAlert("Socket Timeout Error ", "Read timed out");
                } else {
                    showAlert("Connection Failed ", "Failed to connect to server");
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        final String responseData = body.string();
                        Log.d("OnResponse", responseData);
                        runOnUiThread(() -> handleResponseForVehicleNumbers(responseData));
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
            vehicleNumberList.clear();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String vehicleNumber = jsonObject.getString("Vehicle_No");
                vehicleNumberList.add(vehicleNumber);
            }
            vehicleNumberSpinnerAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            runOnUiThread(() -> showAlert("Response Error", "Error on vehicle number response."));
        }
    }

    public void onGetLRNOButtonClick(View view) {
        // Retrieve input values
        String fromDate = editTextFromDateActivityThree.getText().toString().trim();
        Log.e("FromDate", fromDate);
        String toDate = editTextToDateActivityThree.getText().toString().trim();
        Log.e("ToDate", toDate);
        String contractPartyText = contractPartyFinalEditText.getText().toString().trim();
        Log.e("WholeContractPartyText", contractPartyText);
        parts = contractPartyText.split(":");
        contractParty = parts.length >= 2 ? parts[1].trim() : "";
        Log.e("OnlyContractParty", contractParty);
        contractPartyCode = parts.length >= 2 ? parts[0].trim() : "";
        Log.e("OnlyContractPartyCode", contractPartyCode);

        // Validate input values
        if (TextUtils.isEmpty(fromDate)) {
            showWarning("Empty From Date Warning", "Please choose From Date");
            editTextFromDateActivityThree.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(toDate)) {
            showWarning("Empty To Date Warning", "Please choose From Date");
            editTextToDateActivityThree.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(contractParty)) {
            showWarning("Field Empty Warning", "Please enter Contract Party");
            contractPartyEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(vehicleNumberEditText, InputMethodManager.SHOW_IMPLICIT);
            return;
        }

        runOnUiThread(() -> {
            blockingView.setVisibility(View.VISIBLE);
            lottieAnimationView.setVisibility(View.VISIBLE);
            lottieAnimationView.playAnimation();
        });

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        // Construct the POST request body
        RequestBody formBody = new FormBody.Builder().add("fromDate", fromDate).add("toDate", toDate).add("consignor", contractParty).add("depo", depo).build();

        // Create the request
        Request request = new Request.Builder().url("https://vtc3pl.com/get_company_wise_lrno_for_create_prn.php").post(formBody).build();

        // Execute the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    lottieAnimationView.setVisibility(View.GONE);
                    blockingView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();
                    showAlert("Connection Failed", "Failed to connect to server");
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    lottieAnimationView.setVisibility(View.GONE);
                    blockingView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();
                });

                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        String responseData = body.string();
                        Log.d("Reponse For LR :", responseData);
                        if (responseData.equals("[0]")) {
                            runOnUiThread(() -> showWarning("LR Not Found Warning ", "This LR is not available."));
                        } else {
                            handleLRNOResponse(responseData);
                        }
                    } else {
                        runOnUiThread(() -> showAlert("Empty Response", "Empty response is received from server"));
                    }
                } else {
                    runOnUiThread(() -> showAlert("Response Error ", "Unsuccessful response " + response.code()));
                }
            }
        });
    }

    private void fetchWeightsFromServer() {

        runOnUiThread(() -> {
            blockingView.setVisibility(View.VISIBLE);
            lottieAnimationView.setVisibility(View.VISIBLE);
            lottieAnimationView.playAnimation();
        });

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        //When getting the LR if status is 11 then do the calculation after missmatch for that perticuler LR
        // URL for fetching weights
        String url = "https://vtc3pl.com/hamali_bag_box_weight_prn_app.php";

        RequestBody formBody = new FormBody.Builder().add("depo", depo).build();

        Request request = new Request.Builder().url(url).post(formBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(() -> {
                    lottieAnimationView.setVisibility(View.GONE);
                    blockingView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();
                    showAlert("Connection Failed ", "Failed to fetch weights from server");
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                runOnUiThread(() -> {
                    lottieAnimationView.setVisibility(View.GONE);
                    blockingView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();
                });

                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        String responseBody = body.string();
                        Log.d("Response Str(BagAndBox)", responseBody);
                        try {
                            JSONArray jsonArray = new JSONArray(responseBody);
                            double totalBoxWeight = 0;
                            double totalBoxQty = 0;
                            double totalBagWeight = 0;
                            double totalBagQty = 0;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String lrNumber = jsonObject.getString("LRNO");
                                if (selectedLRNOs.contains(lrNumber)) {
                                    Log.d("BagAndBox", "Lrno : " + lrNumber + ", [BoxQty: " + jsonObject.getDouble("TotalBoxQty") + " ]" + ", [BagWeight: " + jsonObject.getDouble("TotalWeightBag") + " ]");
                                    totalBoxWeight += jsonObject.getDouble("TotalWeightBox");
                                    totalBagWeight += jsonObject.getDouble("TotalWeightBag");
                                    totalBoxQty += jsonObject.getDouble("TotalBoxQty");
                                    totalBagQty += jsonObject.getDouble("TotalBagQty");
                                }
                            }

                            totalBoxWeightFromAllLRNO = totalBoxWeight;
                            totalBagWeightFromAllLRNO = totalBagWeight;
                            totalBoxQtyFromAllLRNO = totalBoxQty;
                            totalBagQtyFromAllLRNO = totalBagQty;

                            // Update the UI on the main thread
                            runOnUiThread(() -> {
                                totalBoxQtyEditTextActivityThree.setText(String.valueOf(totalBoxQtyFromAllLRNO));
                                totalBagWeightEditTextActivityThree.setText(String.valueOf(totalBagWeightFromAllLRNO));

                                Log.d("totalBoxWeight : ", String.valueOf(totalBoxWeightFromAllLRNO));
                                Log.d("totalBagWeight : ", String.valueOf(totalBagWeightFromAllLRNO));

                                Log.d("totalBoxQty : ", String.valueOf(totalBoxQtyFromAllLRNO));
                                Log.d("totalBagQty : ", String.valueOf(totalBagQtyFromAllLRNO));
                            });
                        } catch (JSONException e) {
                            runOnUiThread(() -> showAlert("Wrong Response Error ", "Error parsing JSON response"));
                        }
                    } else {
                        runOnUiThread(() -> showAlert("Empty Response Error ", "Response body is null(Box Qty and Bag Weight)"));
                    }
                } else {
                    onFailure(call, new IOException("Unexpected response code " + response));
                }
            }
        });
    }

    private void handleLRNOResponse(String responseData) {
        Log.d("handleLRNOResponse() : ", responseData);
        runOnUiThread(() -> {
            try {
                JSONArray jsonArray = new JSONArray(responseData);
                TableLayout tableLayout = findViewById(R.id.tableLayout);

                // Remove all existing rows to refresh the table
                tableLayout.removeAllViews();

                // Add headers row
                TableRow headersRow = new TableRow(MainActivity3.this);

                TextView srNoHeader = new TextView(MainActivity3.this);
                srNoHeader.setText(R.string.sr_no);
                srNoHeader.setPadding(8, 8, 8, 8);
                srNoHeader.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                srNoHeader.setTypeface(null, Typeface.BOLD);
                headersRow.addView(srNoHeader);

                TextView lrNoHeader = new TextView(MainActivity3.this);
                lrNoHeader.setText(R.string.lr_no_header);
                lrNoHeader.setPadding(8, 8, 8, 8);
                lrNoHeader.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                lrNoHeader.setTypeface(null, Typeface.BOLD);
                headersRow.addView(lrNoHeader);

                // Create CheckBox for "Select All"
                CheckBox selectAllCheckBox = new CheckBox(MainActivity3.this);
                selectAllCheckBox.setText(R.string.select_all);
                selectAllCheckBox.setPadding(8, 8, 8, 8);
                selectAllCheckBox.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                selectAllCheckBox.setTypeface(null, Typeface.BOLD);
                selectAllCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    // Toggle selection of all checkboxes in the table
                    for (int i = 1; i < tableLayout.getChildCount(); i++) {
                        View view = tableLayout.getChildAt(i);
                        if (view instanceof TableRow) {
                            TableRow row = (TableRow) view;
                            CheckBox checkBox = (CheckBox) row.getChildAt(2);
                            checkBox.setChecked(isChecked);
                        }
                    }
                });
                headersRow.addView(selectAllCheckBox);

                tableLayout.addView(headersRow);

                // Iterate through the JSON array and add rows to the table
                for (int i = 0; i < jsonArray.length(); i++) {
                    String lrNo = jsonArray.getString(i);

                    // Create a new TableRow
                    TableRow newRow = new TableRow(MainActivity3.this);

                    // Create TextViews for SrNo and LRNO
                    TextView srNoTextView = new TextView(MainActivity3.this);
                    srNoTextView.setText(String.valueOf(i + 1)); // SrNo starts from 1
                    srNoTextView.setPadding(8, 8, 8, 8);
                    srNoTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    newRow.addView(srNoTextView);

                    TextView lrNoTextView = new TextView(MainActivity3.this);
                    lrNoTextView.setText(lrNo);
                    lrNoTextView.setPadding(8, 8, 8, 8);
                    lrNoTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    newRow.addView(lrNoTextView);

                    // Create CheckBox for selection
                    CheckBox selectCheckBox = new CheckBox(MainActivity3.this);
                    selectCheckBox.setTag(lrNo); // Set LRNO as tag
                    selectCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        String lrNo1 = (String) buttonView.getTag();
                        if (isChecked) {
                            selectedLRNOs.add(lrNo1);
                            Log.d("LRNO Added : ", selectedLRNOs.toString());
                        } else {
                            // Remove LRNO from the selectedLRNOs collection
                            selectedLRNOs.remove(lrNo1);
                            Log.d("LRNO Remove : ", selectedLRNOs.toString());

                            totalBoxQtyFromAllLRNO -= totalBoxQtyFromAllLRNO;
                            totalBagWeightFromAllLRNO -= totalBagWeightFromAllLRNO;
                        }
                        fetchWeightsFromServer();
                        calculateHamali();
                    });
                    newRow.addView(selectCheckBox);

                    // Add the new row to the TableLayout
                    tableLayout.addView(newRow);
                }
            } catch (JSONException e) {
                Log.e("JSON Exception : ", String.valueOf(e));
                showAlert("JSON Error ", "Error parsing JSON response");
            }
        });
    }

    private void fetchHvendors() {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        // URL for fetching Hvendors
        String url = "https://vtc3pl.com/fetch_hamalivendor_only_prn_app.php";

        // Create a form body with spinnerDepo as a parameter
        FormBody formBody = new FormBody.Builder().add("spinnerDepo", depo).build();

        Request request = new Request.Builder().url(url).post(formBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        String responseBody = body.string();
                        List<String> hVendors = new ArrayList<>();
                        try {
                            JSONArray jsonArray = new JSONArray(responseBody);
                            if (jsonArray.length() > 0) {
                                hVendors.add("Please Select Vendor");
                                hVendors.add("No hamali Vendor");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    String hVendor = jsonArray.getString(i);
                                    hVendors.add(hVendor);
                                }
                            } else {
                                runOnUiThread(() -> showAlert("Error", "hamali vendors not found."));
                            }
                        } catch (JSONException e) {
                            runOnUiThread(() -> showAlert("Wrong Response Error", "Wrong response received from server."));
                        }

                        // Update the spinner UI on the main thread
                        runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity3.this, android.R.layout.simple_spinner_item, hVendors);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            hamaliVendorNameSpinnerActivityThree.setAdapter(adapter); // Use hamaliVendorNameSpinner instead of goDownSpinner
                        });
                    } else {
                        runOnUiThread(() -> showAlert("Empty Response Error", "Empty response received from server for vendors."));
                    }
                } else {
                    onFailure(call, new IOException("Unexpected response code " + response));
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(() -> showAlert("Connection Failed", "Failed to fetch Hamali Vendors"));
            }
        });
    }

    private void calculateHamali() {
        Log.d("calculateHamali() :", "Method is invoked");

        if (hamaliVendorNameSpinnerActivityThree.getSelectedItem() == null || hamaliTypeSpinnerActivityThree.getSelectedItem() == null) {
            // One or both spinners are not selected, return without calculating hamali
            return;
        }

        selectedHamaliVendor = hamaliVendorNameSpinnerActivityThree.getSelectedItem().toString();

        selectedHamaliType = hamaliTypeSpinnerActivityThree.getSelectedItem().toString();

        runOnUiThread(() -> {
            blockingView.setVisibility(View.VISIBLE);
            lottieAnimationView.setVisibility(View.VISIBLE);
            lottieAnimationView.playAnimation();
        });

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("spinnerDepo", depo);
        formBuilder.add("Hvendor", selectedHamaliVendor);

        Request request = new Request.Builder().url("https://vtc3pl.com/fetch_hamali_rates_calculation_prn_app.php").post(formBuilder.build()).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    lottieAnimationView.setVisibility(View.GONE);
                    blockingView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();
                    showAlert("Connection Failed ", "Failed to fetch hamali rates from server");
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                runOnUiThread(() -> {
                    lottieAnimationView.setVisibility(View.GONE);
                    blockingView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();
                });

                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        String responseBody = body.string();
                        try {
                            JSONObject jsonObject = new JSONObject(responseBody);

                            double boxRate;
                            double bagRate;

                            if (selectedHamaliType.equals("Regular")) {
                                if (jsonObject.has("Regular")) {
                                    boxRate = Double.parseDouble(jsonObject.getString("Regular"));
                                    bagRate = Double.parseDouble(String.valueOf(jsonObject.getInt("Regularbag")));
                                    Log.d("Regular boxrate : ", boxRate + " bag rate : " + bagRate);
                                } else {
                                    Log.d("Regular Else part : ", "Inside else part just after opening MainActivity2.java");
                                    return;
                                }
                            } else if (selectedHamaliType.equals("Crossing")) {
                                if (jsonObject.has("Crossing")) {
                                    boxRate = Double.parseDouble(jsonObject.getString("Crossing"));
                                    bagRate = Double.parseDouble(String.valueOf(jsonObject.getInt("Crossingbag")));
                                    Log.d("Crossing box rate : ", boxRate + " bag rate : " + bagRate);
                                } else {
                                    Log.d("Crossing Else part : ", "Inside else part just after opening MainActivity2.java");
                                    return;
                                }
                            } else {
                                runOnUiThread(() -> showAlert("Unknown hamali Type Error", "Unknown hamali type selected."));
                                return;
                            }

                            // Perform calculations
                            double hamaliBoxValue = (boxRate * totalBoxQtyFromAllLRNO);
                            Log.d("hamaliBoxValue : ", String.valueOf(hamaliBoxValue));
                            double ratePerTon = bagRate;
                            double weightInTons = (totalBagWeightFromAllLRNO / 1000);
                            Log.d("weightInTons : ", String.valueOf(weightInTons));
                            double hamaliBagValue = (weightInTons * ratePerTon);
                            Log.d("hamaliBagValue : ", String.valueOf(hamaliBagValue));
                            double totalHamaliAmount = (hamaliBoxValue + hamaliBagValue);
                            Log.d("totalHamaliAmount : ", String.valueOf(totalHamaliAmount));

                            runOnUiThread(() -> {
                                // Update UI or perform calculations here with the rates
                                hamaliAmountEditTextActivityThree.setText(String.valueOf(totalHamaliAmount));
                                hamaliAmountEditTextActivityThree.setEnabled(false);
                            });

                            // Assuming you have deductionAmountEditText and amountPaidToHVendorEditText declared and initialized
                            // Assuming these variables are declared globally
                            deductionAmountEditTextActivityThree.setOnKeyListener((v, keyCode, event) -> {
                                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                                    deductionAmount = Double.parseDouble(deductionAmountEditTextActivityThree.getText().toString());

                                    if (deductionAmount < 0) {
                                        // Prevent deduction amount from being less than zero
                                        Toast.makeText(MainActivity3.this, "Deduction amount cannot be less than zero", Toast.LENGTH_SHORT).show();
                                        deductionAmountEditTextActivityThree.setText("0.0");
                                        return true;
                                    }
                                    amountPaidToHVendor = (totalHamaliAmount - deductionAmount);
                                    Log.d("amountPaidToHVendor : ", String.valueOf(amountPaidToHVendor));
                                    amountPaidToHVendorEditTextActivityThree.setText(String.valueOf(amountPaidToHVendor));
                                    amountPaidToHVendorEditTextActivityThree.setEnabled(false);
                                    return true;
                                }
                                return false;
                            });

                            deductionAmountEditTextActivityThree.setOnFocusChangeListener((v, hasFocus) -> {
                                if (!hasFocus) {
                                    // Calculate amount paid to vendor when deduction amount is entered
                                    String deductionAmountStr = deductionAmountEditTextActivityThree.getText().toString().trim();
                                    deductionAmount = deductionAmountStr.isEmpty() ? 0.0 : Double.parseDouble(deductionAmountStr);
                                    amountPaidToHVendor = Double.parseDouble(hamaliAmountEditTextActivityThree.getText().toString()) - deductionAmount;
                                    amountPaidToHVendorEditTextActivityThree.setText(String.valueOf(amountPaidToHVendor));
                                    amountPaidToHVendorEditTextActivityThree.setEnabled(false);
                                }
                            });

                            // Set up hamaliAmountEditText listener
                            hamaliAmountEditTextActivityThree.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                    // Not needed
                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    // Not needed
                                }

                                @Override
                                public void afterTextChanged(Editable s) {

                                    String hamaliAmountStr = s.toString().trim();
                                    double hamaliAmount = hamaliAmountStr.isEmpty() ? 0.0 : Double.parseDouble(hamaliAmountStr);
                                    String deductionAmountStr = deductionAmountEditTextActivityThree.getText().toString().trim();
                                    deductionAmount = deductionAmountStr.isEmpty() ? 0.0 : Double.parseDouble(deductionAmountStr);
                                    amountPaidToHVendor = (hamaliAmount - deductionAmount);
                                    amountPaidToHVendorEditTextActivityThree.setText(String.valueOf(amountPaidToHVendor));
                                    amountPaidToHVendorEditTextActivityThree.setEnabled(false);
                                }
                            });

                        } catch (JSONException e) {
                            runOnUiThread(() -> showAlert("Parsing Response Error", "Wrong response receive from server"));
                        }
                    } else {
                        runOnUiThread(() -> showAlert("Hamali amount Error", "Hamali rates received empty"));
                    }
                } else {
                    runOnUiThread(() -> showAlert("Server Error", "Server error: " + response.code()));
                }
            }
        });
    }

    private void clearUIComponents() {
        contractPartyEditText.setText("");
        contractPartyFinalEditText.setText("");
        vehicleNumberEditText.setText("");
        vehicleNumberSpinner.setSelection(0);
        editTextFromDateActivityThree.setText("");
        editTextToDateActivityThree.setText("");
        totalBoxQtyEditTextActivityThree.setText("");
        totalBagWeightEditTextActivityThree.setText("");
        tableLayout.removeAllViews();
        selectedLRNOs.clear();
        contractPartySpinner.setSelection(0);
        hamaliVendorNameSpinnerActivityThree.setSelection(0);
        amountPaidToHVendorEditTextActivityThree.setText("");
        hamaliTypeSpinnerActivityThree.setSelection(0);
        deductionAmountEditTextActivityThree.setText("");
        hamaliAmountEditTextActivityThree.setText("");

        finish();
    }


    private void submitDataToServer() {

        // Retrieve data from UI components
        String vehicleNo = vehicleNumberEditText.getText().toString();
        String fromDate = editTextFromDateActivityThree.getText().toString();
        String toDate = editTextToDateActivityThree.getText().toString();
        String contractPartyFinal = contractPartyFinalEditText.getText().toString().trim();
        String totalBoxQty = totalBoxQtyEditTextActivityThree.getText().toString().trim();
        String totalBagWeight = totalBagWeightEditTextActivityThree.getText().toString().trim();
        String amountPaidToHVendor = amountPaidToHVendorEditTextActivityThree.getText().toString().trim();
        String deductionAmount = deductionAmountEditTextActivityThree.getText().toString().trim();
        String hamaliAmount = hamaliAmountEditTextActivityThree.getText().toString().trim();


        if (fromDate.isEmpty() || toDate.isEmpty()) {
            showWarning("Empty Date Warning", "Please select a date");
            return;
        }

        if (vehicleNo.isEmpty()) {
            showWarning("Empty Field Warning", "Please give vehicle number");
            vehicleNumberEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(vehicleNumberEditText, InputMethodManager.SHOW_IMPLICIT);
            return;
        }

        if (contractPartyFinal.isEmpty()) {
            showWarning("Empty Field Warning", "Please enter and select contract party.");
            contractPartyFinalEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(contractPartyFinalEditText, InputMethodManager.SHOW_IMPLICIT);
            return;
        }

        if (totalBoxQty.isEmpty() || totalBagWeight.isEmpty()) {
            showWarning("Empty Field Warning", "Please select at least on LRNO.");
            return;
        }

        if (selectedHamaliVendor.equals(getString(R.string.please_select_vendor))) {
            showWarning("Unselected Field Warning", "Please select hamali vendor name.");
            return;
        }

        if (amountPaidToHVendor.isEmpty() || hamaliAmount.isEmpty()) {
            showWarning("Empty Amount Warning", "Choose hamali type or name to get the amount recalculate.");
            return;
        }

        List<String> lrNumbers = new ArrayList<>();
        for (String lrNumber : selectedLRNOs) {
            lrNumbers.add(lrNumber);
        }

        // Convert lrNumbers list to JSON array
        JSONArray jsonArray = new JSONArray();
        for (String lrNumber : lrNumbers) {
            jsonArray.put(lrNumber);
        }
        String arrayListOfLR = jsonArray.toString();

        runOnUiThread(() -> {
            blockingView.setVisibility(View.VISIBLE);
            lottieAnimationView.setVisibility(View.VISIBLE);
            lottieAnimationView.playAnimation();
        });

        // Make HTTP request
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("UserName", username);
        formBuilder.add("spinnerDepo", depo);
        formBuilder.add("vehicleNo", vehicleNo);
        formBuilder.add("arrayListOfLR", arrayListOfLR);
        formBuilder.add("fromDate", fromDate);
        formBuilder.add("spinnerYear", year);
        formBuilder.add("toDate", toDate);
        formBuilder.add("contractParty", contractParty);
        formBuilder.add("contractPartyCode", contractPartyCode);
        formBuilder.add("selectedHamaliVendor", selectedHamaliVendor);
        formBuilder.add("finalHamliAmount", String.valueOf(amountPaidToHVendor));
        formBuilder.add("selectedHamaliType", selectedHamaliType);
        formBuilder.add("deductionAmount", String.valueOf(deductionAmount));

        Request request = new Request.Builder().url("https://vtc3pl.com/create_prn_prn_app.php").post(formBuilder.build()).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    lottieAnimationView.setVisibility(View.GONE);
                    blockingView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();
                    showAlert("Connection Failed", "Failed to connect to server");
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                runOnUiThread(() -> {
                    lottieAnimationView.setVisibility(View.GONE);
                    blockingView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();
                });

                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        String responseBody = body.string();
                        Log.e("Response CreatePRN:", responseBody);

                        if (responseBody.startsWith("fail")) {
                            runOnUiThread(() -> showAlert("Transaction Failed ", responseBody));
                        } else {
                            runOnUiThread(() -> {
                                // Load the original image
                                Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.success);

                                // Scale the image to the desired size
                                Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 32, 32, true);

                                // Create a Drawable from the scaled Bitmap
                                Drawable successIcon = new BitmapDrawable(getResources(), scaledBitmap);

                                final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity3.this).setTitle("Success").setMessage(responseBody).setPositiveButton("OK", (dialog, which) -> {
                                    dialog.dismiss();
                                    clearUIComponents();
                                }).setNeutralButton("Copy", (dialog, which) -> {
                                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("Response", responseBody);
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(MainActivity3.this, "Response copied to clipboard", Toast.LENGTH_SHORT).show();
                                    clearUIComponents();
                                }).setIcon(successIcon).create();
                                alertDialog.setOnDismissListener(dialog -> {
                                    dialog.dismiss();
                                    clearUIComponents();
                                });
                                alertDialog.show();
                            });
                        }
                    } else {
                        runOnUiThread(() -> showAlert("Response Error", "Empty response from server"));
                    }
                } else {
                    runOnUiThread(() -> showAlert("Server Error", "Server error: " + response.code()));
                }
            }
        });
    }

    private void showAlert(String title, String message) {
        // Load the original image
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.declined);

        // Scale the image to the desired size
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 32, 32, true);

        // Create a Drawable from the scaled Bitmap
        Drawable alertIcon = new BitmapDrawable(getResources(), scaledBitmap);

        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
            finish();
        }).setIcon(alertIcon).setCancelable(false).show();
    }

    private void showWarning(String title, String message) {
        // Load the original image
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.caution);

        // Scale the image to the desired size
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 32, 32, true);

        // Create a Drawable from the scaled Bitmap
        Drawable warningIcon = new BitmapDrawable(getResources(), scaledBitmap);

        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).setIcon(warningIcon).setCancelable(false).show();
    }
}