package com.vtc3pl.prnapp2024v2;
//Auto PRN Creation page

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity2 extends AppCompatActivity {

    private static final Pattern LR_NUMBER_PATTERN = Pattern.compile("[A-Z]{3,4}[0-9]{10}+");
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private final Set<String> lrNumbersSet = new HashSet<>();
    private double totalBoxWeightFromAllLRNO = 0, totalBoxQtyFromAllLRNO = 0, totalBagWeightFromAllLRNO = 0, totalBagQtyFromAllLRNO = 0;
    private TableLayout tableLayout;
    private EditText lrEditText, vehicleNumberEditText, totalBoxQtyEditText, totalBagWeightEditText, deductionAmountEditText, hamaliAmountEditText, amountPaidToHVendorEditText;
    private SurfaceView cameraView;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private TextView showUserNameTextView;
    private Spinner goDownSpinner, hamaliVendorNameSpinner, hamaliTypeSpinner;
    private String username = "", depo = "", year = "";
    private char firstLetter = 'A'; //this is for CP
    private boolean isProcessing = false;
    private String selectedHamaliVendor = "";
    private String selectedHamaliType = "";
    private double amountPaidToHVendor, deductionAmount;
    private LottieAnimationView lottieAnimationView;
    private View blockingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);

        showUserNameTextView = findViewById(R.id.showUserNameTextView);
        tableLayout = findViewById(R.id.tableDisplay);

        lrEditText = findViewById(R.id.lrEditText);
        lrEditText.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        vehicleNumberEditText = findViewById(R.id.vehicleNumberEditText);
        vehicleNumberEditText.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        goDownSpinner = findViewById(R.id.goDownSpinner);
        hamaliVendorNameSpinner = findViewById(R.id.hamaliVendorNameSpinner);
        hamaliTypeSpinner = findViewById(R.id.hamaliTypeSpinner);

        totalBoxQtyEditText = findViewById(R.id.totalBoxQtyEditText);
        totalBoxQtyEditText.setEnabled(false);

        totalBagWeightEditText = findViewById(R.id.totalBagWeightEditText);
        totalBagWeightEditText.setEnabled(false);

        deductionAmountEditText = findViewById(R.id.deductionAmountEditText);
        hamaliAmountEditText = findViewById(R.id.hamaliAmountEditText);
        hamaliAmountEditText.setEnabled(false);

        amountPaidToHVendorEditText = findViewById(R.id.amountPaidToHVendorEditText);
        amountPaidToHVendorEditText.setEnabled(false);

        lottieAnimationView = findViewById(R.id.lottieAnimationView);
        blockingView = findViewById(R.id.blockingView);

        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> addRowToTable());

        Button submitButton = findViewById(R.id.submit);
        submitButton.setOnClickListener(v -> submitDataToServer());

        Log.d("First letter Before : ", String.valueOf(firstLetter));

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
            depo = intent.getStringExtra("depo");
            year = intent.getStringExtra("year");

            firstLetter = username.charAt(0);
            Log.d("First Letter After : ", String.valueOf(firstLetter));

            // Set the fetched username to the TextView
            if (username != null) {
                String usernameText = getString(R.string.user_name_prefix, username + " | Depot : " + depo);
                showUserNameTextView.setText(usernameText);
            }
        }

        cameraView = findViewById(R.id.showCameraSurfaceView);
        barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector).setAutoFocusEnabled(true).setRequestedPreviewSize(1600, 1024).build();

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                if (ContextCompat.checkSelfPermission(MainActivity2.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (Exception e) {
                        showAlert("Camera Error", "Failed to start the camera. Please try again.");
                    }
                } else {
                    ActivityCompat.requestPermissions(MainActivity2.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                try {
                    cameraSource.stop();
                } catch (Exception e) {
                    showWarning("Camera Warning", "Failed to stop the camera properly.");
                }
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(@NonNull Detector.Detections<Barcode> detections) {
                // Check if LR number is currently being processed
                if (isProcessing) {
                    return;
                }
                SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    String lrNumber = barcodes.valueAt(0).displayValue;
                    isProcessing = true;
                    runOnUiThread(() -> addLRNumberToTableFromBarcodeScan(lrNumber));
                }
            }
        });

        fetchHvendors();

        hamaliVendorNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedVendor = parent.getItemAtPosition(position).toString();
                if (selectedVendor.equals("No hamali Vendor")) {
                    hamaliAmountEditText.setText("0.0");
                    hamaliAmountEditText.setEnabled(false);

                    deductionAmountEditText.setText("0.0");

                    amountPaidToHVendorEditText.setText("0.0");
                    amountPaidToHVendorEditText.setEnabled(false);
                } else {
                    // If user select any pother value then calculate,
                    calculateHamali();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing if nothing is selected
            }
        });

        hamaliTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void addRowToTable() {
        String lrNumber = lrEditText.getText().toString().trim();
        if (LR_NUMBER_PATTERN.matcher(lrNumber).matches() && !lrNumber.isEmpty() && !lrNumbersSet.contains(lrNumber)) {
            checkLRNumberOnServer(lrNumber);
        } else {
            showWarning("Warning", "LR number format is invalid or duplicate");
        }
    }

    private void addLRNumberToTableFromBarcodeScan(String lrNumber) {
        if (LR_NUMBER_PATTERN.matcher(lrNumber).matches() && !lrNumbersSet.contains(lrNumber)) {
            checkLRNumberOnServer(lrNumber);
        } else {
            // Show Toast message if LR number format is invalid or duplicate
            isProcessing = false;
            showWarning("Warning", "LR number format is invalid or duplicate");
        }
    }

    private void checkLRNumberOnServer(String lrNumber) {

        runOnUiThread(() -> {
            blockingView.setVisibility(View.VISIBLE);
            lottieAnimationView.setVisibility(View.VISIBLE);
            lottieAnimationView.playAnimation();
        });

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("lrNumber", lrNumber);
        formBuilder.add("depo", depo);

        String url = (firstLetter == 'C' || firstLetter == 'c') ? "https://vtc3pl.com/cp_prn_app_get_lrno.php" : "https://vtc3pl.com/prn_app_get_lrno.php";

        Request request = new Request.Builder().url(url).post(formBuilder.build()).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    lottieAnimationView.setVisibility(View.GONE);
                    blockingView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();
                    showAlert("Connection Failed ", "Failed to connect to server");
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
                        Log.i("responseBody : Auto ", responseBody);
                        if (responseBody.equals("0")) {
                            runOnUiThread(() -> showWarning("Warning", "This LR Number not available for PRN"));
                        } else if (responseBody.equals(lrNumber)) {
                            Log.d("LR NUMBER : ", String.valueOf(response));
                            runOnUiThread(() -> addLRNumberToTable(lrNumber));
                        }
                    } else {
                        runOnUiThread(() -> showAlert("Empty Response", "Empty response is received from server"));
                    }
                } else {
                    runOnUiThread(() -> {
                        Log.d("Server Error : ", String.valueOf(response));
                        showAlert("Server Error", "Server error: " + response.code());
                    });
                }
            }
        });
    }

    private void addLRNumberToTable(String lrNumber) {
        TableRow newRow = new TableRow(this);
        TextView textView = new TextView(this);
        textView.setText(lrNumber);
        newRow.addView(textView);

        Button deleteButton = new Button(this);
        deleteButton.setText(getString(R.string.delete_button_text));
        deleteButton.setOnClickListener(v -> {
            // Remove the row when delete button is clicked
            tableLayout.removeView(newRow);
            // Remove LR number from the set
            lrNumbersSet.remove(lrNumber);

            fetchWeightsFromServer();

            // Calculate new hamali amount after deleting the LR number
            calculateHamali();
        });
        newRow.addView(deleteButton);

        tableLayout.addView(newRow, 0);

        // Add LR number to the set
        lrNumbersSet.add(lrNumber);

        fetchWeightsFromServer();
        // Clear the lrEditText after adding the row
        lrEditText.setText("");
        isProcessing = false;
    }

    private void fetchHvendors() {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        // URL for fetching Hvendors
        String url = (firstLetter == 'C' || firstLetter == 'c') ? "https://vtc3pl.com/cp_fetch_hamalivendor_only_prn_app.php" : "https://vtc3pl.com/fetch_hamalivendor_only_prn_app.php";

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
                        Log.i("Hamali: ", responseBody);
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
                                runOnUiThread(() -> showAlert("Error", "hamali vendors not found."));
                            }
                        } catch (JSONException e) {
                            runOnUiThread(() -> showAlert("Wrong Response Error", "Wrong response received from server."));
                        }

                        // Update the spinner UI on the main thread
                        runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity2.this, android.R.layout.simple_spinner_item, hVendors);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            hamaliVendorNameSpinner.setAdapter(adapter); // Use hamaliVendorNameSpinner instead of goDownSpinner
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

    private void fetchWeightsFromServer() {

        runOnUiThread(() -> {
            blockingView.setVisibility(View.VISIBLE);
            lottieAnimationView.setVisibility(View.VISIBLE);
            lottieAnimationView.playAnimation();
        });

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        // URL for fetching weights
        String url = (firstLetter == 'C' || firstLetter == 'c') ? "https://vtc3pl.com/cp_hamali_bag_box_weight_prn_app.php" : "https://vtc3pl.com/hamali_bag_box_weight_prn_app.php";

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(() -> {
                    lottieAnimationView.setVisibility(View.GONE);
                    blockingView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();
                    showAlert("Connection Failed Error", "Failed to fetch Box Quantity and Bag Weight from server");
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
                        Log.i("fetchWeightsFromServer", responseBody);
                        // Parse the JSON response
                        try {
                            JSONArray jsonArray = new JSONArray(responseBody);
                            double totalBoxWeight = 0;
                            double totalBoxQty = 0;
                            double totalBagWeight = 0;
                            double totalBagQty = 0;

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String lrNumber = jsonObject.getString("LRNO");
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


                            // Update the UI on the main thread
                            runOnUiThread(() -> {
                                totalBoxQtyEditText.setText(String.valueOf(totalBoxQtyFromAllLRNO));
                                totalBagWeightEditText.setText(String.valueOf(totalBagWeightFromAllLRNO));

                                Log.d("totalBoxWeight : ", String.valueOf(totalBoxWeightFromAllLRNO));
                                Log.d("totalBagWeight : ", String.valueOf(totalBagWeightFromAllLRNO));

                                Log.d("totalBoxQty : ", String.valueOf(totalBoxQtyFromAllLRNO));
                                Log.d("totalBagQty : ", String.valueOf(totalBagQtyFromAllLRNO));
                            });
                        } catch (JSONException e) {
                            runOnUiThread(() -> showAlert("Wrong Response Error", "Wrong response received from server."));
                        }
                    } else {
                        runOnUiThread(() -> showAlert("Empty Response Error", "Response body is null for(Box Qty and Bag Weight) received."));
                    }
                } else {
                    onFailure(call, new IOException("Unexpected response code " + response));
                }
            }
        });
    }

    private void calculateHamali() {
        Log.d("calculateHamali() :", "Method is invoked");

        if (hamaliVendorNameSpinner.getSelectedItem() == null || hamaliTypeSpinner.getSelectedItem() == null) {
            // One or both spinners are not selected, return without calculating hamali
            return;
        }

        selectedHamaliVendor = hamaliVendorNameSpinner.getSelectedItem().toString();

        selectedHamaliType = hamaliTypeSpinner.getSelectedItem().toString();

        runOnUiThread(() -> {
            blockingView.setVisibility(View.VISIBLE);
            lottieAnimationView.setVisibility(View.VISIBLE);
            lottieAnimationView.playAnimation();
        });

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("spinnerDepo", depo);
        formBuilder.add("Hvendor", selectedHamaliVendor);

        String url = (firstLetter == 'C' || firstLetter == 'c') ? "https://vtc3pl.com/cp_fetch_hamali_rates_calculation_prn_app.php" : "https://vtc3pl.com/fetch_hamali_rates_calculation_prn_app.php";

        Request request = new Request.Builder().url(url).post(formBuilder.build()).build();

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
                                hamaliAmountEditText.setText(String.valueOf(totalHamaliAmount));
                                hamaliAmountEditText.setEnabled(false);
                            });

                            // Assuming you have deductionAmountEditText and amountPaidToHVendorEditText declared and initialized
                            // Assuming these variables are declared globally
                            deductionAmountEditText.setOnKeyListener((v, keyCode, event) -> {
                                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                                    deductionAmount = Double.parseDouble(deductionAmountEditText.getText().toString());

                                    if (deductionAmount < 0) {
                                        // Prevent deduction amount from being less than zero
                                        Toast.makeText(MainActivity2.this, "Deduction amount cannot be less than zero", Toast.LENGTH_SHORT).show();
                                        deductionAmountEditText.setText("0.0");
                                        return true;
                                    }
                                    amountPaidToHVendor = (totalHamaliAmount - deductionAmount);
                                    Log.d("amountPaidToHVendor : ", String.valueOf(amountPaidToHVendor));
                                    amountPaidToHVendorEditText.setText(String.valueOf(amountPaidToHVendor));
                                    amountPaidToHVendorEditText.setEnabled(false);
                                    return true;
                                }
                                return false;
                            });

                            deductionAmountEditText.setOnFocusChangeListener((v, hasFocus) -> {
                                if (!hasFocus) {
                                    // Calculate amount paid to vendor when deduction amount is entered
                                    String deductionAmountStr = deductionAmountEditText.getText().toString().trim();
                                    deductionAmount = deductionAmountStr.isEmpty() ? 0.0 : Double.parseDouble(deductionAmountStr);
                                    amountPaidToHVendor = Double.parseDouble(hamaliAmountEditText.getText().toString()) - deductionAmount;
                                    amountPaidToHVendorEditText.setText(String.valueOf(amountPaidToHVendor));
                                    amountPaidToHVendorEditText.setEnabled(false);
                                }
                            });

                            // Set up hamaliAmountEditText listener
                            hamaliAmountEditText.addTextChangedListener(new TextWatcher() {
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
                                    String deductionAmountStr = deductionAmountEditText.getText().toString().trim();
                                    deductionAmount = deductionAmountStr.isEmpty() ? 0.0 : Double.parseDouble(deductionAmountStr);
                                    amountPaidToHVendor = (hamaliAmount - deductionAmount);
                                    amountPaidToHVendorEditText.setText(String.valueOf(amountPaidToHVendor));
                                    amountPaidToHVendorEditText.setEnabled(false);
                                }
                            });

                        } catch (JSONException e) {
                            runOnUiThread(() -> showAlert("Response Error", "Wrong response received."));
                        }
                    } else {
                        runOnUiThread(() -> showAlert("Empty Response Error", "Response body is empty for hamali rates."));
                    }
                } else {
                    runOnUiThread(() -> showAlert("Server Error", "Server error: " + response.code()));
                }
            }
        });
    }


    private void submitDataToServer() {
        // Retrieve data from UI components
        String vehicleNo = vehicleNumberEditText.getText().toString();
        String goDown = goDownSpinner.getSelectedItem().toString();
//        String hamaliVendor = hamaliVendorNameSpinner.getSelectedItem().toString(); //giving null that why it is commented

        Object selectedItem = hamaliVendorNameSpinner.getSelectedItem();
        if (selectedItem == null) {
            showWarning("Unselected Field Warning", "Please select hamali vendor name.");
            return;
        }

        String hamaliVendor = selectedItem.toString();

        String amountPaidToHVendor = amountPaidToHVendorEditText.getText().toString().trim();
//        String hamaliType = hamaliTypeSpinner.getSelectedItem().toString();
        String deductionAmount = deductionAmountEditText.getText().toString().trim();
        String hamaliAmount = hamaliAmountEditText.getText().toString().trim();

        if (lrNumbersSet.isEmpty()) {
            showWarning("LR Number Not Found", "At least one LR Number require to create PRN");
            lrEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(lrEditText, InputMethodManager.SHOW_IMPLICIT);
            return;
        }

        if (vehicleNo.isEmpty()) {
            showWarning("Empty Field Warning", "Please enter vehicle Number");
            vehicleNumberEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(vehicleNumberEditText, InputMethodManager.SHOW_IMPLICIT);
            return;
        }

        if (hamaliVendor.equals("Please Select Vendor")) {
            showWarning("Unselected Field Warning", "Please select hamali vendor name.");
            return;
        }

        if (amountPaidToHVendor.isEmpty() || hamaliAmount.isEmpty()) {
            showWarning("Empty Field Warning", "Amount is empty.");
            return;
        }

        if (goDown.equals("Select Godown")) {
            showWarning("Unselected Field Warning", "Please select Godown line.");
            return;
        }

        List<String> lrNumbers = new ArrayList<>();
        for (String lrNumber : lrNumbersSet) {
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
        formBuilder.add("spinnerYear", year);
        formBuilder.add("vehicleNo", vehicleNo);
        formBuilder.add("goDown", goDown);
        formBuilder.add("arrayListOfLR", arrayListOfLR);
        formBuilder.add("selectedHamaliVendor", selectedHamaliVendor);
        formBuilder.add("finalHamliAmount", String.valueOf(amountPaidToHVendor));
        formBuilder.add("selectedHamaliType", selectedHamaliType);
        formBuilder.add("deductionAmount", String.valueOf(deductionAmount));

        String url = (firstLetter == 'C' || firstLetter == 'c') ? "https://vtc3pl.com/cp_insert_prn_app.php" : "https://vtc3pl.com/insert_prn_app.php";

        Request request = new Request.Builder().url(url).post(formBuilder.build()).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("MainActivity2(submit)", "Failed to connect to server", e);
                runOnUiThread(() -> {
                    lottieAnimationView.setVisibility(View.GONE);
                    blockingView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();
                    showAlert("Connection Failed Error", "Failed to connect to server");
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

                                final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity2.this).setTitle("Success").setMessage(responseBody).setPositiveButton("OK", (dialog, which) -> {
                                    dialog.dismiss();
                                    clearUIComponents();
                                }).setNeutralButton("Copy", (dialog, which) -> {
                                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("Response", responseBody);
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(MainActivity2.this, "Response copied to clipboard", Toast.LENGTH_SHORT).show();
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
                        Log.e("Response CreatePRN:", "Empty response body");
                        runOnUiThread(() -> showAlert("Empty Response Error", "Empty response received from server"));
                    }
                } else {
                    runOnUiThread(() -> showAlert("Server Error", "Server error: " + response.code()));
                }
            }
        });
    }

    private void clearUIComponents() {
        vehicleNumberEditText.setText("");
        lrEditText.setText("");
        tableLayout.removeAllViews();
        lrNumbersSet.clear();
        goDownSpinner.setSelection(0);
        hamaliVendorNameSpinner.setSelection(0);
        amountPaidToHVendorEditText.setText("");
        hamaliTypeSpinner.setSelection(0);
        deductionAmountEditText.setText("");
        hamaliAmountEditText.setText("");

        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    showWarning("Permission Warning", "Camera permission is granted, but check failed unexpectedly.");
                    return;
                }
                cameraSource.start(cameraView.getHolder());
            } catch (IOException e) {
                showAlert("Camera Error", "Failed to start the camera. Please try again.");
            }
        } else {
            showAlert("Permission Denied Error", "Please give camera permission to scan the LR Number.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
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