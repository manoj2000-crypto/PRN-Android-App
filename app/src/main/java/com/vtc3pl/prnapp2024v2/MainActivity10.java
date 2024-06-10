package com.vtc3pl.prnapp2024v2;
//Godown LR page

import static android.view.Gravity.CENTER;
import static android.view.Gravity.CENTER_HORIZONTAL;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
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
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;

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

public class MainActivity10 extends AppCompatActivity {
    private static final Pattern LR_NUMBER_PATTERN = Pattern.compile("[A-Z]{3,4}[0-9]{10}+");
    private final Set<String> lrNumbersSet = new HashSet<>();
    private String username = "", depo = "", year = "";
    private TextView showUserNameTextViewActivityTen, lRNumberTextViewActivityTen, goDownTextViewActivityTen, remarkTextViewActivityTen;
    private EditText lrEditTextActivityTen, remarkEditTextActivityTen;
    private Button addButtonActivityTen, submitButtonActivityTen;
    private Spinner goDownSpinnerActivityTen;
    private TableLayout tableLayoutActivityTen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main10);

        showUserNameTextViewActivityTen = findViewById(R.id.showUserNameTextViewActivityTen);
        lRNumberTextViewActivityTen = findViewById(R.id.lRNumberTextViewActivityTen);
        goDownTextViewActivityTen = findViewById(R.id.goDownTextViewActivityTen);
        remarkTextViewActivityTen = findViewById(R.id.remarkTextViewActivityTen);

        lrEditTextActivityTen = findViewById(R.id.lrEditTextActivityTen);
        lrEditTextActivityTen.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        remarkEditTextActivityTen = findViewById(R.id.remarkEditTextActivityTen);
        remarkEditTextActivityTen.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        goDownSpinnerActivityTen = findViewById(R.id.goDownSpinnerActivityTen);

        addButtonActivityTen = findViewById(R.id.addButtonActivityTen);
        addButtonActivityTen.setOnClickListener(v -> addLrToTable());

        submitButtonActivityTen = findViewById(R.id.submitButtonActivityTen);
        submitButtonActivityTen.setOnClickListener(v -> submitDataToServer());

        tableLayoutActivityTen = findViewById(R.id.tableLayoutActivityTen);

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
            depo = intent.getStringExtra("depo");
            year = intent.getStringExtra("year");

            // Set the fetched username to the TextView
            if (username != null) {
                String usernameText = getString(R.string.user_name_prefix, username);
                showUserNameTextViewActivityTen.setText(usernameText);
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void addLrToTable() {
        String lrNumber = lrEditTextActivityTen.getText().toString().trim();

        if (lrNumber.isEmpty()) {
            showAlert("Error", "LR number cannot be empty");
        } else if (!LR_NUMBER_PATTERN.matcher(lrNumber).matches()) {
            showAlert("Error", "LR number format is invalid");
        } else if (lrNumbersSet.contains(lrNumber)) {
            showAlert("Error", "LR number is a duplicate");
        } else {
            checkLRNumberOnServer(lrNumber);
        }
    }

    private void checkLRNumberOnServer(String lrNumber) {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("lrNumber", lrNumber);
        formBuilder.add("depo", depo);

        Request request = new Request.Builder().url("https://vtc3pl.com/prn_app_get_lrno_for_godown.php").post(formBuilder.build()).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    showAlert("Connection failed", "Failed to connect to server");
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
                                showAlert("Error", "This LR Number not available.");
                            });
                        } else if (responseBody.equals(lrNumber)) {
                            Log.d("LR NUMBER : ", String.valueOf(response));
                            runOnUiThread(() -> addLRNumberToTable(lrNumber));
                        }
                    } else {
                        runOnUiThread(() -> {
                            showAlert("Response Erro", "Response from server is null");
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
        addHeaders();

        TableRow newRow = new TableRow(this);
        newRow.setGravity(CENTER_HORIZONTAL);

        TextView textView = new TextView(this);
        textView.setText(lrNumber);
        textView.setPadding(16, 16, 16, 16);
        textView.setGravity(CENTER);
        newRow.addView(textView);

        Button deleteButton = new Button(this);
        deleteButton.setText(getString(R.string.delete_button_text));
        deleteButton.setOnClickListener(v -> {
            tableLayoutActivityTen.removeView(newRow);
            lrNumbersSet.remove(lrNumber);
            if (lrNumbersSet.isEmpty()) {
                tableLayoutActivityTen.removeAllViews();
            }
        });

        TableRow.LayoutParams buttonParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        buttonParams.setMargins(0, 0, 0, 0); // Remove any space between button and text
        deleteButton.setLayoutParams(buttonParams);
        newRow.addView(deleteButton);
        tableLayoutActivityTen.addView(newRow);
        lrNumbersSet.add(lrNumber);
        lrEditTextActivityTen.setText("");
    }

    private void addHeaders() {
        // Check if the table already has headers
        if (tableLayoutActivityTen.getChildCount() == 0) {
            TableRow headerRow = new TableRow(this);
            headerRow.setGravity(CENTER_HORIZONTAL);

            TextView headerLrNumber = new TextView(this);
            headerLrNumber.setText("LR Number");
            headerLrNumber.setPadding(16, 16, 16, 16); // Padding for header
            headerLrNumber.setGravity(CENTER);
            headerRow.addView(headerLrNumber);

            TextView headerAction = new TextView(this);
            headerAction.setText("Action");
            headerAction.setPadding(16, 16, 16, 16); // Padding for header
            headerAction.setGravity(CENTER);
            headerRow.addView(headerAction);

            tableLayoutActivityTen.addView(headerRow);
        }
    }

    private void submitDataToServer() {
        // Retrieve data from UI components
        String goDown = goDownSpinnerActivityTen.getSelectedItem().toString().trim();
        String remark = remarkEditTextActivityTen.getText().toString().trim();

        // Check if mandatory fields are filled
        if (lrNumbersSet.isEmpty()) {
            showWarning("Warning", "At least one LR Number is require to submit");
            return; // Exit the method
        }

        if (remark.isEmpty()) {
            showWarning("Warning", "Please give remark");
            remarkEditTextActivityTen.setText("");
            remarkEditTextActivityTen.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(remarkEditTextActivityTen, InputMethodManager.SHOW_IMPLICIT);
            return;
        }

        if (goDown.equals("Select Godown")) {
            showWarning("Warning", "Please select a Godown");
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
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("UserName", username);
        formBuilder.add("spinnerDepo", depo);
        formBuilder.add("spinnerYear", year);
        formBuilder.add("remark", remark);
        formBuilder.add("goDown", goDown);
        formBuilder.add("arrayListOfLR", arrayListOfLR);

        Request request = new Request.Builder().url("https://vtc3pl.com/godown_lr_submit_from_prn_app.php").post(formBuilder.build()).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    showAlert("Connection failed", "Failed to connect to server");
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        String responseBody = body.string();
                        Log.e("Response GroupCode :", responseBody);
                        // Process the response here
                        runOnUiThread(() -> {

//                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity10.this);
//                            builder.setTitle("Success").setMessage(responseBody).setPositiveButton("OK", (dialog, which) -> {
//                                dialog.dismiss();
//                                clearUIComponents();
//                            }).setNeutralButton("Copy", (dialog, which) -> {
//                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                                ClipData clip = ClipData.newPlainText("Response", responseBody);
//                                clipboard.setPrimaryClip(clip);
//                                Toast.makeText(MainActivity10.this, "Response copied to clipboard", Toast.LENGTH_SHORT).show();
//                                clearUIComponents();
//                            }).setIcon(android.R.drawable.checkbox_on_background).show();

                            // Load the original image
                            Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.success);

                            // Scale the image to the desired size
                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 32, 32, true);

                            // Create a Drawable from the scaled Bitmap
                            Drawable successIcon = new BitmapDrawable(getResources(), scaledBitmap);

                            final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity10.this)
                                    .setTitle("Success")
                                    .setMessage(responseBody)
                                    .setPositiveButton("OK", (dialog, which) -> {
                                        dialog.dismiss();
                                        clearUIComponents();
                                    }).setNeutralButton("Copy", (dialog, which) -> {
                                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("Response", responseBody);
                                        clipboard.setPrimaryClip(clip);
                                        Toast.makeText(MainActivity10.this, "Response copied to clipboard", Toast.LENGTH_SHORT).show();
                                        clearUIComponents();
                                    })
                                    .setIcon(successIcon)
                                    .create();
                            alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    dialog.dismiss();
                                    clearUIComponents();
                                }
                            });

                            alertDialog.show();

                        });
                    } else {
                        runOnUiThread(() -> {
                            showAlert("Response Error", "Empty response from server");
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

    private void clearUIComponents() {
        lrEditTextActivityTen.setText("");
        remarkEditTextActivityTen.setText("");
        tableLayoutActivityTen.removeAllViews();
        lrNumbersSet.clear();
        goDownSpinnerActivityTen.setSelection(0);

        finish();
    }

    private void showAlert(String title, String message) {
        Drawable alertIcon = ContextCompat.getDrawable(MainActivity10.this, android.R.drawable.ic_delete);
        if (alertIcon != null) {
            alertIcon = DrawableCompat.wrap(alertIcon);
            DrawableCompat.setTint(alertIcon, Color.RED);
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setIcon(alertIcon)
                .show();
    }

    private void showWarning(String title, String message) {
        Drawable warningIcon = ContextCompat.getDrawable(MainActivity10.this, android.R.drawable.stat_notify_error);
        if (warningIcon != null) {
            warningIcon = DrawableCompat.wrap(warningIcon);
            DrawableCompat.setTint(warningIcon, Color.YELLOW);
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setIcon(warningIcon)
                .show();
    }

}