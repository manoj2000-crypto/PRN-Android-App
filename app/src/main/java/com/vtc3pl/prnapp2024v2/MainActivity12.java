package com.vtc3pl.prnapp2024v2;
// PRN Cancel

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity12 extends AppCompatActivity {

    private String username = "", depo = "", year = "";
    private TextView showUserNameActivityTwelveTextView, prnNumberTextViewShowValue, prnDateTextViewShow, vehicleNoTextViewShow, godownTextViewShow;
    private EditText editTextPRN, reasonEditText;
    private Button searchPRNButton, cancelPRNButton;
    private ConstraintLayout detailsContainer;
    private OkHttpClient client;
    private boolean isActive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main12);

        client = new OkHttpClient();

        showUserNameActivityTwelveTextView = findViewById(R.id.showUserNameActivityTwelveTextView);
        editTextPRN = findViewById(R.id.editTextPRN);
        editTextPRN.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        prnNumberTextViewShowValue = findViewById(R.id.prnNumberTextViewShowValue);
        prnDateTextViewShow = findViewById(R.id.prnDateTextViewShow);
        vehicleNoTextViewShow = findViewById(R.id.vehicleNoTextViewShow);
        godownTextViewShow = findViewById(R.id.godownTextViewShow);
        searchPRNButton = findViewById(R.id.searchPRNButton);
        cancelPRNButton = findViewById(R.id.cancelPRNButton);
        reasonEditText = findViewById(R.id.reasonEditText);
        reasonEditText.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        detailsContainer = findViewById(R.id.detailsContainer);

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
            depo = intent.getStringExtra("depo");
            year = intent.getStringExtra("year");

            if (username != null) {
                String usernameText = getString(R.string.user_name_prefix, username);
                showUserNameActivityTwelveTextView.setText(usernameText);
            }
        }

        searchPRNButton.setOnClickListener(v -> {
            String prnId = editTextPRN.getText().toString().trim();
            if (!prnId.isEmpty()) {
                fetchData(prnId);
            } else {
                showWarning("Empty Field", "PRN Number is empty.");
            }
        });

        cancelPRNButton.setOnClickListener(v -> {
            String prnId = editTextPRN.getText().toString().trim();
            String reason = reasonEditText.getText().toString().trim();

            if (!prnId.isEmpty() && !reason.isEmpty()) {
                cancelPRN(prnId, reason);
            } else {
                showWarning("Empty Fields", "PRN Number or reason is empty.");
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActive = false;
    }

    private void fetchData(String prnId) {
        String url = "https://vtc3pl.com/fetch_prn_cancel_data.php?prnId=" + prnId;
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> showAlert("Network Error ", "Network request failed."));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    runOnUiThread(() -> {
                        updateUI(responseData);
                    });
                } else {
                    runOnUiThread(() -> showAlert("Server Error", "Server returned an error."));
                }
            }
        });
    }

    private void updateUI(String jsonResponse) {
        try {
            // Parse the JSON array
            JSONArray jsonArray = new JSONArray(jsonResponse);

            // Check if the array is not empty
            if (jsonArray.length() > 0) {
                // Get the first object
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                // Extract data from the JSON object
                String prnNumber = jsonObject.optString("PRNId", "--");
                String prnDate = jsonObject.optString("PRNDate", "--");
                String vehicleNo = jsonObject.optString("VehicleNo", "--");
                String godown = jsonObject.optString("Godown", "--");

                // Update TextViews with the extracted data
                prnNumberTextViewShowValue.setText(prnNumber);
                prnDateTextViewShow.setText(prnDate);
                vehicleNoTextViewShow.setText(vehicleNo);
                godownTextViewShow.setText(godown);

                // Make the details container visible
                detailsContainer.setVisibility(View.VISIBLE);
            } else {
                detailsContainer.setVisibility(View.GONE);
                showAlert("Error ", "No data found.");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            detailsContainer.setVisibility(View.GONE);
            showAlert("Error ", "Error parsing data.");
        }
    }

    private void cancelPRN(String prnId, String reason) {
        String url = "https://vtc3pl.com/prn_cancel_update_data_prn_app.php";
        RequestBody formBody = new FormBody.Builder().add("PRNId", prnId).add("cancelreason", reason).add("loginUser", username).build();

        Request request = new Request.Builder().url(url).post(formBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> showAlert("Network Error", "Network request failed. Please check your internet connection and try again."));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        String responseBody = body.string();
                        Log.e("Response CreatePRN:", responseBody);
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            String status = jsonResponse.getString("status");
                            runOnUiThread(() -> {
                                if (status.equals("success")) {
                                    if (isActive) { // Check if activity is still active
                                        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.success);
                                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 32, 32, true);
                                        Drawable successIcon = new BitmapDrawable(getResources(), scaledBitmap);

                                        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity12.this).setTitle("Success").setMessage("PRN successfully canceled.").setPositiveButton("OK", (dialog, which) -> {
                                            dialog.dismiss();
                                            clearUIComponents();
                                        }).setIcon(successIcon).create();

                                        alertDialog.setOnDismissListener(dialog -> {
                                            dialog.dismiss();
                                            clearUIComponents();
                                        });

                                        alertDialog.show();
                                    }
                                } else {
                                    String message = null;
                                    try {
                                        message = jsonResponse.getString("message");
                                    } catch (JSONException e) {
                                        runOnUiThread(() -> showAlert("Parsing Error", "An error occurred while parsing the server response."));
                                    }
                                    showAlert("Server Error", message);
                                }
                            });
                        } catch (JSONException e) {
                            runOnUiThread(() -> showAlert("Parsing Error", "An error occurred while parsing the server response."));
                        }
                    } else {
                        runOnUiThread(() -> showAlert("Empty Response Error", "Empty response received from server"));
                    }
                } else {
                    runOnUiThread(() -> showAlert("Server Error", "Server returned an error."));
                }
            }
        });
    }

    private void clearUIComponents() {
        editTextPRN.setText("");
        prnNumberTextViewShowValue.setText("");
        prnDateTextViewShow.setText("");
        vehicleNoTextViewShow.setText("");
        godownTextViewShow.setText("");
        reasonEditText.setText("");
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