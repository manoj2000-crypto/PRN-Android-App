package com.vtc3pl.prnapp2024v2;
//Login Page (1st Page)

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    private final String appVersion = "versionSix";
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
        userNameEditText.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
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

                    loginButton.setEnabled(false);

                    Log.e("OnClick LoginButton", depo);

                    if (depo.isEmpty()) {
                        showWarning("Empty Depot Warning", "Please login again.");
                        loginButton.setEnabled(true);
                    } else {
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
        RequestBody formBody = new FormBody.Builder().add("user_name", username).add("password", password).add("appVersion", appVersion).build();

        // Create the request
        Request request = new Request.Builder().url(loginUrl).post(formBody).build();

        // Create OkHttpClient instance with a timeout
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        // Execute the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (e instanceof SocketTimeoutException || e instanceof ConnectTimeoutException) {
                            showAlert("Time Out Error", "Request timed out due to slow network connection.");
                        } else {
                            showAlert("Connection Failed", "Failed to connect to server");
                        }
                        loginButton.setEnabled(true);
                    }
                });
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

                            // Load the original image
                            Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.success);

                            // Scale the image to the desired size
                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 32, 32, true);

                            // Create a Drawable from the scaled Bitmap
                            Drawable successIcon = new BitmapDrawable(getResources(), scaledBitmap);

//                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//                            builder.setTitle("Success").setMessage("Login successful").setPositiveButton("OK", (dialog, which) -> {
//                                dialog.dismiss();
//                                startActivity(intent);
//                                finish();
//                            }).setIcon(successIcon).show();

                            final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Success")
                                    .setMessage("Login successful")
                                    .setPositiveButton("OK", (dialog, which) -> {
                                        dialog.dismiss();
                                        startActivity(intent);
                                        finish();
                                    })
                                    .setIcon(successIcon)
                                    .create();
                            alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    dialog.dismiss();
                                    startActivity(intent);
                                    finish();
                                }
                            });

                            alertDialog.show();
                        } else {
                            showAlert("Login Failed", "Login failed : " + responseData);
                        }
                        loginButton.setEnabled(true);
                    }
                });
            }
        });
    }

    private void fetchDepoFromUserName() {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

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
                            showAlert("Connection Failed", "Unable to connect to server. Please check your internet connection.");
                        } else {
                            showAlert("Connection Failed", "Failed to connect to server");
                        }
                        loginButton.setEnabled(true);
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        String responseData = body.string();
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
                                        showAlert("Response Error", "Unexpected response format");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    showAlert("Error", "Wrong response received from server");
                                }
                                loginButton.setEnabled(true);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showAlert("Empty Response Error", "Empty response received from server");
                                loginButton.setEnabled(true);
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showAlert("Server Error", "Server error: " + response.code());
                            loginButton.setEnabled(true);
                        }
                    });
                }
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