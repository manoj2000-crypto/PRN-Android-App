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

import com.airbnb.lottie.LottieAnimationView;

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
import okhttp3.MediaType;
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
    private Button searchPRNButtonActivityThirteen, addButtonActivityThirteen, submitActivityThirteen;
    private ScrollView scrollViewActivityThirteen;
    private TableLayout tableDisplayActivityThirteen;
    private Spinner hamaliVendorNameSpinnerActivityThirteen, hamaliTypeSpinnerActivityThirteen;
    private double totalBoxWeightFromAllLRNO = 0, totalBoxQtyFromAllLRNO = 0, totalBagWeightFromAllLRNO = 0, totalBagQtyFromAllLRNO = 0;
    private String selectedHamaliVendor = "";
    private String selectedHamaliType = "";
    private double amountPaidToHVendor, deductionAmount;
    private ConstraintLayout hamaliCalculationsFeildConstraintLayout;
    private boolean updatingFromFetchPRNData = false, fromPRNEditText = false;
    private LottieAnimationView lottieAnimationView;

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

        submitActivityThirteen = findViewById(R.id.submitActivityThirteen);
        lottieAnimationView = findViewById(R.id.lottieAnimationView);

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
                editTextPRNActivityThirteen.setEnabled(false);
                searchPRNButtonActivityThirteen.setEnabled(false);
                scrollViewActivityThirteen.setVisibility(View.VISIBLE);
                fetchPRNData(prnId, depo);
            } else {
                showWarning("Empty Field", "PRN Number is empty.");
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

        submitActivityThirteen.setOnClickListener(v -> {
            submitData();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void addRowToTable() {
        String lrNumber = lrEditTextActivityThirteen.getText().toString().trim();
        if (lrNumber.isEmpty()) {
            showWarning("Warning", "LR number cannot be empty");
        } else if (!LR_NUMBER_PATTERN.matcher(lrNumber).matches()) {
            showWarning("Warning", "LR number format is invalid");
        } else if (lrNumbersSet.contains(lrNumber)) {
            showWarning("Warning", "Duplicate LR number");
        } else {
            checkLRNumberOnServer(lrNumber);
        }
    }

    private void checkLRNumberOnServer(String lrNumber) {

        runOnUiThread(() -> {
            // Show the Lottie animation
            lottieAnimationView.setVisibility(View.VISIBLE);
            lottieAnimationView.playAnimation();
        });

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
                    lottieAnimationView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();
                    showAlert("Connection Failed", "Failed to connect to server");
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                runOnUiThread(() -> {
                    lottieAnimationView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();
                });

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
                            runOnUiThread(() -> {
                                addLRNumberToTable(lrNumber);
                            });
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

    private void addLRNumberToTable(String lrNumber) {
        TableRow newRow = new TableRow(this);
        TextView textView = new TextView(this);
        textView.setText(lrNumber);
        newRow.addView(textView);

        Button deleteButton = new Button(this);
        deleteButton.setText(getString(R.string.delete_button_text));
        deleteButton.setOnClickListener(v -> {
            fromPRNEditText = false;
            tableDisplayActivityThirteen.removeView(newRow);
            lrNumbersSet.remove(lrNumber);
            Log.i("Set : ", "After remove lrNumbersSet : " + lrNumbersSet);
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
        Log.i("Set : ", "After add lrNumbersSet : " + lrNumbersSet);
    }

    private void fetchWeightsFromServer() {
        runOnUiThread(() -> {
            // Show the Lottie animation
            lottieAnimationView.setVisibility(View.VISIBLE);
            lottieAnimationView.playAnimation();
        });

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        String url = "https://vtc3pl.com/hamali_bag_box_weight_prn_app.php";

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    lottieAnimationView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();
                    showAlert("Connection Failed Error", "Failed to fetch Box Quantity and Box Weight from server");
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                runOnUiThread(() -> {
                    lottieAnimationView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();
                });

                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        String responseBody = body.string();
                        Log.i("responseBody : ", " fetchWeightsFromServer : " + responseBody);
                        try {
                            JSONArray jsonArray = new JSONArray(responseBody);
                            double totalBoxWeight = 0;
                            double totalBoxQty = 0;
                            double totalBagWeight = 0;
                            double totalBagQty = 0;

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String lrNumber = jsonObject.getString("LRNO");
                                Log.i("Response : : ", "lrNumber : " + lrNumber);
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
                                if (!updatingFromFetchPRNData) {
                                    totalBoxQtyEditTextActivityThirteen.setText(String.valueOf(totalBoxQtyFromAllLRNO));
                                    totalBagWeightEditTextActivityThirteen.setText(String.valueOf(totalBagWeightFromAllLRNO));
                                }

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
        runOnUiThread(() -> {
            // Show the Lottie animation
            lottieAnimationView.setVisibility(View.VISIBLE);
            lottieAnimationView.playAnimation();
        });

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
                    lottieAnimationView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();
                    showAlert("Connection Failed Error", "Failed to fetch hamali rates from server");
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    lottieAnimationView.setVisibility(View.GONE);
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
                                    String deductionAmountText = deductionAmountEditTextActivityThirteen.getText().toString().trim();

                                    if (deductionAmountText.isEmpty()) {
                                        deductionAmountText = "0.0";
                                        deductionAmountEditTextActivityThirteen.setText(deductionAmountText);
                                    }

                                    try {
                                        deductionAmount = Double.parseDouble(deductionAmountText);

                                        if (deductionAmount < 0) {
                                            Toast.makeText(MainActivity13.this, "Deduction amount cannot be less than zero", Toast.LENGTH_SHORT).show();
                                            deductionAmountEditTextActivityThirteen.setText("0.0");
                                            return true;
                                        }
                                        amountPaidToHVendor = (totalHamaliAmount - deductionAmount);
                                        Log.d("amountPaidToHVendor : ", String.valueOf(amountPaidToHVendor));
                                        amountPaidToHVendorEditTextActivityThirteen.setText(String.valueOf(amountPaidToHVendor));
                                        amountPaidToHVendorEditTextActivityThirteen.setEnabled(false);
                                        return true;
                                    } catch (NumberFormatException e) {
                                        e.printStackTrace();
                                        Toast.makeText(MainActivity13.this, "Invalid input. Please enter a valid number.", Toast.LENGTH_SHORT).show();
                                        deductionAmountEditTextActivityThirteen.setText("0.0");
                                        return true;
                                    }
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

        runOnUiThread(() -> {
            // Show the Lottie animation
            lottieAnimationView.setVisibility(View.VISIBLE);
            lottieAnimationView.playAnimation();
        });

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        String url = "https://vtc3pl.com/fetch_prn_data_for_prn_app.php";

        RequestBody formBody = new FormBody.Builder().add("prnId", prnId).add("depo", depo).build();

        Request request = new Request.Builder().url(url).post(formBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    lottieAnimationView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();
                    showAlert("Connection Failed Error", "Failed to fetch PRN data from server.");
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                runOnUiThread(() -> {
                    lottieAnimationView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();
                });
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        String responseBody = body.string();
                        Log.i("responseBody", responseBody);
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);

                            if (jsonResponse.has("error")) {
                                String errorMessage = jsonResponse.getString("error");
                                runOnUiThread(() -> showAlert("Server Error", errorMessage));
                                return;
                            }

                            JSONArray prvArray = jsonResponse.getJSONArray("prv");
                            if (prvArray.length() > 0) {
                                JSONObject prvTable = prvArray.getJSONObject(0);
                                double totalBoxQty = prvTable.getDouble("TotalBoxQty");
                                double totalBagWeight = prvTable.getDouble("TotalWeightBag");
                                Log.i("totalBoxQty : ", String.valueOf(totalBoxQty));
                                Log.i("totalBagWeight : ", String.valueOf(totalBagWeight));

                                runOnUiThread(() -> {
                                    updatingFromFetchPRNData = true;
                                    totalBoxQtyEditTextActivityThirteen.setText(String.valueOf(totalBoxQty));
                                    totalBagWeightEditTextActivityThirteen.setText(String.valueOf(totalBagWeight));
                                    totalBoxQtyFromAllLRNO = totalBoxQty;
                                    totalBagWeightFromAllLRNO = totalBagWeight;
                                    updatingFromFetchPRNData = false;
                                });
                            }

                            JSONArray lrnos = jsonResponse.getJSONArray("T1");
                            for (int i = 0; i < lrnos.length(); i++) {
                                String lrno = lrnos.getJSONObject(i).getString("LRNO");
                                fromPRNEditText = true;
                                runOnUiThread(() -> addLRNumberToTable(lrno));
                            }

                            runOnUiThread(() -> {
                                checkAndHideHamaliCalculations();
                            });

                        } catch (JSONException e) {
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

    private void submitData() {
        runOnUiThread(() -> {
            // Show the Lottie animation
            lottieAnimationView.setVisibility(View.VISIBLE);
            lottieAnimationView.playAnimation();
        });

        // Convert lrNumbersSet to JSON Array
        JSONArray jsonArray = new JSONArray(lrNumbersSet);
        String lRNOSetJson = jsonArray.toString();

        String prnNumber = editTextPRNActivityThirteen.getText().toString().trim();
        String totalBagValue = totalBagWeightEditTextActivityThirteen.getText().toString().trim();
        String totalBoxValue = totalBoxQtyEditTextActivityThirteen.getText().toString().trim();
        String hamaliVendorName = hamaliVendorNameSpinnerActivityThirteen.getSelectedItem().toString().trim();
        String hamaliVendorType = hamaliTypeSpinnerActivityThirteen.getSelectedItem().toString().trim();
        String hamaliAmount = hamaliAmountEditTextActivityThirteen.getText().toString().trim();
        String amountPaidToHVendor = amountPaidToHVendorEditTextActivityThirteen.getText().toString().trim();
        String deductionAmount = deductionAmountEditTextActivityThirteen.getText().toString().trim();

        // Create JSON Object to send
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("lrNumbersSet", lRNOSetJson);
            jsonObject.put("prnNumber", prnNumber);
            jsonObject.put("totalBagValue", totalBagValue);
            jsonObject.put("totalBoxValue", totalBoxValue);
            jsonObject.put("hamaliVendorName", hamaliVendorName);
            jsonObject.put("hamaliVendorType", hamaliVendorType);
            jsonObject.put("hamaliAmount", hamaliAmount);
            jsonObject.put("amountPaidToHVendor", amountPaidToHVendor);
            jsonObject.put("deductionAmount", deductionAmount);
            jsonObject.put("userName", username);
            jsonObject.put("depo", depo);
            jsonObject.put("year", year);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Send data to PHP file
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
        RequestBody requestBody = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder().url("https://vtc3pl.com/update_prn_prn_app.php").post(requestBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    lottieAnimationView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();
                    showAlert("Error", "Failed to submit data");
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    lottieAnimationView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();
                });
                String responseBody = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String status = jsonResponse.optString("status", "");
                        String message = jsonResponse.optString("message", "No message from server");

                        if ("success".equals(status)) {
                            runOnUiThread(() -> {
                                // Load the original image
                                Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.success);

                                // Scale the image to the desired size
                                Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 32, 32, true);

                                // Create a Drawable from the scaled Bitmap
                                Drawable successIcon = new BitmapDrawable(getResources(), scaledBitmap);

                                final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity13.this).setTitle("Success").setMessage(message).setPositiveButton("OK", (dialog, which) -> {
                                    dialog.dismiss();
                                    clearUIComponents();
                                }).setIcon(successIcon).create();

                                alertDialog.setOnDismissListener(dialog -> {
                                    dialog.dismiss();
                                    clearUIComponents();
                                });

                                alertDialog.show();
                            });
                        } else {
                            runOnUiThread(() -> showAlert("Error", message));
                        }
                    } catch (JSONException e) {
                        runOnUiThread(() -> showAlert("Error", "Failed to parse server response"));
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(() -> showAlert("Error", "Failed to submit data: " + response.message()));
                }
            }
        });
    }

    private void clearUIComponents() {
        editTextPRNActivityThirteen.setText("");
        lrEditTextActivityThirteen.setText("");
        tableDisplayActivityThirteen.removeAllViews();
        lrNumbersSet.clear();
        hamaliVendorNameSpinnerActivityThirteen.setSelection(0);
        hamaliTypeSpinnerActivityThirteen.setSelection(0);
        hamaliAmountEditTextActivityThirteen.setText("");
        deductionAmountEditTextActivityThirteen.setText("");
        amountPaidToHVendorEditTextActivityThirteen.setText("");

        finish();
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

        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).setIcon(warningIcon).show();
    }
}