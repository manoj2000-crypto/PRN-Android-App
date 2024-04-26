package com.vtc3pl.prnapp2024v2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

public class MainActivity2 extends AppCompatActivity {

    private static final Pattern LR_NUMBER_PATTERN = Pattern.compile("[A-Z]{3,4}[0-9]{10}+");
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private final Set<String> lrNumbersSet = new HashSet<>();
    private TableLayout tableLayout;
    private EditText lrEditText, vehicleNumberEditText, totalBoxWeightEditText, totalBagWeightEditText;
    private SurfaceView cameraView;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;

    private TextView showUserNameTextView;

    private Spinner goDownSpinner, hamaliVendorNameSpinner;

    private String username = "", depo = "", year = "";

    // Define a flag to indicate whether an LR number is being processed
    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);

        showUserNameTextView = findViewById(R.id.showUserNameTextView);
        tableLayout = findViewById(R.id.tableDisplay);
        lrEditText = findViewById(R.id.lrEditText);
        vehicleNumberEditText = findViewById(R.id.vehicleNumberEditText);
        goDownSpinner = findViewById(R.id.goDownSpinner);
        hamaliVendorNameSpinner = findViewById(R.id.hamaliVendorNameSpinner);
        totalBoxWeightEditText = findViewById(R.id.totalBoxWeightEditText);
        totalBagWeightEditText = findViewById(R.id.totalBagWeightEditText);

        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> addRowToTable());

        Button submitButton = findViewById(R.id.submit);
        submitButton.setOnClickListener(v -> submitDataToServer());

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
            depo = intent.getStringExtra("depo");
            year = intent.getStringExtra("year");

            // Set the fetched username to the TextView
            if (username != null) {
                showUserNameTextView.setText("User name: " + username);
            }
        }

        cameraView = findViewById(R.id.showCameraSurfaceView);
        barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector).setAutoFocusEnabled(true).setRequestedPreviewSize(1600, 1024).build();

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ContextCompat.checkSelfPermission(MainActivity2.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ActivityCompat.requestPermissions(MainActivity2.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
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
            // Show Toast message if LR number format is invalid or duplicate
            Toast.makeText(this, "LR number format is invalid or duplicate", Toast.LENGTH_SHORT).show();
        }
    }

    private void addLRNumberToTableFromBarcodeScan(String lrNumber) {
        if (LR_NUMBER_PATTERN.matcher(lrNumber).matches() && !lrNumbersSet.contains(lrNumber)) {
            checkLRNumberOnServer(lrNumber);
        } else {
            // Show Toast message if LR number format is invalid or duplicate
            isProcessing = false;
            Toast.makeText(this, "LR number format is invalid or duplicate", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkLRNumberOnServer(String lrNumber) {
        OkHttpClient client = new OkHttpClient();

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("lrNumber", lrNumber);

        Request request = new Request.Builder().url("https://vtc3pl.com/prn_app_get_lrno.php").post(formBuilder.build()).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity2.this, "Failed to connect to server", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    // Process the response here
                    if (responseBody.equals("0")) {
                        Toast.makeText(MainActivity2.this, "This LR Number not available for PRN", Toast.LENGTH_SHORT).show();
                    } else if (responseBody.equals("PRN already generated")) {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity2.this, "PRN already generated", Toast.LENGTH_SHORT).show();
                        });
                    } else if (responseBody.equals(lrNumber)) {
                        Log.d("LR NUMBER : ", String.valueOf(response));
                        runOnUiThread(() -> addLRNumberToTable(lrNumber));
                    }
                } else {
                    runOnUiThread(() -> {
                        Log.d("Server Error : ", String.valueOf(response));
                        Toast.makeText(MainActivity2.this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
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
        deleteButton.setText("Delete");
        deleteButton.setOnClickListener(v -> {
            // Remove the row when delete button is clicked
            tableLayout.removeView(newRow);
            // Remove LR number from the set
            lrNumbersSet.remove(lrNumber);

            fetchWeightsFromServer();
        });
        newRow.addView(deleteButton);

        tableLayout.addView(newRow);

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
        String url = "https://vtc3pl.com/fetch_hamalivendor_only_prn_app.php";

        // Create a form body with spinnerDepo as a parameter
        FormBody formBody = new FormBody.Builder().add("spinnerDepo", depo).build();

        Request request = new Request.Builder().url(url).post(formBody) // Use POST method and set the form body
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    // Parse the JSON response
                    List<String> hVendors = new ArrayList<>();
                    try {
                        JSONArray jsonArray = new JSONArray(responseBody);
                        hVendors.add("Please Select Vendor");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String hVendor = jsonArray.getString(i);
                            hVendors.add(hVendor);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // Update the spinner UI on the main thread
                    runOnUiThread(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity2.this, android.R.layout.simple_spinner_item, hVendors);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        hamaliVendorNameSpinner.setAdapter(adapter); // Use hamaliVendorNameSpinner instead of goDownSpinner
                    });
                } else {
                    onFailure(call, new IOException("Unexpected response code " + response));
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity2.this, "Failed to fetch Hamali Vendors", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void fetchWeightsFromServer() {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        // URL for fetching weights
        String url = "vtc3pl.com/hamali_bag_box_weight_prn_app.php";

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity2.this, "Failed to fetch weights from server", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    final double[] totalBoxWeight = {0};
                    final double[] totalBagWeight = {0};

                    // Parse the JSON response
                    try {
                        JSONArray jsonArray = new JSONArray(responseBody);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String lrNumber = jsonObject.getString("LRNO");
                            if (lrNumbersSet.contains(lrNumber)) {
                                totalBoxWeight[0] += jsonObject.getDouble("TotalWeightBox");
                                totalBagWeight[0] += jsonObject.getDouble("TotalWeightBag");
                            }
                        }

                        // Update the UI on the main thread
                        runOnUiThread(() -> {
                            totalBoxWeightEditText.setText(String.valueOf(totalBoxWeight[0]));
                            totalBagWeightEditText.setText(String.valueOf(totalBagWeight[0]));
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity2.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    onFailure(call, new IOException("Unexpected response code " + response));
                }
            }
        });
    }

    private void submitDataToServer() {
        // Retrieve data from UI components
        String vehicleNo = vehicleNumberEditText.getText().toString();
        String goDown = goDownSpinner.getSelectedItem().toString();

        if (goDown.equals(getString(R.string.select_godown))) {
            // Show a message to the user
            Toast.makeText(this, "Please select a Godown", Toast.LENGTH_SHORT).show();
            return; // Exit the method
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

        // Make HTTP request
        OkHttpClient client = new OkHttpClient();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("UserName", username);
        formBuilder.add("spinnerDepo", depo);
        formBuilder.add("spinnerYear", year);
        formBuilder.add("vehicleNo", vehicleNo);
        formBuilder.add("goDown", goDown);
        formBuilder.add("arrayListOfLR", arrayListOfLR);

        Request request = new Request.Builder().url("https://vtc3pl.com/insert_prn_app.php").post(formBuilder.build()).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("MainActivity2(submit)", "Failed to connect to server", e);
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity2.this, "Failed to connect to server", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    // Process the response here
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity2.this, responseBody, Toast.LENGTH_SHORT).show();
                        vehicleNumberEditText.setText("");
                        lrEditText.setText("");
                        tableLayout.removeAllViews();
                        lrNumbersSet.clear();
                        goDownSpinner.setSelection(0);
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity2.this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                cameraSource.start(cameraView.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Camera permission was denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }
}