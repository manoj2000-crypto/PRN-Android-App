package com.vtc3pl.prnapp2024v2;
//Arrival Page Part 2

import android.content.Context;
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
    private Button submitButtonArrivalPRN, getDataButton;

    private TextView loadingUnloadingTextView, hamaliVendorNameTextViewActivitySeven, hamaliTypeTextViewActivitySeven, hamaliAmountTextViewActivitySeven, deductionAmountTextViewActivitySeven, amountPaidToHVendorTextViewActivitySeven, freightTextView, godownKeeperNameTextView;

    private LottieAnimationView lottieAnimationView;

    private ScrollView scrollViewActivitySeven;
    private View blockingView;

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

        loadingUnloadingTextView = findViewById(R.id.loadingUnloadingTextView);
        hamaliVendorNameTextViewActivitySeven = findViewById(R.id.hamaliVendorNameTextViewActivitySeven);
        hamaliTypeTextViewActivitySeven = findViewById(R.id.hamaliTypeTextViewActivitySeven);
        hamaliAmountTextViewActivitySeven = findViewById(R.id.hamaliAmountTextViewActivitySeven);
        deductionAmountTextViewActivitySeven = findViewById(R.id.deductionAmountTextViewActivitySeven);
        amountPaidToHVendorTextViewActivitySeven = findViewById(R.id.amountPaidToHVendorTextViewActivitySeven);
        freightTextView = findViewById(R.id.freightTextView);
        godownKeeperNameTextView = findViewById(R.id.godownKeeperNameTextView);

        submitButtonArrivalPRN = findViewById(R.id.submitButtonArrivalPRN);
        submitButtonArrivalPRN.setEnabled(false);

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
        scrollViewActivitySeven = findViewById(R.id.scrollViewActivitySeven);

        fetchHvendors();

        hamaliVendorNameSpinnerActivitySeven.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedVendor = parent.getItemAtPosition(position).toString();
                Log.i("SelectedVendor: ", "Parent Selected Vendor : " + selectedVendor);

                if (selectedVendor.equals("No hamali Vendor")) {
                    hamaliAmountEditTextActivitySeven.setText("0.0");
                    hamaliAmountEditTextActivitySeven.setEnabled(false);

                    deductionAmountEditTextActivitySeven.setText("0.0");

                    amountPaidToHVendorEditTextActivitySeven.setText("0.0");
                    amountPaidToHVendorEditTextActivitySeven.setEnabled(false);
                    submitButtonArrivalPRN.setEnabled(true);
                } else if (!selectedVendor.equals("Please Select Vendor")) {
                    Log.i("selectedVendor : ", selectedVendor);
                    calculateHamali();
                    submitButtonArrivalPRN.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        hamaliTypeSpinnerActivitySeven.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calculateHamali();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        Log.i("response : ", response);
        displayDataInTable(response);

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

        submitButtonArrivalPRN.setOnClickListener(v -> {
            blockingView.setVisibility(View.VISIBLE);
            lottieAnimationView.setVisibility(View.VISIBLE);
            lottieAnimationView.playAnimation();

            submitDataToServer();
        });

        getDataButton = findViewById(R.id.getDataButton);

        // Direct references to views
        getDataButton.setOnClickListener(v -> {
            boolean allReasonsFilled = extractDataToJson();
            if (allReasonsFilled) {
                disableComponents();
                scrollViewActivitySeven.setVisibility(View.VISIBLE);
                getDataButton.setEnabled(false);
                submitButtonArrivalPRN.setEnabled(false);
            }
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
                                runOnUiThread(() -> showAlert("Hamali Error", "No hamali vendors found."));
                            }
                        } catch (JSONException e) {
                            runOnUiThread(() -> showAlert("JSON Error", "Exception while fetching Hamali vendor."));
                        }

                        // Update the spinner UI on the main thread
                        runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity7.this, android.R.layout.simple_spinner_item, hVendors);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            hamaliVendorNameSpinnerActivitySeven.setAdapter(adapter);
                        });
                    } else {
                        runOnUiThread(() -> showAlert("Response Error", "Response body is null for hamali vendors)"));
                    }
                } else {
                    onFailure(call, new IOException("Unexpected response code " + response));
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(() -> showAlert("Error", "Failed to fetch Hamali Vendors"));
            }
        });
    }

    private void calculateHamali() {
        Log.d("calculateHamali() :", "Method is invoked");

        if (hamaliVendorNameSpinnerActivitySeven.getSelectedItem() == null || hamaliTypeSpinnerActivitySeven.getSelectedItem() == null)
            return;

        selectedHamaliVendor = hamaliVendorNameSpinnerActivitySeven.getSelectedItem().toString();

        selectedHamaliType = hamaliTypeSpinnerActivitySeven.getSelectedItem().toString();

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
                    showAlert("Error", "Failed to fetch hamali rates");
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
                                runOnUiThread(() -> showAlert("Hamali Type Error", "Unknown hamali type"));
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
                            runOnUiThread(() -> showAlert("JSON Error", "Error parsing JSON response"));
                        }
                    } else {
                        runOnUiThread(() -> showAlert("Response Error", "Response body is empty (Hamali rates)"));
                    }
                } else {
                    runOnUiThread(() -> showAlert("Server Error", "Server error: " + response.code()));
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
                lrNoTextView.setTag("lrNo");

                TextView lrDateTextView = createTextView(jsonObject.getString("LRDate"));
                lrDateTextView.setTag("lrDate");

                TextView toPlaceTextView = createTextView(jsonObject.getString("ToPlace"));
                toPlaceTextView.setTag("toPlace");

                String prnShortQty = jsonObject.getString("PRNShortQty");
                Log.i("prnShortQty : ", "prnShortQty");
                TextView pkgsNoTextView;

                if (!prnShortQty.equals("0")) {
                    pkgsNoTextView = createTextView(prnShortQty);
                } else {
                    pkgsNoTextView = createTextView(jsonObject.getString("PkgsNo"));
                }

                EditText differentQtyEditText = new EditText(MainActivity7.this);
                differentQtyEditText.setTag("differentQty");
                differentQtyEditText.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                differentQtyEditText.setEnabled(false);
                if (!prnShortQty.equals("0")) {
                    differentQtyEditText.setText(prnShortQty);
                } else {
                    differentQtyEditText.setText("0");
                }

                EditText reasonEditText = new EditText(MainActivity7.this);
                reasonEditText.setTag("reason");
                reasonEditText.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                if (!prnShortQty.equals("0")) {
                    reasonEditText.setText(jsonObject.getString("reason"));
                } else {
                    reasonEditText.setText(R.string.ok);
                }
                reasonEditText.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
                reasonEditText.setEnabled(false);

                // Creating new TextViews and EditTexts for TotalBagQty and TotalBoxQty
                TextView totalBagQtyTextView = createTextView("Bag ");
                EditText totalBagQtyEditText = new EditText(MainActivity7.this);
                totalBagQtyEditText.setTag("totalBagQty");
                if (!prnShortQty.equals("0")) {
                    totalBagQtyEditText.setText("0");
                } else {
                    totalBagQtyEditText.setText(jsonObject.getString("TotalBagQty"));
                }
                totalBagQtyEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                totalBagQtyEditText.setEnabled(false);

                TextView totalBoxQtyTextView = createTextView("Box ");
                EditText totalBoxQtyEditText = new EditText(MainActivity7.this);
                totalBoxQtyEditText.setTag("totalBoxQty");
                if (!prnShortQty.equals("0")) {
                    totalBoxQtyEditText.setText("0");
                } else {
                    totalBoxQtyEditText.setText(jsonObject.getString("TotalBoxQty"));
                }
                totalBoxQtyEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                totalBoxQtyEditText.setEnabled(false);

                totalBagQtyEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        calculateDifferentQty(pkgsNoTextView, totalBagQtyEditText, totalBoxQtyEditText, differentQtyEditText, reasonEditText);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                totalBoxQtyEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        calculateDifferentQty(pkgsNoTextView, totalBagQtyEditText, totalBoxQtyEditText, differentQtyEditText, reasonEditText);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                // Creating a LinearLayout to hold TotalBagQty and TotalBoxQty views
                LinearLayout extraInfoLayout = new LinearLayout(MainActivity7.this);
                extraInfoLayout.setOrientation(LinearLayout.HORIZONTAL);
                extraInfoLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                extraInfoLayout.addView(totalBagQtyTextView);
                extraInfoLayout.addView(totalBagQtyEditText);
                extraInfoLayout.addView(totalBoxQtyTextView);
                extraInfoLayout.addView(totalBoxQtyEditText);

                row.addView(rowCheckBox);
                row.addView(lrNoTextView);
                row.addView(lrDateTextView);
                row.addView(toPlaceTextView);
                row.addView(pkgsNoTextView);
                row.addView(extraInfoLayout);
                row.addView(differentQtyEditText);
                row.addView(reasonEditText);
                tableLayoutActivitySeven.addView(row);

                // Set the OnClickListener for the CheckBox
                rowCheckBox.setOnClickListener(v -> {
                    boolean isChecked = ((CheckBox) v).isChecked();
                    totalBagQtyEditText.setEnabled(isChecked);
                    totalBoxQtyEditText.setEnabled(isChecked);
                    reasonEditText.setEnabled(isChecked);
                });
            }
        } catch (JSONException e) {
            runOnUiThread(() -> showAlert("JSON Error", "Unable to insert data into the table."));
        }
    }

    private void calculateDifferentQty(TextView pkgsNoTextView, EditText totalBagQtyEditText, EditText totalBoxQtyEditText, EditText differentQtyEditText, EditText reasonEditText) {
        try {
            int pkgsNo = Integer.parseInt(pkgsNoTextView.getText().toString());
            int bagQty = Integer.parseInt(totalBagQtyEditText.getText().toString().isEmpty() ? "0" : totalBagQtyEditText.getText().toString());
            int boxQty = Integer.parseInt(totalBoxQtyEditText.getText().toString().isEmpty() ? "0" : totalBoxQtyEditText.getText().toString());

            int totalQty = (bagQty + boxQty);
            int differentQty = (pkgsNo - totalQty);

            differentQtyEditText.setText(String.valueOf(differentQty));

            // Set reason based on differentQty
            String reason = (differentQty == 0) ? "OK" : "MISSMATCH";
            reasonEditText.setText(reason);

        } catch (NumberFormatException e) {
            differentQtyEditText.setText("0");
            reasonEditText.setText("");
        }
    }

    private boolean extractDataToJson() {
        JSONArray jsonArray = new JSONArray();
        int rowCount = tableLayoutActivitySeven.getChildCount();
        boolean hasEmptyReason = false;

        for (int i = 1; i < rowCount; i++) { // Start from 1 to skip header row
            TableRow row = (TableRow) tableLayoutActivitySeven.getChildAt(i);

            // Extract data from views in the row
            TextView lrNoTextView = (TextView) row.getChildAt(1);
            TextView lrDateTextView = (TextView) row.getChildAt(2);
            TextView toPlaceTextView = (TextView) row.getChildAt(3);
            TextView pkgsNoTextView = (TextView) row.getChildAt(4);
            LinearLayout extraInfoLayout = (LinearLayout) row.getChildAt(5);

            EditText totalBagQtyEditText = (EditText) extraInfoLayout.getChildAt(1);
            EditText totalBoxQtyEditText = (EditText) extraInfoLayout.getChildAt(3);

            EditText differentQtyEditText = (EditText) row.getChildAt(6);
            EditText reasonEditText = (EditText) row.getChildAt(7);

            // Check if reasonEditText is empty
            if (reasonEditText.getText().toString().trim().isEmpty()) {
                hasEmptyReason = true;

                runOnUiThread(() -> {
                    reasonEditText.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(reasonEditText, InputMethodManager.SHOW_IMPLICIT);
                });
                break;
            }

            try {
                // Create a new JSON object for the row
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("LRNO", lrNoTextView.getText().toString());
                jsonObject.put("LRDate", lrDateTextView.getText().toString());
                jsonObject.put("ToPlace", toPlaceTextView.getText().toString());
                jsonObject.put("PkgsNo", pkgsNoTextView.getText().toString());
                jsonObject.put("DifferentQty", differentQtyEditText.getText().toString());
                jsonObject.put("Reason", reasonEditText.getText().toString());
                jsonObject.put("TotalBagQty", totalBagQtyEditText.getText().toString());
                jsonObject.put("TotalBoxQty", totalBoxQtyEditText.getText().toString());

                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                runOnUiThread(() -> showAlert("JSON Error", "Unable to extract the data from the table."));
            }
        }

        if (hasEmptyReason) {
            showWarning("Empty Field Warning", "Please fill the reason.");
            return false;
        } else {
            Log.d("JSON Data", jsonArray.toString());
            sendJsonToServer(jsonArray);
            return true;
        }
    }

    private void sendJsonToServer(JSONArray jsonArray) {
        runOnUiThread(() -> {
            blockingView.setVisibility(View.VISIBLE);
            lottieAnimationView.setVisibility(View.VISIBLE);
            lottieAnimationView.playAnimation();
        });

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        RequestBody body = RequestBody.create(jsonArray.toString(), MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder().url("https://vtc3pl.com/fetch_bag_box_weight_only_for_single_lrno.php").post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    lottieAnimationView.setVisibility(View.GONE);
                    blockingView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();
                    showAlert("Network Error", "Please check your internet.");
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
                        Log.e("Response : ", responseBody);
                        try {
                            JSONObject jsonObject = new JSONObject(responseBody);

                            double totalBoxWeight = jsonObject.getDouble("TotalBoxWeight");
                            double totalBoxQty = jsonObject.getDouble("TotalBoxQty");
                            double totalBagWeight = jsonObject.getDouble("TotalBagWeight");
                            double totalBagQty = jsonObject.getDouble("TotalBagQty");

                            totalBoxWeightFromAllLRNO = totalBoxWeight;
                            totalBagWeightFromAllLRNO = totalBagWeight;
                            totalBoxQtyFromAllLRNO = totalBoxQty;
                            totalBagQtyFromAllLRNO = totalBagQty;

                            runOnUiThread(() -> {
                                Log.i("New", "This data is coming from New Logic");
                                Log.d("totalBoxWeight : ", String.valueOf(totalBoxWeightFromAllLRNO));
                                Log.d("totalBagWeight : ", String.valueOf(totalBagWeightFromAllLRNO));
                                Log.d("totalBoxQty : ", String.valueOf(totalBoxQtyFromAllLRNO));
                                Log.d("totalBagQty : ", String.valueOf(totalBagQtyFromAllLRNO));
                            });

                        } catch (JSONException e) {
                            runOnUiThread(() -> showAlert("JSON Error", "Error parsing JSON response"));
                        }
                    }
                } else {
                    runOnUiThread(() -> showAlert("Server Error", "Server error: " + response.code()));
                }
            }
        });
    }

    private void disableComponents() {
        int rowCount = tableLayoutActivitySeven.getChildCount();

        for (int i = 1; i < rowCount; i++) { // Start from 1 to skip header row
            TableRow row = (TableRow) tableLayoutActivitySeven.getChildAt(i);

            // Disable CheckBox
            CheckBox rowCheckBox = (CheckBox) row.getChildAt(0);
            rowCheckBox.setEnabled(false);

            // Disable EditTexts
            LinearLayout extraInfoLayout = (LinearLayout) row.getChildAt(5);

            EditText totalBagQtyEditText = (EditText) extraInfoLayout.getChildAt(1);
            EditText totalBoxQtyEditText = (EditText) extraInfoLayout.getChildAt(3);

            EditText differentQtyEditText = (EditText) row.getChildAt(6);
            EditText reasonEditText = (EditText) row.getChildAt(7);

            totalBagQtyEditText.setEnabled(false);
            totalBoxQtyEditText.setEnabled(false);
            differentQtyEditText.setEnabled(false);
            reasonEditText.setEnabled(false);
        }
    }

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

            // Extract data using the tags
            String lrNo = ((TextView) row.findViewWithTag("lrNo")).getText().toString().trim();
            String lrDate = ((TextView) row.findViewWithTag("lrDate")).getText().toString().trim();
            String toPlace = ((TextView) row.findViewWithTag("toPlace")).getText().toString().trim();
            String pkgsNo = ((TextView) row.findViewWithTag("pkgsNo")).getText().toString().trim();
            String totalBagQty = ((EditText) row.findViewWithTag("totalBagQty")).getText().toString().trim();
            String totalBoxQty = ((EditText) row.findViewWithTag("totalBoxQty")).getText().toString().trim();
            String differentQty = ((EditText) row.findViewWithTag("differentQty")).getText().toString().trim();
            String reason = ((EditText) row.findViewWithTag("reason")).getText().toString().trim();

            // Calculate the receivedQty as the sum of bags and box quantities
            String receivedQty = String.valueOf(Integer.parseInt(totalBagQty.isEmpty() ? "0" : totalBagQty) + Integer.parseInt(totalBoxQty.isEmpty() ? "0" : totalBoxQty));

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
                runOnUiThread(() -> {
                    lottieAnimationView.setVisibility(View.GONE);
                    blockingView.setVisibility(View.GONE);
                    lottieAnimationView.cancelAnimation();
                    showAlert("JSON Error", "Unable to insert data into the table.");
                });
            }
        }

        // Print the table data in list:
        Log.d("TablLayoutData", rowDataList.toString());

        Object selectedItem = hamaliVendorNameSpinnerActivitySeven.getSelectedItem();
        if (selectedItem == null || selectedItem.toString().equals("Please Select Vendor")) {
            runOnUiThread(() -> {
                lottieAnimationView.setVisibility(View.GONE);
                blockingView.setVisibility(View.GONE);
                lottieAnimationView.cancelAnimation();
                showWarning("Unselected Field Warning", "Please select hamali vendor name.");
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

        if (radioGroupOptions.getCheckedRadioButtonId() == -1) {
            runOnUiThread(() -> {
                lottieAnimationView.setVisibility(View.GONE);
                blockingView.setVisibility(View.GONE);
                lottieAnimationView.cancelAnimation();
                showWarning("Unselected Radio Button Warning", "Please select any one radio button.");
            });
            return;
        }

        if (selectedRadioButton == null || selectedRadioButton.isEmpty()) {

            runOnUiThread(() -> {
                lottieAnimationView.setVisibility(View.GONE);
                blockingView.setVisibility(View.GONE);
                lottieAnimationView.cancelAnimation();
                showWarning("Unselected Radio Button Warning", "Please select any one radio button.");
            });
            return;
        }

        if (freightAmount.isEmpty()) {

            runOnUiThread(() -> {
                lottieAnimationView.setVisibility(View.GONE);
                blockingView.setVisibility(View.GONE);
                lottieAnimationView.cancelAnimation();

                showWarning("Empty Field Warning", "Please enter freight amount.");
                freightEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(freightEditText, InputMethodManager.SHOW_IMPLICIT);

            });
            return;
        }

        if (godownKeeperName.isEmpty()) {

            runOnUiThread(() -> {
                lottieAnimationView.setVisibility(View.GONE);
                blockingView.setVisibility(View.GONE);
                lottieAnimationView.cancelAnimation();

                showWarning("Empty Field Warning", "Please enter godown keeper name.");
                godownKeeperNameEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(godownKeeperNameEditText, InputMethodManager.SHOW_IMPLICIT);
            });
            return;
        }

        if (hamaliVendor.equals("Please Select Vendor")) {

            runOnUiThread(() -> {
                lottieAnimationView.setVisibility(View.GONE);
                blockingView.setVisibility(View.GONE);
                lottieAnimationView.cancelAnimation();
                showWarning("Unselected Field Warning", "Please select hamali vendor name.");
            });
            return;
        }

        if (amountPaidToHVendor.isEmpty() || hamaliAmount.isEmpty()) {

            runOnUiThread(() -> {
                lottieAnimationView.setVisibility(View.GONE);
                blockingView.setVisibility(View.GONE);
                lottieAnimationView.cancelAnimation();
                showWarning("Empty Field Warning", "Amount is empty.");
            });
            return;
        }

        if (deductionAmount.isEmpty()) {
            deductionAmount = "0";
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

                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            String status = jsonResponse.getString("status");
                            String message = jsonResponse.getString("message");

                            runOnUiThread(() -> {
                                if ("success".equals(status)) {
                                    Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.success);
                                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 32, 32, true);
                                    Drawable successIcon = new BitmapDrawable(getResources(), scaledBitmap);

                                    final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity7.this).setTitle("Success").setMessage(message).setPositiveButton("OK", (dialog, which) -> {
                                        dialog.dismiss();
                                        clearUIComponents();
                                    }).setIcon(successIcon).create();
                                    alertDialog.setOnDismissListener(dialog -> {
                                        dialog.dismiss();
                                        clearUIComponents();
                                    });

                                    alertDialog.show();
                                } else {
                                    runOnUiThread(() -> showAlert("Error", message));
                                }
                            });
                        } catch (JSONException e) {
                            runOnUiThread(() -> showAlert("Parsing Error", "Error parsing response: " + e.getMessage()));
                        }
                    } else {
                        runOnUiThread(() -> showAlert("Empty Response Error", "Empty response received from server"));
                    }
                } else {
                    runOnUiThread(() -> showAlert("Server Error", "Server error: " + response.code()));
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