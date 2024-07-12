package com.vtc3pl.prnapp2024v2;
//Arrival Page Part 2

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity7 extends AppCompatActivity {
    private final Set<String> lrNumbersSet = new HashSet<>();
    private double totalBoxWeightFromAllLRNO = 0, totalBoxQtyFromAllLRNO = 0, totalBagWeightFromAllLRNO = 0, totalBagQtyFromAllLRNO = 0;
    private String selectedRadioButton = "", prnId = "", depo = "", username = "", response = "", year = "";
    private String[] lrnoArray;
    private Spinner hamaliVendorNameSpinnerActivitySeven, hamaliTypeSpinnerActivitySeven;
    private EditText hamaliAmountEditTextActivitySeven, deductionAmountEditTextActivitySeven, amountPaidToHVendorEditTextActivitySeven, freightEditText, godownKeeperNameEditText;
    private RadioGroup radioGroupOptions;
    private RadioButton radioButtonUnLoading, radioButtonWithoutUnLoading;
    private String selectedHamaliVendor = "", selectedHamaliType = "";
    private double amountPaidToHVendor, deductionAmount;
    private TableLayout tableLayoutActivitySeven;

    private double previousBoxReceivedWeight = 0;
    private double previousBoxReceivedQty = 0;
    private double previousBagReceivedWeight = 0;
    private double previousBagReceivedQty = 0;

    private int previousReceivedQty = 0;

    private LottieAnimationView lottieAnimationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main7);

        // Get the prnId from the intent extras
        prnId = getIntent().getStringExtra("prnId");
        depo = getIntent().getStringExtra("depo");
        username = getIntent().getStringExtra("username");
        response = getIntent().getStringExtra("response");
        lrnoArray = getIntent().getStringArrayExtra("lrnoArray");
        year = getIntent().getStringExtra("year");

        if (prnId != null) {
            Toast.makeText(this, "PRN ID: " + prnId, Toast.LENGTH_LONG).show();
        }

        // Insert "lrnoArray" into lrNumbersSet
        if (lrnoArray != null) {
            lrNumbersSet.addAll(Arrays.asList(lrnoArray));
        }

        hamaliVendorNameSpinnerActivitySeven = findViewById(R.id.hamaliVendorNameSpinnerActivitySeven);
        hamaliTypeSpinnerActivitySeven = findViewById(R.id.hamaliTypeSpinnerActivitySeven);
        tableLayoutActivitySeven = findViewById(R.id.tableLayoutActivitySeven);

        hamaliAmountEditTextActivitySeven = findViewById(R.id.hamaliAmountEditTextActivitySeven);
        hamaliAmountEditTextActivitySeven.setEnabled(false);

        deductionAmountEditTextActivitySeven = findViewById(R.id.deductionAmountEditTextActivitySeven);

        amountPaidToHVendorEditTextActivitySeven = findViewById(R.id.amountPaidToHVendorEditTextActivitySeven);
        amountPaidToHVendorEditTextActivitySeven.setEnabled(false);

        freightEditText = findViewById(R.id.freightEditText);
        freightEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

        godownKeeperNameEditText = findViewById(R.id.godownKeeperNameEditText);
        godownKeeperNameEditText.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        lottieAnimationView = findViewById(R.id.lottieAnimationView);

        fetchHvendors();
        Log.d("onCreate fetch Weight", "fetchWeightsFromServer() called after fetchvendor");
        fetchWeightsFromServer();

        hamaliVendorNameSpinnerActivitySeven.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedVendor = parent.getItemAtPosition(position).toString();
                if (selectedVendor.equals("No hamali Vendor")) {
                    hamaliAmountEditTextActivitySeven.setText("0.0");
                    hamaliAmountEditTextActivitySeven.setEnabled(false);

                    deductionAmountEditTextActivitySeven.setText("0.0");

                    amountPaidToHVendorEditTextActivitySeven.setText("0.0");
                    amountPaidToHVendorEditTextActivitySeven.setEnabled(false);
                } else {
                    fetchWeightsFromServer();
                    // If user select any another value then calculate,
                    calculateHamali();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing if nothing is selected
            }
        });

        hamaliTypeSpinnerActivitySeven.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calculateHamali();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing if nothing is selected
            }
        });

        displayDataInTable(response);

        Button submitButtonArrivalPRN = findViewById(R.id.submitButtonArrivalPRN);
        submitButtonArrivalPRN.setOnClickListener(v -> {
            lottieAnimationView.setVisibility(View.VISIBLE);
            lottieAnimationView.playAnimation();

            submitDataToServer();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void fetchHvendors() {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        // URL for fetching Hvendors
        String url = "https://vtc3pl.com/fetch_hamalivendor_only_prn_app.php";

        // Create a form body with spinnerDepo as a parameter
        FormBody formBody = new FormBody.Builder().add("spinnerDepo", depo).build();

        Request request = new Request.Builder().url(url).post(formBody) // Use POST method and set the form body
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        String responseBody = body.string();
                        // Parse the JSON response
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
                                runOnUiThread(() -> {
                                    Toast.makeText(MainActivity7.this, "No hamali vendors found", Toast.LENGTH_SHORT).show();
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Update the spinner UI on the main thread
                        runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity7.this, android.R.layout.simple_spinner_item, hVendors);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            hamaliVendorNameSpinnerActivitySeven.setAdapter(adapter);
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity7.this, "Response body is null (fetch vendors)", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    onFailure(call, new IOException("Unexpected response code " + response));
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity7.this, "Failed to fetch Hamali Vendors", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void fetchWeightsFromServer() {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        // URL for fetching weights
        String url = "https://vtc3pl.com/hamali_bag_box_weight_prn_app_arrival.php";

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity7.this, "Failed to fetch weights from server", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        String responseBody = body.string();
                        Log.e("Response : ", responseBody);
                        // Parse the JSON response
                        try {
                            JSONArray jsonArray = new JSONArray(responseBody);
                            double totalBoxWeight = 0, totalBoxQty = 0, totalBagWeight = 0, totalBagQty = 0;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String lrNumber = jsonObject.getString("LRNO").trim();
                                if (lrNumbersSet.contains(lrNumber)) {
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

                            runOnUiThread(() -> {
                                Log.d("totalBoxWeight : ", String.valueOf(totalBoxWeightFromAllLRNO));
                                Log.d("totalBagWeight : ", String.valueOf(totalBagWeightFromAllLRNO));
                                Log.d("totalBoxQty : ", String.valueOf(totalBoxQtyFromAllLRNO));
                                Log.d("totalBagQty : ", String.valueOf(totalBagQtyFromAllLRNO));
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity7.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity7.this, "Response body is null(Box Qty and Bag Weight)", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    onFailure(call, new IOException("Unexpected response code " + response));
                }
            }
        });
    }

    private void calculateHamali() {
        Log.d("calculateHamali() :", "Method is invoked");

        if (hamaliVendorNameSpinnerActivitySeven.getSelectedItem() == null || hamaliTypeSpinnerActivitySeven.getSelectedItem() == null) {
            // One or both spinners are not selected, return without calculating hamali
            return;
        }

        selectedHamaliVendor = hamaliVendorNameSpinnerActivitySeven.getSelectedItem().toString();

        selectedHamaliType = hamaliTypeSpinnerActivitySeven.getSelectedItem().toString();

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("spinnerDepo", depo);
        formBuilder.add("Hvendor", selectedHamaliVendor);

        Request request = new Request.Builder()
                .url("https://vtc3pl.com/fetch_hamali_rates_calculation_prn_app.php")
                .post(formBuilder.build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity7.this, "Failed to fetch hamali rates", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
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
                                runOnUiThread(() -> {
                                    Toast.makeText(MainActivity7.this, "Unknown hamali type", Toast.LENGTH_SHORT).show();
                                });
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
                                hamaliAmountEditTextActivitySeven.setText(String.valueOf(totalHamaliAmount));
                                hamaliAmountEditTextActivitySeven.setEnabled(false);
                            });

                            // Assuming you have deductionAmountEditText and amountPaidToHVendorEditText declared and initialized
                            // Assuming these variables are declared globally
                            deductionAmountEditTextActivitySeven.setOnKeyListener((v, keyCode, event) -> {
                                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                                    deductionAmount = Double.parseDouble(deductionAmountEditTextActivitySeven.getText().toString());

                                    if (deductionAmount < 0) {
                                        // Prevent deduction amount from being less than zero
                                        Toast.makeText(MainActivity7.this, "Deduction amount cannot be less than zero", Toast.LENGTH_SHORT).show();
                                        deductionAmountEditTextActivitySeven.setText("0.0");
                                        return true;
                                    }
                                    amountPaidToHVendor = (totalHamaliAmount - deductionAmount);
                                    Log.d("amountPaidToHVendor : ", String.valueOf(amountPaidToHVendor));
                                    amountPaidToHVendorEditTextActivitySeven.setText(String.valueOf(amountPaidToHVendor));
                                    amountPaidToHVendorEditTextActivitySeven.setEnabled(false);
                                    return true;
                                }
                                return false;
                            });

                            deductionAmountEditTextActivitySeven.setOnFocusChangeListener((v, hasFocus) -> {
                                if (!hasFocus) {
                                    // Calculate amount paid to vendor when deduction amount is entered
                                    String deductionAmountStr = deductionAmountEditTextActivitySeven.getText().toString().trim();
                                    deductionAmount = deductionAmountStr.isEmpty() ? 0.0 : Double.parseDouble(deductionAmountStr);
                                    amountPaidToHVendor = Double.parseDouble(hamaliAmountEditTextActivitySeven.getText().toString()) - deductionAmount;
                                    amountPaidToHVendorEditTextActivitySeven.setText(String.valueOf(amountPaidToHVendor));
                                    amountPaidToHVendorEditTextActivitySeven.setEnabled(false);
                                }
                            });

                            // Set up hamaliAmountEditText listener
                            hamaliAmountEditTextActivitySeven.addTextChangedListener(new TextWatcher() {
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
                                    String deductionAmountStr = deductionAmountEditTextActivitySeven.getText().toString().trim();
                                    deductionAmount = deductionAmountStr.isEmpty() ? 0.0 : Double.parseDouble(deductionAmountStr);
                                    amountPaidToHVendor = (hamaliAmount - deductionAmount);
                                    amountPaidToHVendorEditTextActivitySeven.setText(String.valueOf(amountPaidToHVendor));
                                    amountPaidToHVendorEditTextActivitySeven.setEnabled(false);
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                // Handle JSON parsing error
                                Toast.makeText(MainActivity7.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity7.this, "Response body is empty (Hamali rates)", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        // Handle server error
                        Toast.makeText(MainActivity7.this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void displayDataInTable(String response) {
        TableRow headerRow = new TableRow(MainActivity7.this);
        headerRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView selectHeader = createHeaderTextView("Select");
        headerRow.addView(selectHeader);

        TextView lrNOHeader = createHeaderTextView("LRNO");
        headerRow.addView(lrNOHeader);

        TextView lrDateHeader = createHeaderTextView("LRDate");
        headerRow.addView(lrDateHeader);

        TextView toPlaceHeader = createHeaderTextView("ToPlace");
        headerRow.addView(toPlaceHeader);

        TextView qtyHeader = createHeaderTextView("Qty");
        headerRow.addView(qtyHeader);

        TextView recievedQtyHeader = createHeaderTextView("Received Qty");
        headerRow.addView(recievedQtyHeader);

        TextView differentQtyHeader = createHeaderTextView("Difference Qty");
        headerRow.addView(differentQtyHeader);

        TextView reasonHeader = createHeaderTextView("Reason");
        headerRow.addView(reasonHeader);

        tableLayoutActivitySeven.addView(headerRow);

        try {
            JSONArray jsonArray = new JSONArray(response);
            Log.e("JSON Array: ", String.valueOf(jsonArray));
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                // Create a new row for each item in the JSON array
                TableRow row = new TableRow(MainActivity7.this);
                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                row.setLayoutParams(layoutParams);

                // Create a CheckBox for the row
                CheckBox rowCheckBox = new CheckBox(MainActivity7.this);
                rowCheckBox.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

                // Create TextViews for each column
                TextView lrNoTextView = createTextView(jsonObject.getString("LRNO"));
                TextView lrDateTextView = createTextView(jsonObject.getString("LRDate"));
                TextView toPlaceTextView = createTextView(jsonObject.getString("ToPlace"));
                TextView pkgsNoTextView = createTextView(jsonObject.getString("PkgsNo"));

                EditText receivedQtyEditText = new EditText(MainActivity7.this);
                receivedQtyEditText.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
//                receivedQtyEditText.setText(jsonObject.getString("RecievedQty"));
                receivedQtyEditText.setText(jsonObject.getString("PkgsNo"));
                receivedQtyEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                receivedQtyEditText.setEnabled(false);

                EditText differentQtyEditText = new EditText(MainActivity7.this);
                differentQtyEditText.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                differentQtyEditText.setEnabled(false);
                differentQtyEditText.setText("0");

                // Create an EditText for reason column
                EditText reasonEditText = new EditText(MainActivity7.this);
                reasonEditText.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
//                reasonEditText.setText(jsonObject.getString("Reason"));
                reasonEditText.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
                reasonEditText.setText("OK");
                reasonEditText.setEnabled(false);

                // Add a listener to the CheckBox to enable/disable EditTexts
                rowCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    receivedQtyEditText.setEnabled(isChecked);
                    if (!isChecked) {
                        reasonEditText.setText("OK");
                    } else {
                        reasonEditText.setText("");
                    }
                    reasonEditText.setEnabled(isChecked);

                    // Log message when checkbox is selected
                    if (isChecked) {
                        try {
                            JSONObject logJson = new JSONObject();
                            logJson.put("LRNO", lrNoTextView.getText().toString());
                            logJson.put("Received Qty", receivedQtyEditText.getText().toString());
                            Log.d("SelectedItem", logJson.toString());
                            sendJsonToServer(logJson, row, receivedQtyEditText); // Send JSON to server with the current row and previous EditText
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                // Add a TextWatcher to receivedQtyEditText to update differentQtyEditText
                receivedQtyEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        try {
                            int pkgsNo = Integer.parseInt(pkgsNoTextView.getText().toString());
                            int receivedQty = Integer.parseInt(s.toString());
                            int differenceQty = (pkgsNo - receivedQty);
                            differentQtyEditText.setText(String.valueOf(differenceQty));

                            JSONObject updateJson = new JSONObject();
                            updateJson.put("LRNO", lrNoTextView.getText().toString());
                            updateJson.put("ReceivedQty", s.toString());
                            sendUpdatedValueToServer(updateJson);
                        } catch (NumberFormatException e) {
                            differentQtyEditText.setText("0");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                // Add the CheckBox and TextViews to the row
                row.addView(rowCheckBox);
                // Add TextViews to the row
                row.addView(lrNoTextView);
                row.addView(lrDateTextView);
                row.addView(toPlaceTextView);
                row.addView(pkgsNoTextView);
                row.addView(receivedQtyEditText);
                row.addView(differentQtyEditText);
                row.addView(reasonEditText);

                tableLayoutActivitySeven.addView(row);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendJsonToServer(JSONObject json, TableRow row, EditText previousReceivedQtyEditText) {

        // Show the Lottie animation
        lottieAnimationView.setVisibility(View.VISIBLE);
        lottieAnimationView.playAnimation();

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://vtc3pl.com/fetch_lrno_details_prn_arrival_prn_app.php")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lottieAnimationView.setVisibility(View.GONE);
                        lottieAnimationView.cancelAnimation();
                    }
                });

                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("ServerResponse", responseBody);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lottieAnimationView.setVisibility(View.GONE);
                            lottieAnimationView.cancelAnimation();
                            try {
                                JSONObject responseJson = new JSONObject(responseBody);
                                Log.d("Inside server() ", "OK");
                                Log.d("checkAndAddEditText ", "Calling method checkAndAddEditText() ");
                                checkAndAddEditText(responseJson, row, previousReceivedQtyEditText);
                            } catch (JSONException e) {
                                lottieAnimationView.setVisibility(View.GONE);
                                lottieAnimationView.cancelAnimation();
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lottieAnimationView.setVisibility(View.GONE);
                            lottieAnimationView.cancelAnimation();
                            Log.e("ServerResponse", "Request failed: " + response.code());
                        }
                    });
                }
            }
        });
    }

    private void checkAndAddEditText(JSONObject responseJson, TableRow row, EditText previousReceivedQtyEditText) {
        try {
//            Log.d("LRNO: " , "LRNO IN checkAndAddEditText : " + responseJson);
            JSONObject bags = responseJson.getJSONObject("BAGS");
            JSONObject box = responseJson.getJSONObject("BOX");

            int bagsReceivedQty = bags.getInt("receivedQty");
            int boxReceivedQty = box.getInt("receivedQty");
            int bagsReceivedWeight = bags.getInt("receivedWeight");
            int boxReceivedWeight = box.getInt("receivedWeight");

            // Get LRNO from responseJson
            String lrNo = responseJson.getString("LRNO");

            if (bagsReceivedQty > 0 && boxReceivedQty > 0) {

                //responseJson = {"LRNO":"PNA0000925705","BAGS":{"receivedWeight":74,"receivedQty":17},"BOX":{"receivedWeight":225,"receivedQty":25}}
                //Here we have to subtract values from variables from response values

                // Subtract values from global variables
                totalBagWeightFromAllLRNO -= bags.getInt("receivedWeight");
                totalBagQtyFromAllLRNO -= bagsReceivedQty;

                totalBoxWeightFromAllLRNO -= box.getInt("receivedWeight");
                totalBoxQtyFromAllLRNO -= boxReceivedQty;

                // Log the updated global variables
                Log.d("Global Variables", "totalBagWeightFromAllLRNO: " + totalBagWeightFromAllLRNO);
                Log.d("Global Variables", "totalBagQtyFromAllLRNO: " + totalBagQtyFromAllLRNO);
                Log.d("Global Variables", "totalBoxWeightFromAllLRNO: " + totalBoxWeightFromAllLRNO);
                Log.d("Global Variables", "totalBoxQtyFromAllLRNO: " + totalBoxQtyFromAllLRNO);

                // Hide the previous EditText
                previousReceivedQtyEditText.setVisibility(View.GONE);

                LinearLayout receivedQtyLayout = new LinearLayout(MainActivity7.this);
                receivedQtyLayout.setOrientation(LinearLayout.HORIZONTAL);
                receivedQtyLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

                TextView bagsLabel = new TextView(MainActivity7.this);
                bagsLabel.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                bagsLabel.setText("BAGS:");

                EditText bagsReceivedQtyEditText = new EditText(MainActivity7.this);
                bagsReceivedQtyEditText.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                bagsReceivedQtyEditText.setText(String.valueOf(bagsReceivedQty));
                bagsReceivedQtyEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

                TextView boxLabel = new TextView(MainActivity7.this);
                boxLabel.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                boxLabel.setText("BOX:");

                EditText boxReceivedQtyEditText = new EditText(MainActivity7.this);
                boxReceivedQtyEditText.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                boxReceivedQtyEditText.setText(String.valueOf(boxReceivedQty));
                boxReceivedQtyEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

                bagsReceivedQtyEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        try {
                            JSONObject updateJson = new JSONObject();
                            updateJson.put("LRNO", lrNo);
                            updateJson.put("BAGS", s.toString());
                            updateJson.put("BOX", boxReceivedQtyEditText.getText().toString());
                            sendUpdatedValueToServer(updateJson);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                boxReceivedQtyEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        try {
                            JSONObject updateJson = new JSONObject();
                            updateJson.put("LRNO", lrNo);
                            updateJson.put("BOX", s.toString());
                            updateJson.put("BAGS", bagsReceivedQtyEditText.getText().toString());
                            sendUpdatedValueToServer(updateJson);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                receivedQtyLayout.addView(bagsLabel);
                receivedQtyLayout.addView(bagsReceivedQtyEditText);
                receivedQtyLayout.addView(boxLabel);
                receivedQtyLayout.addView(boxReceivedQtyEditText);

                // Add new EditTexts in place of the previous one
                int receivedQtyIndex = row.indexOfChild(previousReceivedQtyEditText);
                if (receivedQtyIndex != -1) {
                    row.removeViewAt(receivedQtyIndex);
                    row.addView(receivedQtyLayout, receivedQtyIndex);
                }
            } else if (bagsReceivedQty > 0 && boxReceivedQty == 0) {
                // Subtract values from global variables
                totalBagWeightFromAllLRNO -= bagsReceivedWeight;
                totalBagQtyFromAllLRNO -= bagsReceivedQty;

                // Log the updated global variables
                Log.d("Global Variables", "totalBagWeightFromAllLRNO: " + totalBagWeightFromAllLRNO);
                Log.d("Global Variables", "totalBagQtyFromAllLRNO: " + totalBagQtyFromAllLRNO);

                // Add TextWatcher to the existing previousReceivedQtyEditText
                previousReceivedQtyEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        try {
                            JSONObject updateJson = new JSONObject();
                            updateJson.put("LRNO", lrNo);
                            updateJson.put("BAGS", s.toString());
                            sendUpdatedValueToServer(updateJson);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else if (bagsReceivedQty == 0 && boxReceivedQty > 0) {
                // Subtract values from global variables
                totalBoxWeightFromAllLRNO -= boxReceivedWeight;
                totalBoxQtyFromAllLRNO -= boxReceivedQty;

                // Log the updated global variables
                Log.d("Global Variables", "totalBoxWeightFromAllLRNO: " + totalBoxWeightFromAllLRNO);
                Log.d("Global Variables", "totalBoxQtyFromAllLRNO: " + totalBoxQtyFromAllLRNO);

                // Add TextWatcher to the existing previousReceivedQtyEditText
                previousReceivedQtyEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        try {
                            JSONObject updateJson = new JSONObject();
                            updateJson.put("LRNO", lrNo);
                            updateJson.put("BOX", s.toString());
                            sendUpdatedValueToServer(updateJson);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendUpdatedValueToServer(JSONObject json) {
        Log.d("JSON RESPONSE : ", "sendUpdatedValueToServer JSON response after change value in BAGS OR BOX Value: " + json);

        lottieAnimationView.setVisibility(View.VISIBLE);
        lottieAnimationView.playAnimation();

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://vtc3pl.com/fetch_lrno_details_for_single_lrno_prn_arrival_prn_app.php")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lottieAnimationView.setVisibility(View.GONE);
                        lottieAnimationView.cancelAnimation();
                    }
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("ServerResponse", responseBody);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lottieAnimationView.setVisibility(View.GONE);
                            lottieAnimationView.cancelAnimation();


                            try {
                                JSONObject responseJson = new JSONObject(responseBody);

                                // Extract BAGS and BOX data
                                JSONObject bags = responseJson.getJSONObject("BAGS");
                                JSONObject box = responseJson.getJSONObject("BOX");

                                int bagsReceivedWeight = bags.getInt("receivedWeight");
                                int bagsReceivedQty = bags.getInt("receivedQty");

                                int boxReceivedWeight = box.getInt("receivedWeight");
                                int boxReceivedQty = box.getInt("receivedQty");

                                int receivedQty = responseJson.optInt("ReceivedQty", 0);

                                // Subtract the previous values
                                totalBagWeightFromAllLRNO -= previousBagReceivedWeight;
                                totalBagQtyFromAllLRNO -= previousBagReceivedQty;

                                totalBoxWeightFromAllLRNO -= previousBoxReceivedWeight;
                                totalBoxQtyFromAllLRNO -= previousBoxReceivedQty;

                                totalBoxQtyFromAllLRNO -= previousReceivedQty;

                                // Add the new values
                                totalBagWeightFromAllLRNO += bagsReceivedWeight;
                                totalBagQtyFromAllLRNO += bagsReceivedQty;

                                totalBoxWeightFromAllLRNO += boxReceivedWeight;
                                totalBoxQtyFromAllLRNO += boxReceivedQty;

                                totalBoxQtyFromAllLRNO += receivedQty;

                                // Update the previous values
                                previousBagReceivedWeight = bagsReceivedWeight;
                                previousBagReceivedQty = bagsReceivedQty;
                                previousBoxReceivedWeight = boxReceivedWeight;
                                previousBoxReceivedQty = boxReceivedQty;
                                previousReceivedQty = receivedQty;

                                // Log the updated global variables
                                Log.d("Global Variables", "totalBagWeightFromAllLRNO: " + totalBagWeightFromAllLRNO);
                                Log.d("Global Variables", "totalBagQtyFromAllLRNO: " + totalBagQtyFromAllLRNO);
                                Log.d("Global Variables", "totalBoxWeightFromAllLRNO: " + totalBoxWeightFromAllLRNO);
                                Log.d("Global Variables", "totalBoxQtyFromAllLRNO: " + totalBoxQtyFromAllLRNO);

                            } catch (JSONException e) {
                                lottieAnimationView.setVisibility(View.GONE);
                                lottieAnimationView.cancelAnimation();
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lottieAnimationView.setVisibility(View.GONE);
                            lottieAnimationView.cancelAnimation();

                            Log.e("ServerResponse", "Request failed: " + response.code());
                        }
                    });

                }
            }
        });
    }

//    private int getIndexOfReceivedQty(TableRow row) {
//        for (int i = 0; i < row.getChildCount(); i++) {
//            View view = row.getChildAt(i);
//            if (view instanceof EditText && ((EditText) view).getHint() != null && ((EditText) view).getHint().toString().equals("Received Qty")) {
//                return i;
//            }
//        }
//        return -1;
//    }

    private TextView createHeaderTextView(String text) {
        TextView textView = new TextView(MainActivity7.this);
        textView.setText(text);
        textView.setTypeface(null, Typeface.BOLD); // Set text to bold
        textView.setPadding(10, 10, 10, 10); // Padding
        return textView;
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(MainActivity7.this);
        textView.setText(text);
        textView.setPadding(10, 10, 10, 10);
        textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        return textView;
    }

    private void submitDataToServer() {

        // Initialize a list to store the data from each row
        List<JSONObject> rowDataList = new ArrayList<>();

        // Iterate through each row in the TableLayout
        for (int i = 1; i < tableLayoutActivitySeven.getChildCount(); i++) { // Start from 1 to skip header row
            TableRow row = (TableRow) tableLayoutActivitySeven.getChildAt(i);

            // Initialize variables to store data for each row
            String lrNo = "";
            String lrDate = "";
            String toPlace = "";
            String pkgsNo = "";
            String receivedQty = "";
            String differentQty = "";
            String reason = "";

            // Iterate through each cell in the row
            for (int j = 0; j < row.getChildCount(); j++) {
                View view = row.getChildAt(j);
                if (view instanceof TextView) {
                    // Extract data from TextViews
                    TextView textView = (TextView) view;
                    String text = textView.getText().toString().trim();
                    Log.d("Extracting TextView", "Index: " + j + " Text: " + text);
                    switch (j) {
                        case 1: // lrNoTextView
                            lrNo = text;
                            break;
                        case 2: // lrDateTextView
                            lrDate = text;
                            break;
                        case 3: // toPlaceTextView
                            toPlace = text;
                            break;
                        case 4: // pkgsNoTextView
                            pkgsNo = text;
                            break;
                        case 5: // receivedQtyTextView
                            receivedQty = text;
                            break;
                        case 6: // differentQtyTextView
                            differentQty = text;
                            break;
                        case 7: // reasonTextView
                            reason = text;
                            break;
                    }
                }
            }

            // Validate data before storing
            if (!lrNo.isEmpty() && !lrDate.isEmpty() && !toPlace.isEmpty() && !pkgsNo.isEmpty() && !receivedQty.isEmpty() && !differentQty.isEmpty() && !reason.isEmpty()) {
                // Create a JSON object to store row data
                JSONObject rowData = new JSONObject();
                try {
                    rowData.put("LRNO", lrNo);
                    rowData.put("LRDate", lrDate);
                    rowData.put("ToPlace", toPlace);
                    rowData.put("PkgsNo", pkgsNo);
                    rowData.put("ReceivedQty", receivedQty);
                    rowData.put("DifferentQty", differentQty);
                    rowData.put("Reason", reason);

                    // Add the JSON object to the list
                    rowDataList.add(rowData);
                } catch (JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lottieAnimationView.setVisibility(View.GONE);
                            lottieAnimationView.cancelAnimation();
                        }
                    });
                    e.printStackTrace();
                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lottieAnimationView.setVisibility(View.GONE);
                        lottieAnimationView.cancelAnimation();
                    }
                });
                showWarning("Empty Field Warning", "One or more fields are empty for LRNO: " + lrNo);
                return;
            }
        }

        //print the table data in list :
        Log.d("TablLayoutData : ", rowDataList.toString());

        Object selectedItem = hamaliVendorNameSpinnerActivitySeven.getSelectedItem();
        if (selectedItem == null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lottieAnimationView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();

                    showWarning("Unselected Field Warning", "Please select hamali vendor name.");
                }
            });
            return;
        }

        String hamaliVendor = selectedItem.toString().trim();
        Log.d("hamaliVendor", hamaliVendor);
        String hamaliType = hamaliTypeSpinnerActivitySeven.getSelectedItem().toString().trim();
        Log.d("hamaliType", hamaliType);
        String deductionAmount = deductionAmountEditTextActivitySeven.getText().toString().trim();
        Log.d("deductionAmount", deductionAmount);
        String hamaliAmount = hamaliAmountEditTextActivitySeven.getText().toString().trim();
        Log.d("hamaliAmount", hamaliAmount);
        String amountPaidToHVendor = amountPaidToHVendorEditTextActivitySeven.getText().toString().trim();
        Log.d("amountPaidToHVendor", amountPaidToHVendor);
        String freightAmount = freightEditText.getText().toString().trim();
        Log.d("freightAmount", freightAmount);
        String godownKeeperName = godownKeeperNameEditText.getText().toString().trim();
        Log.d("godownKeeperName", godownKeeperName);

        radioGroupOptions = findViewById(R.id.radioGroupOptions);
        radioButtonWithoutUnLoading = findViewById(R.id.radioButtonWithoutUnLoading);
        radioButtonUnLoading = findViewById(R.id.radioButtonUnLoading);

        radioGroupOptions.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == radioButtonWithoutUnLoading.getId()) {
                selectedRadioButton = "WithoutUnLoading";
                Log.d("If RadioButton Value:", selectedRadioButton + " , checkId = " + checkedId);
            } else if (checkedId == radioButtonUnLoading.getId()) {
                selectedRadioButton = "UnLoading";
                Log.d("else RadioButton Value:", selectedRadioButton + " , checkId = " + checkedId);
            }
        });

        if (radioGroupOptions.getCheckedRadioButtonId() == -1) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lottieAnimationView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();

                    showWarning("Unselected Radio Button Warning", "Please select any one radio button.");
                }
            });
            return;
        }

        if (selectedRadioButton == null || selectedRadioButton.isEmpty()) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lottieAnimationView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();

                    showWarning("Unselected Radio Button Warning", "Please select any one radio button.");
                }
            });
            return;
        }

        if (freightAmount.isEmpty()) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lottieAnimationView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();

                    showWarning("Empty Field Warning", "Please enter freight amount.");
                    freightEditText.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(freightEditText, InputMethodManager.SHOW_IMPLICIT);

                }
            });
            return;
        }

        if (godownKeeperName.isEmpty()) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lottieAnimationView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();

                    showWarning("Empty Field Warning", "Please enter godown keeper name.");
                    godownKeeperNameEditText.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(godownKeeperNameEditText, InputMethodManager.SHOW_IMPLICIT);
                }
            });
            return;
        }

        if (hamaliVendor.equals("Please Select Vendor")) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lottieAnimationView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();

                    showWarning("Unselected Field Warning", "Please select hamali vendor name.");
                }
            });
            return;
        }

        if (amountPaidToHVendor.isEmpty() || hamaliAmount.isEmpty()) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lottieAnimationView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();

                    showWarning("Empty Field Warning", "Amount is empty.");
                }
            });
            return;
        }

        // Make HTTP request
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).build();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("UserName", username);
        formBuilder.add("spinnerDepo", depo);
        formBuilder.add("spinnerYear", year);
        formBuilder.add("freightAmount", freightAmount);
        formBuilder.add("godownKeeperName", godownKeeperName);
        formBuilder.add("selectedHamaliVendor", selectedHamaliVendor);
        formBuilder.add("finalHamliAmount", amountPaidToHVendor);
        formBuilder.add("selectedHamaliType", hamaliType);
        formBuilder.add("deductionAmount", deductionAmount);
        formBuilder.add("selectedRadioButton", selectedRadioButton);
        formBuilder.add("rowDataList", rowDataList.toString());
        formBuilder.add("PRN", prnId);

        Request request = new Request.Builder().url("https://vtc3pl.com/insert_arrival_data_prn_app.php").post(formBuilder.build()).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lottieAnimationView.setVisibility(View.GONE);
                        lottieAnimationView.cancelAnimation();
                        Log.e("MainActivity7(submit)", "Failed to connect to server", e);
                        showAlert("Connection Failed Error", "Failed to connect to server");
                    }
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        String responseBody = body.string();
                        Log.e("Response CreatePRN:", responseBody);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                lottieAnimationView.setVisibility(View.GONE);
                                lottieAnimationView.cancelAnimation();

                                Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.success);
                                Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 32, 32, true);
                                Drawable successIcon = new BitmapDrawable(getResources(), scaledBitmap);

                                final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity7.this)
                                        .setTitle("Success")
                                        .setMessage(responseBody)
                                        .setPositiveButton("OK", (dialog, which) -> {
                                            dialog.dismiss();
                                            clearUIComponents();
                                        }).setIcon(successIcon).create();
                                alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        dialog.dismiss();
                                        clearUIComponents();
                                    }
                                });

                                alertDialog.show();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                lottieAnimationView.setVisibility(View.GONE);
                                lottieAnimationView.cancelAnimation();

                                showAlert("Empty Response Error", "Empty response received from server");
                                Log.e("Response CreatePRN:", "Empty response body");
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lottieAnimationView.setVisibility(View.GONE);
                            lottieAnimationView.cancelAnimation();

                            showAlert("Server Error", "Server error: " + response.code());
                        }
                    });
                }
            }
        });
    }

    private void clearUIComponents() {
        radioGroupOptions.clearCheck();
        freightEditText.setText("");
        godownKeeperNameEditText.setText("");
        tableLayoutActivitySeven.removeAllViews();
        lrNumbersSet.clear();
        hamaliVendorNameSpinnerActivitySeven.setSelection(0);
        amountPaidToHVendorEditTextActivitySeven.setText("");
        hamaliTypeSpinnerActivitySeven.setSelection(0);
        deductionAmountEditTextActivitySeven.setText("");
        hamaliAmountEditTextActivitySeven.setText("");
        finish();
    }

    private void showAlert(String title, String message) {
        // Load the original image
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.declined);

        // Scale the image to the desired size
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 32, 32, true);

        // Create a Drawable from the scaled Bitmap
        Drawable alertIcon = new BitmapDrawable(getResources(), scaledBitmap);

        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).setIcon(alertIcon).show();
    }

    private void showWarning(String title, String message) {
        // Load the original image
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.caution);

        // Scale the image to the desired size
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 32, 32, true);

        // Create a Drawable from the scaled Bitmap
        Drawable warningIcon = new BitmapDrawable(getResources(), scaledBitmap);

        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).setIcon(warningIcon).show();
    }

}