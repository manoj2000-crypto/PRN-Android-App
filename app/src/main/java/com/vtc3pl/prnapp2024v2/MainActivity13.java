package com.vtc3pl.prnapp2024v2;
// Edit PRN

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
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.HashSet;
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

public class MainActivity13 extends AppCompatActivity {

    private static final Pattern LR_NUMBER_PATTERN = Pattern.compile("[A-Z]{3,4}[0-9]{10}+");
    private final Set<String> lrNumbersSet = new HashSet<>();
    private String username = "", depo = "", year = "";
    private TextView showUserNameActivityThirteenTextView, prnNumberActivityThirteenTextView;
    private EditText editTextPRNActivityThirteen, hamaliAmountEditTextActivityThirteen, amountPaidToHVendorEditTextActivityThirteen, deductionAmountEditTextActivityThirteen, lrEditTextActivityThirteen;
    private Button searchPRNButtonActivityThirteen, addButtonActivityThirteen;
    private ScrollView scrollViewActivityThirteen;

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
        addButtonActivityThirteen = findViewById(R.id.addButtonActivityThirteen);

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
                scrollViewActivityThirteen.setVisibility(View.VISIBLE);
            } else {
                showAlert("Empty Field", "PRN Number is empty.");
            }
        });

        addButtonActivityThirteen.setOnClickListener(v -> addRowToTable());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void addRowToTable() {
        String lrNumber = editTextPRNActivityThirteen.getText().toString().trim();
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
//                            runOnUiThread(() -> addLRNumberToTable(lrNumber));
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