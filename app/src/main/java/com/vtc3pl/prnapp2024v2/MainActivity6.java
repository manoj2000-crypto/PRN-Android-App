package com.vtc3pl.prnapp2024v2;
//Arrival Page Part 1

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity6 extends AppCompatActivity {

    private EditText editTextFromDateActivitySix, editTextToDateActivitySix, prnNumberEditText;
    private TextView textViewFromDateActivitySix, textViewToDateActivitySix, prnNumberTextView;
    private Calendar fromCalendar, toCalendar;
    private DatePickerDialog.OnDateSetListener fromDateSetListener, toDateSetListener;
    private Button searchButton;
    private String selectedRadioButton = "", username = "", depo = "", year = "";
    private TableLayout tableLayout;
    private LottieAnimationView lottieAnimationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main6);

        textViewFromDateActivitySix = findViewById(R.id.textViewFromDateActivitySix);
        editTextFromDateActivitySix = findViewById(R.id.editTextFromDateActivitySix);

        textViewToDateActivitySix = findViewById(R.id.textViewToDateActivitySix);
        editTextToDateActivitySix = findViewById(R.id.editTextToDateActivitySix);

        prnNumberTextView = findViewById(R.id.prnNumberTextView);
        prnNumberEditText = findViewById(R.id.prnNumberEditText);
        prnNumberEditText.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        searchButton = findViewById(R.id.searchButton);

        tableLayout = findViewById(R.id.tableLayoutActivitySix);
        lottieAnimationView = findViewById(R.id.lottieAnimationView);

        fromCalendar = Calendar.getInstance();
        Log.e("fromCalendar at Start", String.valueOf(fromCalendar));
        toCalendar = Calendar.getInstance();
        Log.e("toCalendar at Start", String.valueOf(toCalendar));

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
            depo = intent.getStringExtra("depo");
            year = intent.getStringExtra("year");
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

        setupRadioButtons();

        searchButton.setOnClickListener(v -> {

            runOnUiThread(() -> {
                lottieAnimationView.setVisibility(View.VISIBLE);
                lottieAnimationView.playAnimation();
            });

            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

            String url = "https://vtc3pl.com/arrival_prn_search_prn_app.php";

            Log.e("usernme", username);
            Log.e("depo", depo);
            Log.e("year", year);
            Log.e("Selected ", selectedRadioButton);

            FormBody.Builder formBuilder = new FormBody.Builder();
            formBuilder.add("username", username);
            formBuilder.add("depo", depo);
            formBuilder.add("year", year);
            formBuilder.add("selectedRadioButton", selectedRadioButton);
            if (selectedRadioButton.equals("radioButton1")) {
                String fromDate = editTextFromDateActivitySix.getText().toString().trim();
                String toDate = editTextToDateActivitySix.getText().toString().trim();

                if (fromDate.isEmpty()) {
                    runOnUiThread(() -> {
                        lottieAnimationView.setVisibility(View.GONE);
                        lottieAnimationView.cancelAnimation();
                    });

                    showWarning("Empty From-Date Warning", "From Date is required");
                    editTextFromDateActivitySix.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editTextFromDateActivitySix, InputMethodManager.SHOW_IMPLICIT);
                    return;
                }

                if (toDate.isEmpty()) {
                    runOnUiThread(() -> {
                        lottieAnimationView.setVisibility(View.GONE);
                        lottieAnimationView.cancelAnimation();
                    });

                    showWarning("Empty To-Date Warning", "To Date is required");
                    editTextToDateActivitySix.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editTextToDateActivitySix, InputMethodManager.SHOW_IMPLICIT);
                    return;
                }

                formBuilder.add("fromDate", fromDate);
                formBuilder.add("toDate", toDate);
            } else if (selectedRadioButton.equals("radioButton2")) {
                String prnNumber = prnNumberEditText.getText().toString().trim();

                if (prnNumber.isEmpty()) {
                    runOnUiThread(() -> {
                        lottieAnimationView.setVisibility(View.GONE);
                        lottieAnimationView.cancelAnimation();
                    });

                    showWarning("Empty PRN Warning", "PRN Number is required");
                    prnNumberEditText.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(prnNumberEditText, InputMethodManager.SHOW_IMPLICIT);
                    return;
                }

                formBuilder.add("prnNumber", prnNumber);
            }

            Request request = new Request.Builder().url(url).post(formBuilder.build()).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        lottieAnimationView.setVisibility(View.GONE);
                        lottieAnimationView.cancelAnimation();
                        showAlert("Connection Failed", "Failed to connect to server.");
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
                            String responseData = body.string();
                            try {
                                JSONArray jsonArray = new JSONArray(responseData);
                                Log.e("response on success", String.valueOf(jsonArray));
                                runOnUiThread(() -> {
                                    if (jsonArray.length() > 0) {
                                        displayDataInTable(jsonArray);
                                    } else {
                                        showAlert("Empty Response", "No data available");
                                    }
                                });
                            } catch (JSONException e) {
                                runOnUiThread(() -> showAlert("Response Error", "Wrong response received from server"));
                            }
                        } else {
                            runOnUiThread(() -> showAlert("Empty Response", "Empty response is received from server"));
                        }
                    } else {
                        runOnUiThread(() -> showAlert("Response Error", "Unsuccessful response: " + response));
                    }
                }
            });
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
        editTextFromDateActivitySix.setText(android.text.format.DateFormat.format(dateFormat, fromCalendar));
    }

    private void updateToDate() {
        String dateFormat = "yyyy/MM/dd";
        editTextToDateActivitySix.setText(android.text.format.DateFormat.format(dateFormat, toCalendar));
    }

    private void setupRadioButtons() {
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        RadioButton radioButtonDate = findViewById(R.id.radioButtonDate);
        RadioButton radioButtonPRN = findViewById(R.id.radioButtonPRN);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == radioButtonDate.getId()) {
                showDateRangeViews();
                adjustSearchButtonPosition(editTextToDateActivitySix);
                selectedRadioButton = "radioButton1";
                Log.d("If RadioButton Value:", selectedRadioButton + " , checkId = " + checkedId);
            } else if (checkedId == radioButtonPRN.getId()) {
                showPRNViews();
                adjustSearchButtonPosition(prnNumberEditText);
                selectedRadioButton = "radioButton2";
                Log.d("else RadioButton Value:", selectedRadioButton + " , checkId = " + checkedId);
            }
        });
    }

    private void showDateRangeViews() {
        updateFromDate();
        updateToDate();
        textViewFromDateActivitySix.setVisibility(View.VISIBLE);
        editTextFromDateActivitySix.setVisibility(View.VISIBLE);
        textViewToDateActivitySix.setVisibility(View.VISIBLE);
        editTextToDateActivitySix.setVisibility(View.VISIBLE);

        prnNumberTextView.setVisibility(View.GONE);
        prnNumberEditText.setVisibility(View.GONE);
    }

    private void showPRNViews() {
        textViewFromDateActivitySix.setVisibility(View.GONE);
        editTextFromDateActivitySix.setVisibility(View.GONE);
        textViewToDateActivitySix.setVisibility(View.GONE);
        editTextToDateActivitySix.setVisibility(View.GONE);

        prnNumberTextView.setVisibility(View.VISIBLE);
        prnNumberEditText.setVisibility(View.VISIBLE);
    }

    private void adjustSearchButtonPosition(View anchorView) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) searchButton.getLayoutParams();
        params.topToBottom = anchorView.getId();
        searchButton.setLayoutParams(params);
        searchButton.setVisibility(View.VISIBLE);
    }

    private void displayDataInTable(JSONArray jsonArray) {
        // Clear existing table rows
        tableLayout.removeAllViews();

        // Create table headers
        TableRow headerRow = new TableRow(MainActivity6.this);
        headerRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView srNoHeader = createHeaderTextView("SrNo");
        headerRow.addView(srNoHeader);

        TextView prnNoHeader = createHeaderTextView("PRN No");
        headerRow.addView(prnNoHeader);

        TextView prnDateHeader = createHeaderTextView("PRN Date");
        headerRow.addView(prnDateHeader);

        TextView vehicleNoHeader = createHeaderTextView("Vehicle No");
        headerRow.addView(vehicleNoHeader);

        TextView locationHeader = createHeaderTextView("Location");
        headerRow.addView(locationHeader);

        TextView updateStockHeader = createHeaderTextView("Update Stock");
        headerRow.addView(updateStockHeader);

        tableLayout.addView(headerRow, 0);

        // Iterate through JSON array and add rows to the table
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                final String prnId = jsonObject.getString("PRNId");
                String avDate = jsonObject.getString("AVDate");
                String vehicleNo = jsonObject.getString("VehicleNo");
                String depo = jsonObject.getString("Depo");

                TableRow row = new TableRow(MainActivity6.this);
                row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                TextView srNo = createTextView(String.valueOf(i + 1));
                row.addView(srNo);

                TextView prnNo = createTextView(prnId);
                row.addView(prnNo);

                TextView prnDate = createTextView(avDate);
                row.addView(prnDate);

                TextView vehicle = createTextView(vehicleNo);
                row.addView(vehicle);

                TextView location = createTextView(depo);
                row.addView(location);

                Button updateButton = new Button(MainActivity6.this);
                updateButton.setText(R.string.arrival);
                updateButton.setPadding(10, 5, 10, 5);

                // Set OnClickListener for the update button
                updateButton.setOnClickListener(v -> {

                    runOnUiThread(() -> {
                        lottieAnimationView.setVisibility(View.VISIBLE);
                        lottieAnimationView.playAnimation();
                    });

                    RequestBody requestBody = new FormBody.Builder().add("prnId", prnId).build();

                    Request request = new Request.Builder().url("https://vtc3pl.com/get_all_lrno_from_prn_number.php").post(requestBody).build();

                    OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            runOnUiThread(() -> {
                                lottieAnimationView.setVisibility(View.GONE);
                                lottieAnimationView.cancelAnimation();
                                showAlert("Connection Failed", "Failed to connect to server.");
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
                                    String responseData = body.string();
                                    //GETTING ERROR HERE AS WRONG RESPONSE .
                                    Log.i("WRONG RESPONSE :", "Wrong response received from server : " + responseData);
                                    try {
                                        JSONArray jsonArray1 = new JSONArray(responseData);
                                        ArrayList<String> lrnoList = new ArrayList<>();
                                        for (int i1 = 0; i1 < jsonArray1.length(); i1++) {
                                            JSONObject jsonObject1 = jsonArray1.getJSONObject(i1);
                                            String lrno = jsonObject1.getString("LRNO").trim();
                                            lrnoList.add(lrno);
                                        }

                                        String[] lrnoArray = new String[lrnoList.size()];
                                        lrnoArray = lrnoList.toArray(lrnoArray);
                                        Log.e("lrnoArray Only : ", Arrays.toString(lrnoArray));

                                        Intent intent = new Intent(MainActivity6.this, MainActivity7.class);
                                        intent.putExtra("prnId", prnId);
                                        intent.putExtra("depo", depo);
                                        intent.putExtra("year", year);
                                        intent.putExtra("username", username);
                                        intent.putExtra("response", responseData);
                                        intent.putExtra("lrnoArray", lrnoArray);
                                        startActivity(intent);

                                    } catch (JSONException e) {
                                        runOnUiThread(() -> showAlert("Response Error", "Wrong response received from server"));
                                    }
                                } else {
                                    runOnUiThread(() -> showAlert("Empty Response Error", "Empty response received from server"));
                                }
                            } else {
                                runOnUiThread(() -> showAlert("Response Error", "Unsuccessful response: " + response));
                            }
                        }
                    });
                });

                row.addView(updateButton);

                tableLayout.addView(row);
            } catch (JSONException e) {
                runOnUiThread(() -> {
                    showAlert("Table Error", "Table Creation error");
                    Log.e("TableCreation Excep: ", String.valueOf(e));
                });
            }
        }
    }

    private TextView createHeaderTextView(String text) {
        TextView textView = new TextView(MainActivity6.this);
        textView.setText(text);
        textView.setTypeface(null, Typeface.BOLD); // Set text to bold
        textView.setPadding(10, 10, 10, 10); // Padding
        return textView;
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(MainActivity6.this);
        textView.setText(text);
        textView.setPadding(10, 10, 10, 10); // Padding
        return textView;
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

        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
        }).setIcon(warningIcon).setCancelable(false).show();
    }

}