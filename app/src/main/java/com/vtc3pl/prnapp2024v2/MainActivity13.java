package com.vtc3pl.prnapp2024v2;
// Edit PRN

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity13 extends AppCompatActivity {

    private static final Pattern LR_NUMBER_PATTERN = Pattern.compile("[A-Z]{3,4}[0-9]{10}+");
    private final Set<String> lrNumbersSet = new HashSet<>();
    private String username = "", depo = "", year = "";
    private TextView showUserNameActivityThirteenTextView, prnNumberActivityThirteenTextView;
    private EditText editTextPRNActivityThirteen, hamaliAmountEditTextActivityThirteen, amountPaidToHVendorEditTextActivityThirteen, deductionAmountEditTextActivityThirteen, lrEditTextActivityThirteen, totalBoxQtyEditTextActivityThirteen, totalBagWeightEditTextActivityThirteen;
    private Button searchPRNButtonActivityThirteen, addButtonActivityThirteen;
    private ScrollView scrollViewActivityThirteen;
    private TableLayout tableDisplayActivityThirteen;
    private Spinner hamaliVendorNameSpinnerActivityThirteen, hamaliTypeSpinnerActivityThirteen;
    private double totalBoxWeightFromAllLRNO = 0, totalBoxQtyFromAllLRNO = 0, totalBagWeightFromAllLRNO = 0, totalBagQtyFromAllLRNO = 0;
    private String selectedHamaliVendor = "";
    private String selectedHamaliType = "";
    private double amountPaidToHVendor, deductionAmount;
    private ConstraintLayout hamaliCalculationsFeildConstraintLayout;
    private boolean fromPRNEditText = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main13);

        showUserNameActivityThirteenTextView = findViewById(R.id.showUserNameActivityThirteenTextView);
        prnNumberActivityThirteenTextView = findViewById(R.id.prnNumberActivityThirteenTextView);
        editTextPRNActivityThirteen = findViewById(R.id.editTextPRNActivityThirteen);
        editTextPRNActivityThirteen.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        searchPRNButtonActivityThirteen = findViewById(R.id.searchPRNButtonActivityThirteen);
        scrollViewActivityThirteen = findViewById(R.id.scrollViewActivityThirteen);
        hamaliAmountEditTextActivityThirteen = findViewById(R.id.hamaliAmountEditTextActivityThirteen);
        hamaliAmountEditTextActivityThirteen.setEnabled(false);

        amountPaidToHVendorEditTextActivityThirteen = findViewById(R.id.amountPaidToHVendorEditTextActivityThirteen);
        amountPaidToHVendorEditTextActivityThirteen.setEnabled(false);

        deductionAmountEditTextActivityThirteen = findViewById(R.id.deductionAmountEditTextActivityThirteen);
        lrEditTextActivityThirteen = findViewById(R.id.lrEditTextActivityThirteen);
        lrEditTextActivityThirteen.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        addButtonActivityThirteen = findViewById(R.id.addButtonActivityThirteen);
        tableDisplayActivityThirteen = findViewById(R.id.tableDisplayActivityThirteen);
        hamaliVendorNameSpinnerActivityThirteen = findViewById(R.id.hamaliVendorNameSpinnerActivityThirteen);
        hamaliTypeSpinnerActivityThirteen = findViewById(R.id.hamaliTypeSpinnerActivityThirteen);

        totalBoxQtyEditTextActivityThirteen = findViewById(R.id.totalBoxQtyEditTextActivityThirteen);
        totalBoxQtyEditTextActivityThirteen.setEnabled(false);
        totalBagWeightEditTextActivityThirteen = findViewById(R.id.totalBagWeightEditTextActivityThirteen);
        totalBagWeightEditTextActivityThirteen.setEnabled(false);

        hamaliCalculationsFeildConstraintLayout = findViewById(R.id.hamaliCalculationsFeildConstraintLayout);

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
            depo = intent.getStringExtra("depo");
            year = intent.getStringExtra("year");

            if (username != null) {
                String usernameText = getString(R.string.user_name_prefix, username);
                showUserNameActivityThirteenTextView.setText(usernameText);
            }
        }

        searchPRNButtonActivityThirteen.setOnClickListener(v -> {
            String prnId = editTextPRNActivityThirteen.getText().toString().trim();
            if (!prnId.isEmpty()) {
                searchPRNButtonActivityThirteen.setEnabled(false);
                scrollViewActivityThirteen.setVisibility(View.VISIBLE);
                fetchPRNData(prnId, depo);
            } else {
                showAlert("Empty Field", "PRN Number is empty.");
            }
        });

        addButtonActivityThirteen.setOnClickListener(v -> addRowToTable());

        fetchHvendors();

        hamaliVendorNameSpinnerActivityThirteen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedVendor = parent.getItemAtPosition(position).toString();
                if (selectedVendor.equals("No hamali Vendor")) {
                    hamaliAmountEditTextActivityThirteen.setText("0.0");
                    hamaliAmountEditTextActivityThirteen.setEnabled(false);

                    deductionAmountEditTextActivityThirteen.setText("0.0");

                    amountPaidToHVendorEditTextActivityThirteen.setText("0.0");
                    amountPaidToHVendorEditTextActivityThirteen.setEnabled(false);
                } else {
                    calculateHamali();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing if nothing is selected
            }
        });

        hamaliTypeSpinnerActivityThirteen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        String lrNumber = lrEditTextActivityThirteen.getText().toString().trim();
        if (LR_NUMBER_PATTERN.matcher(lrNumber).matches() && !lrNumber.isEmpty() && !lrNumbersSet.contains(lrNumber)) {
            checkLRNumberOnServer(lrNumber);
        } else {
            showWarning("Warning", "LR number format is invalid or duplicate");
        }
    }

    private void checkLRNumberOnServer(String lrNumber) {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("lrNumber", lrNumber);
        formBuilder.add("depo", depo);

        String url = "https://vtc3pl.com/prn_app_get_lrno.php";

        Request request = new Request.Builder().url(url).post(formBuilder.build()).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    showAlert("Connection Failed", "Failed to connect to server");
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        String responseBody = body.string();
                        if (responseBody.equals("0")) {
                            runOnUiThread(() -> {
                                showWarning("Warning", "This LR Number not available for PRN");
                            });
                        } else if (responseBody.equals(lrNumber)) {
                            Log.d("LR NUMBER : ", String.valueOf(response));
                            fromPRNEditText = false;
                            runOnUiThread(() -> addLRNumberToTable(lrNumber, fromPRNEditText));
                        }
                    } else {
                        runOnUiThread(() -> {
                            showAlert("Empty Response", "Empty response is received from server");
                        });
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

    private void addLRNumberToTable(String lrNumber, boolean fromPRNEditText) {
        TableRow newRow = new TableRow(this);
        TextView textView = new TextView(this);
        textView.setText(lrNumber);
        newRow.addView(textView);

        Button deleteButton = new Button(this);
        deleteButton.setText(getString(R.string.delete_button_text));
        deleteButton.setOnClickListener(v -> {
            tableDisplayActivityThirteen.removeView(newRow);
            lrNumbersSet.remove(lrNumber);
            if (!fromPRNEditText) {
                fetchWeightsFromServer();
            }
            calculateHamali();
        });
        newRow.addView(deleteButton);
        tableDisplayActivityThirteen.addView(newRow, 0);
        lrNumbersSet.add(lrNumber);
        if (!fromPRNEditText) {
            fetchWeightsFromServer();
        }
        lrEditTextActivityThirteen.setText("");
    }

    private void fetchWeightsFromServer() {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        String url = "https://vtc3pl.com/hamali_bag_box_weight_prn_app.php";

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    showAlert("Connection Failed Error", "Failed to fetch Box Quantity and Box Weight from server");
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        String responseBody = body.string();
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

                            runOnUiThread(() -> {
                                totalBoxQtyEditTextActivityThirteen.setText(String.valueOf(totalBoxQtyFromAllLRNO));
                                totalBagWeightEditTextActivityThirteen.setText(String.valueOf(totalBagWeightFromAllLRNO));

                                Log.d("totalBoxWeight : ", String.valueOf(totalBoxWeightFromAllLRNO));
                                Log.d("totalBagWeight : ", String.valueOf(totalBagWeightFromAllLRNO));

                                Log.d("totalBoxQty : ", String.valueOf(totalBoxQtyFromAllLRNO));
                                Log.d("totalBagQty : ", String.valueOf(totalBagQtyFromAllLRNO));

                                // Check and hide the ConstraintLayout based on the values
                                checkAndHideHamaliCalculations();

                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                showAlert("Wrong Response Error", "Wrong response received from server.");
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            showAlert("Empty Response Error", "Response body is null for(Box Qty and Bag Weight) received.");
                        });
                    }
                } else {
                    onFailure(call, new IOException("Unexpected response code " + response));
                }
            }
        });
    }

    private void fetchHvendors() {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        String url = "https://vtc3pl.com/fetch_hamalivendor_only_prn_app.php";

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
                                runOnUiThread(() -> {
                                    showAlert("Error", "hamali vendors not found.");
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity13.this, android.R.layout.simple_spinner_item, hVendors);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            hamaliVendorNameSpinnerActivityThirteen.setAdapter(adapter);
                        });
                    } else {
                        runOnUiThread(() -> {
                            showAlert("Empty Response Error", "Empty response received from server for vendors.");
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
                    showAlert("Connection Failed", "Failed to fetch Hamali Vendors");
                });
            }
        });
    }

    private void calculateHamali() {
        Log.d("calculateHamali() :", "Method is invoked");

        if (hamaliVendorNameSpinnerActivityThirteen.getSelectedItem() == null || hamaliTypeSpinnerActivityThirteen.getSelectedItem() == null) {
            // One or both spinners are not selected, return without calculating hamali
            return;
        }

        selectedHamaliVendor = hamaliVendorNameSpinnerActivityThirteen.getSelectedItem().toString();

        selectedHamaliType = hamaliTypeSpinnerActivityThirteen.getSelectedItem().toString();

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("spinnerDepo", depo);
        formBuilder.add("Hvendor", selectedHamaliVendor);

        String url = "https://vtc3pl.com/fetch_hamali_rates_calculation_prn_app.php";

        Request request = new Request.Builder().url(url).post(formBuilder.build()).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    showAlert("Connection Failed Error", "Failed to fetch hamali rates from server");
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
                                    showAlert("Unknown hamali Type Error", "Unknown hamali type selected.");
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
                                hamaliAmountEditTextActivityThirteen.setText(String.valueOf(totalHamaliAmount));
                                hamaliAmountEditTextActivityThirteen.setEnabled(false);
                            });

                            // Assuming you have deductionAmountEditText and amountPaidToHVendorEditText declared and initialized
                            // Assuming these variables are declared globally
                            deductionAmountEditTextActivityThirteen.setOnKeyListener((v, keyCode, event) -> {
                                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                                    deductionAmount = Double.parseDouble(deductionAmountEditTextActivityThirteen.getText().toString());

                                    if (deductionAmount < 0) {
                                        // Prevent deduction amount from being less than zero
                                        Toast.makeText(MainActivity13.this, "Deduction amount cannot be less than zero", Toast.LENGTH_SHORT).show();
                                        deductionAmountEditTextActivityThirteen.setText("0.0");
                                        return true;
                                    }
                                    amountPaidToHVendor = (totalHamaliAmount - deductionAmount);
                                    Log.d("amountPaidToHVendor : ", String.valueOf(amountPaidToHVendor));
                                    amountPaidToHVendorEditTextActivityThirteen.setText(String.valueOf(amountPaidToHVendor));
                                    amountPaidToHVendorEditTextActivityThirteen.setEnabled(false);
                                    return true;
                                }
                                return false;
                            });

                            deductionAmountEditTextActivityThirteen.setOnFocusChangeListener((v, hasFocus) -> {
                                if (!hasFocus) {
                                    // Calculate amount paid to vendor when deduction amount is entered
                                    String deductionAmountStr = deductionAmountEditTextActivityThirteen.getText().toString().trim();
                                    deductionAmount = deductionAmountStr.isEmpty() ? 0.0 : Double.parseDouble(deductionAmountStr);
                                    amountPaidToHVendor = Double.parseDouble(hamaliAmountEditTextActivityThirteen.getText().toString()) - deductionAmount;
                                    amountPaidToHVendorEditTextActivityThirteen.setText(String.valueOf(amountPaidToHVendor));
                                    amountPaidToHVendorEditTextActivityThirteen.setEnabled(false);
                                }
                            });

                            // Set up hamaliAmountEditText listener
                            hamaliAmountEditTextActivityThirteen.addTextChangedListener(new TextWatcher() {
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
                                    String deductionAmountStr = deductionAmountEditTextActivityThirteen.getText().toString().trim();
                                    deductionAmount = deductionAmountStr.isEmpty() ? 0.0 : Double.parseDouble(deductionAmountStr);
                                    amountPaidToHVendor = (hamaliAmount - deductionAmount);
                                    amountPaidToHVendorEditTextActivityThirteen.setText(String.valueOf(amountPaidToHVendor));
                                    amountPaidToHVendorEditTextActivityThirteen.setEnabled(false);
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                showAlert("Response Error", "Wrong response received.");
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            showAlert("Empty Response Error", "Response body is empty for hamali rates.");
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        showAlert("Server Error", "Server error: " + response.code());
                    });
                }
            }
        });
    }

    private void checkAndHideHamaliCalculations() {
        String boxQtyText = totalBoxQtyEditTextActivityThirteen.getText().toString().trim();
        String bagWeightText = totalBagWeightEditTextActivityThirteen.getText().toString().trim();

        boolean isBoxQtyEmptyOrZero = boxQtyText.isEmpty() || boxQtyText.equals("0.0") || boxQtyText.equals("0");
        boolean isBagWeightEmptyOrZero = bagWeightText.isEmpty() || bagWeightText.equals("0.0") || bagWeightText.equals("0");

        if (isBoxQtyEmptyOrZero && isBagWeightEmptyOrZero) {
            hamaliCalculationsFeildConstraintLayout.setVisibility(View.GONE);
        } else {
            hamaliCalculationsFeildConstraintLayout.setVisibility(View.VISIBLE);
        }
    }

    private void fetchPRNData(String prnId, String depo) {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        String url = "https://vtc3pl.com/fetch_prn_data_for_prn_app.php";

        RequestBody formBody = new FormBody.Builder().add("prnId", prnId).add("depo", depo).build();

        Request request = new Request.Builder().url(url).post(formBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    showAlert("Connection Failed Error", "Failed to fetch PRN data from server.");
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        String responseBody = body.string();
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);

                            JSONArray prvArray = jsonResponse.getJSONArray("prv");
                            if (prvArray.length() > 0) {
                                JSONObject prvTable = prvArray.getJSONObject(0);
                                double totalBoxQty = prvTable.getDouble("TotalBoxQty");
                                double totalBagWeight = prvTable.getDouble("TotalWeightBag");
                                Log.i("totalBoxQty : ", String.valueOf(totalBoxQty));
                                Log.i("totalBagWeight : ", String.valueOf(totalBagWeight));

                                runOnUiThread(() -> {
                                    totalBoxQtyEditTextActivityThirteen.setText(String.valueOf(totalBoxQty));
                                    totalBagWeightEditTextActivityThirteen.setText(String.valueOf(totalBagWeight));
                                    totalBoxQtyFromAllLRNO = totalBoxQty;
                                    totalBagWeightFromAllLRNO = totalBagWeight;
                                });
                            }

                            JSONArray lrnos = jsonResponse.getJSONArray("T1");
                            for (int i = 0; i < lrnos.length(); i++) {
                                String lrno = lrnos.getJSONObject(i).getString("LRNO");
                                fromPRNEditText = true;
                                runOnUiThread(() -> addLRNumberToTable(lrno, fromPRNEditText));
                            }

                            runOnUiThread(() -> checkAndHideHamaliCalculations());

                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                showAlert("Wrong Response Error", "Wrong response received from server.");
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            showAlert("Empty Response Error", "Response body is null.");
                        });
                    }
                } else {
                    onFailure(call, new IOException("Unexpected response code " + response));
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