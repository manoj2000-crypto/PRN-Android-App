package com.vtc3pl.prnapp2024v2;
//Login Page (1st Page)

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private EditText userNameEditText, passwordEditText;
    //    private Spinner spinnerDepo, spinnerYear;
    private Button loginButton;
    private CheckBox rememberLoginCheckBox;
    private String depo = "", year = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        userNameEditText = findViewById(R.id.userName);
        passwordEditText = findViewById(R.id.userPassword);

        // Calculate the year
        calculateYear();

//        spinnerDepo = findViewById(R.id.spinnerDepo);
//        spinnerYear = findViewById(R.id.spinnerYear);
        loginButton = findViewById(R.id.loginButton);
        rememberLoginCheckBox = findViewById(R.id.rememberLoginCheckBox);

        // Load login state
        SharedPreferences preferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
        boolean rememberLogin = preferences.getBoolean("remember_login", false);
        rememberLoginCheckBox.setChecked(rememberLogin);

        // Populate saved username and password if Remember Login is checked
        if (rememberLogin) {
            String savedUsername = preferences.getString("username", "");
            String savedPassword = preferences.getString("password", "");
            userNameEditText.setText(savedUsername);
            passwordEditText.setText(savedPassword);
        }

//        year = 2425 complete this , it should take by default current year if year = 2024 then take 2425 , if it is 2025 then take 2526
//        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Please Select Year", "2122", "2223", "2324", "2425", "2526", "2627", "2728", "2829", "2930"});
//        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerYear.setAdapter(yearAdapter);

//        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                // Handle item selection
//                String selectedYear = (String) parent.getItemAtPosition(position);
//                Toast.makeText(MainActivity.this, "Selected year: " + selectedYear, Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validateInputs()) {

                    fetchDepoFromUserName();

                    performLogin();

                    // Save login state if checkbox is checked
                    if (rememberLoginCheckBox.isChecked()) {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("remember_login", true);
                        editor.putString("username", userNameEditText.getText().toString().trim());
                        editor.putString("password", passwordEditText.getText().toString().trim());
                        editor.apply();
                    }
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private boolean validateInputs() {
        String username = userNameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
//        String depo = spinnerDepo.getSelectedItem().toString();
//        String year = spinnerYear.getSelectedItem().toString();

        if (username.isEmpty() || password.isEmpty()) { //|| depo.equals("Please Select Depo") || year.equals("Please Select Year")) {
            Toast.makeText(MainActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void performLogin() {
        // Construct the URL for the login API endpoint
        String loginUrl = "https://vtc3pl.com/attloginprn.php";

        // Get inputs
        String username = userNameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
//        String depo = spinnerDepo.getSelectedItem().toString();
//        String year = spinnerYear.getSelectedItem().toString();

        // Prepare the request body
        RequestBody formBody = new FormBody.Builder().add("user_name", username).add("password", password).build();

        // Create the request
        Request request = new Request.Builder().url(loginUrl).post(formBody).build();

        // Create OkHttpClient instance with a timeout
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).build();

        // Execute the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (e instanceof SocketTimeoutException || e instanceof ConnectTimeoutException) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Request timed out", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Failed to connect to server", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseData = response.body().string();
                Log.d("Response Data : ", responseData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (responseData.equals("Success")) {
                            Intent intent = new Intent(MainActivity.this, MainActivity4.class);
                            intent.putExtra("username", username);
                            intent.putExtra("depo", depo);
                            intent.putExtra("year", year);
                            startActivity(intent);
                            finish();
                            Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Login failed: " + responseData, Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
    }

    private void fetchDepoFromUserName() {
        OkHttpClient client = new OkHttpClient();
//        Request request = new Request.Builder().url("https://vtc3pl.com/Fetch_DepoCode_PRN_APP.php").build();

        String userName = userNameEditText.getText().toString().trim();

        // Create the form body
        RequestBody formBody = new FormBody.Builder().add("username", userName).build();

        Request request = new Request.Builder().url("https://vtc3pl.com/fetch_depot_from_username_prn_app.php").post(formBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (e instanceof UnknownHostException) {
                            Toast.makeText(MainActivity.this, "Unable to connect to server. Please check your internet connection.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Failed to connect to server", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseData = response.body().string();
                Log.d("Response Data", responseData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(responseData);
                            if (jsonObject.has("depotCode")) {
                                Log.d("DepotCodeFromUserName", jsonObject.getString("depotCode"));
                                depo = jsonObject.getString("depotCode");
                                Toast.makeText(MainActivity.this, "Depot Code: " + depo, Toast.LENGTH_SHORT).show();
                            } else if (jsonObject.has("error")) {
                                String error = jsonObject.getString("error");
                                Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Unexpected response format", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void calculateYear() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        Log.e("CurrentYear", String.valueOf(currentYear));
        int nextYear = (currentYear % 100) + 1;
        Log.e("nextYear", String.valueOf(nextYear));
        year = String.format(Locale.getDefault(), "%02d%02d", currentYear % 100, nextYear);
        Log.e("CalculatedYear", year);
    }

}