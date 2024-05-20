package com.vtc3pl.prnapp2024v2;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity7 extends AppCompatActivity {

    final double[] totalBoxWeight = {0};
    final double[] totalBoxQty = {0};
    final double[] totalBagWeight = {0};
    final double[] totalBagQty = {0};
    private final Set<String> lrNumbersSet = new HashSet<>();
    private String prnId = "", depo = "", username = "", response = "";
    private String[] lrnoArray;
    private Spinner hamaliVendorNameSpinnerActivitySeven, hamaliTypeSpinnerActivitySeven;
    private EditText hamaliAmountEditTextActivitySeven, deductionAmountEditTextActivitySeven, amountPaidToHVendorEditTextActivitySeven, freightEditText;
    private RadioGroup radioGroupOptions;
    private RadioButton radioButtonUnLoading, radioButtonWithoutUnLoading;
    private String selectedHamaliVendor = "", selectedHamaliType = "";
    private double amountPaidToHVendor, deductionAmount;
    private TableLayout tableLayoutActivitySeven;

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

        Log.e("lrnoArray from Avt 7 :", Arrays.toString(lrnoArray));

        Log.d("response:", response);

        if (prnId != null) {
            Log.d("PRN ID MAIN ACTIVITY6", prnId);
            Log.d("depo", depo);
            Log.d("username", username);

            Toast.makeText(this, "PRN ID: " + prnId, Toast.LENGTH_LONG).show();
        }

        // Insert "lrnoArray" into lrNumbersSet
        if (lrnoArray != null) {
            lrNumbersSet.addAll(Arrays.asList(lrnoArray));
            Log.d("lrNumbersSet", lrNumbersSet.toString());
        }

        hamaliVendorNameSpinnerActivitySeven = findViewById(R.id.hamaliVendorNameSpinnerActivitySeven);
        hamaliTypeSpinnerActivitySeven = findViewById(R.id.hamaliTypeSpinnerActivitySeven);
        tableLayoutActivitySeven = findViewById(R.id.tableLayoutActivitySeven);

        hamaliAmountEditTextActivitySeven = findViewById(R.id.hamaliAmountEditTextActivitySeven);
        deductionAmountEditTextActivitySeven = findViewById(R.id.deductionAmountEditTextActivitySeven);
        amountPaidToHVendorEditTextActivitySeven = findViewById(R.id.amountPaidToHVendorEditTextActivitySeven);
        freightEditText = findViewById(R.id.freightEditText);

        fetchHvendors();

        hamaliVendorNameSpinnerActivitySeven.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedVendor = parent.getItemAtPosition(position).toString();
                if (selectedVendor.equals("No hamali Vendor")) {
                    hamaliAmountEditTextActivitySeven.setText("0.0");
                    hamaliAmountEditTextActivitySeven.setEnabled(false);

                    deductionAmountEditTextActivitySeven.setText("0.0");
                    deductionAmountEditTextActivitySeven.setEnabled(false);

                    amountPaidToHVendorEditTextActivitySeven.setText("0.0");
                    amountPaidToHVendorEditTextActivitySeven.setEnabled(false);
                } else {
                    fetchWeightsFromServer();
                    // If user select any pother value then calculate,
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
        String url = "https://vtc3pl.com/hamali_bag_box_weight_prn_app.php";

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
                        // Parse the JSON response
                        try {
                            JSONArray jsonArray = new JSONArray(responseBody);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String lrNumber = jsonObject.getString("LRNO");
                                if (lrNumbersSet.contains(lrNumber)) {
                                    totalBoxWeight[0] += jsonObject.getDouble("TotalWeightBox");
                                    totalBagWeight[0] += jsonObject.getDouble("TotalWeightBag");
                                    totalBoxQty[0] += jsonObject.getDouble("TotalBoxQty");
                                    totalBagQty[0] += jsonObject.getDouble("TotalBagQty");
                                }
                            }

                            // Update the UI on the main thread
                            runOnUiThread(() -> {
//                                totalBoxQtyEditText.setText(String.valueOf(totalBoxQty[0]));
//                                totalBagWeightEditText.setText(String.valueOf(totalBagWeight[0]));

                                Log.d("totalBoxWeight : ", String.valueOf(totalBoxWeight[0]));
                                Log.d("totalBagWeight : ", String.valueOf(totalBagWeight[0]));

                                Log.d("totalBoxQty : ", String.valueOf(totalBoxQty[0]));
                                Log.d("totalBagQty : ", String.valueOf(totalBagQty[0]));
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

        OkHttpClient client = new OkHttpClient();

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
                            double hamaliBoxValue = boxRate * totalBoxQty[0];
                            Log.d("hamaliBoxValue : ", String.valueOf(hamaliBoxValue));
                            double ratePerTon = bagRate;
                            double weightInTons = totalBagWeight[0] / 1000;
                            Log.d("weightInTons : ", String.valueOf(weightInTons));
                            double hamaliBagValue = weightInTons * ratePerTon;
                            Log.d("hamaliBagValue : ", String.valueOf(hamaliBagValue));
                            double totalHamaliAmount = hamaliBoxValue + hamaliBagValue;
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

        TextView lrNOHeader = createHeaderTextView("LRNO");
        headerRow.addView(lrNOHeader);

        TextView lrDateHeader = createHeaderTextView("LRDate");
        headerRow.addView(lrDateHeader);

        TextView toPlaceHeader = createHeaderTextView("ToPlace");
        headerRow.addView(toPlaceHeader);

        TextView qtyHeader = createHeaderTextView("Qty");
        headerRow.addView(qtyHeader);

        TextView recievedQtyHeader = createHeaderTextView("Recieved Qty");
        headerRow.addView(recievedQtyHeader);

        TextView reasonHeader = createHeaderTextView("Reason");
        headerRow.addView(reasonHeader);

        tableLayoutActivitySeven.addView(headerRow);

        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                // Create a new row for each item in the JSON array
                TableRow row = new TableRow(MainActivity7.this);
                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                row.setLayoutParams(layoutParams);

                // Create TextViews for each column
                TextView lrNoTextView = createTextView(jsonObject.getString("LRNO"));
                TextView lrDateTextView = createTextView(jsonObject.getString("LRDate"));
                TextView toPlaceTextView = createTextView(jsonObject.getString("ToPlace"));
                TextView pkgsNoTextView = createTextView(jsonObject.getString("PkgsNo"));
                //TextView receivedQtyTextView = createTextView(jsonObject.getString("RecievedQty"));
                //TextView reasonTextView = createTextView(jsonObject.getString("Reason"));

                EditText receivedQtyEditText = new EditText(MainActivity7.this);
                receivedQtyEditText.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                receivedQtyEditText.setText(jsonObject.getString("RecievedQty"));

                // Create an EditText for reason column
                EditText reasonEditText = new EditText(MainActivity7.this);
                reasonEditText.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                reasonEditText.setText(jsonObject.getString("Reason"));

                // Add TextViews to the row
                row.addView(lrNoTextView);
                row.addView(lrDateTextView);
                row.addView(toPlaceTextView);
                row.addView(pkgsNoTextView);
                //row.addView(receivedQtyTextView);
                //row.addView(reasonTextView);
                row.addView(receivedQtyEditText);
                row.addView(reasonEditText);


                // Add the row to the TableLayout
                tableLayoutActivitySeven.addView(row);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private TextView createHeaderTextView(String text) {
        TextView textView = new TextView(MainActivity7.this);
        textView.setText(text);
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

}