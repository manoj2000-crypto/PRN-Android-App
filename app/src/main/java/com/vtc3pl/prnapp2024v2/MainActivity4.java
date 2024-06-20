package com.vtc3pl.prnapp2024v2;
// Second Page with four Buttons

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity4 extends AppCompatActivity {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private String username = "", depo = "", year = "";
    private TextView showUserNameActivityFourTextView;
    private Button createPrnButton, arrivalPrnButton, prnListButton, lrNoPendingForPRNButton, lrNoMissmatchReportPRNButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main4);

        showUserNameActivityFourTextView = findViewById(R.id.showUserNameActivityFourTextView);
        createPrnButton = findViewById(R.id.createPrnButton);
        arrivalPrnButton = findViewById(R.id.arrivalPrnButton);
        prnListButton = findViewById(R.id.prnListButton);
        lrNoPendingForPRNButton = findViewById(R.id.lrNoPendingForPRNButton);
        lrNoMissmatchReportPRNButton = findViewById(R.id.lrNoMissmatchReportPRNButton);

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
            Log.e("username", username != null ? username : null);
            depo = intent.getStringExtra("depo");
            Log.e("depo", depo != null ? depo : null);
            year = intent.getStringExtra("year");
            Log.e("year", year != null ? year : null);

            if (username != null) {
                String usernameText = getString(R.string.user_name_prefix, username);
                showUserNameActivityFourTextView.setText(usernameText);
            }
        }

        fetchPageAccess(username);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void fetchPageAccess(String username) {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("userName", username);

        Request request = new Request.Builder().url("https://vtc3pl.com/page_access_prn_app.php").post(formBuilder.build()).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    new AlertDialog.Builder(MainActivity4.this).setTitle("Error").setMessage("Error fetching page access. Please try again later.").setPositiveButton("OK", null).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                try {
                    JSONObject result = new JSONObject(response.body().string());
                    Log.d("ResultNotNull", String.valueOf(result));
                    if (result.has("error")) {
                        runOnUiThread(() -> {
                            new AlertDialog.Builder(MainActivity4.this)
                                    .setTitle("Information")
                                    .setMessage(result.optString("error"))
                                    .setPositiveButton("OK", (dialog, which) -> {
                                        finish();
                                    })
                                    .show();
                        });
                    } else {
                        runOnUiThread(() -> setupButtonListeners(result));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        new AlertDialog.Builder(MainActivity4.this).setTitle("Error").setMessage("Error parsing page access data.").setPositiveButton("OK", null).show();
                    });
                }
            }
        });
    }

    private void setupButtonListeners(JSONObject accessData) {
        try {
            final boolean createPRNAccess = accessData.getInt("createPRN") == 1;
            final boolean arrivalPRNAccess = accessData.getInt("arrivalPRN") == 1;
            final boolean prnListAccess = accessData.getInt("prnList") == 1;
            final boolean lrPendingForPRNAccess = accessData.getInt("lrPendingForPRN") == 1;
            final boolean lrNoMissmatchReportAccess = accessData.getInt("LrMissmatchReport") == 1;

            createPrnButton.setOnClickListener(v -> {
                if (createPRNAccess) {
                    Intent intent = new Intent(MainActivity4.this, MainActivity9.class);
                    intent.putExtra("username", username);
                    intent.putExtra("depo", depo);
                    intent.putExtra("year", year);
                    startActivity(intent);
                } else {
                    showAccessDeniedAlert();
                }
            });

            arrivalPrnButton.setOnClickListener(v -> {
                if (arrivalPRNAccess) {
//                    runOnUiThread(() -> {
//                        new AlertDialog.Builder(MainActivity4.this)
//                                .setTitle("Maintenance")
//                                .setMessage("This feature is under maintenance.")
//                                .setPositiveButton("OK", null)
//                                .show();
//                    });
                    Intent intent = new Intent(MainActivity4.this, MainActivity6.class);
                    intent.putExtra("username", username);
                    intent.putExtra("depo", depo);
                    intent.putExtra("year", year);
                    startActivity(intent);
                } else {
                    showAccessDeniedAlert();
                }
            });

            prnListButton.setOnClickListener(v -> {
                if (prnListAccess) {
                    Intent intent = new Intent(MainActivity4.this, MainActivity5.class);
                    intent.putExtra("username", username);
                    intent.putExtra("depo", depo);
                    intent.putExtra("year", year);
                    startActivity(intent);
                } else {
                    showAccessDeniedAlert();
                }
            });

            lrNoPendingForPRNButton.setOnClickListener(v -> {
                if (lrPendingForPRNAccess) {
                    Intent intent = new Intent(MainActivity4.this, MainActivity8.class);
                    intent.putExtra("username", username);
                    intent.putExtra("depo", depo);
                    intent.putExtra("year", year);
                    startActivity(intent);
                } else {
                    showAccessDeniedAlert();
                }
            });

            lrNoMissmatchReportPRNButton.setOnClickListener(v -> {
                if (lrNoMissmatchReportAccess) {
                    Intent intent = new Intent(MainActivity4.this, MainActivity11.class);
                    intent.putExtra("username", username);
                    intent.putExtra("depo", depo);
                    intent.putExtra("year", year);
                    startActivity(intent);
                } else {
                    showAccessDeniedAlert();
                }
            });

        } catch (Exception e) {
            Log.e("MainActivity4", "Error parsing access data", e);
        }
    }

    private void showAccessDeniedAlert() {
        new AlertDialog.Builder(MainActivity4.this)
                .setTitle("Access Denied")
                .setMessage("You are not allowed to view this page.")
                .setPositiveButton("OK", null)
                .setNegativeButton("Return to Login", (dialog, which) -> {
                    Intent intent = new Intent(MainActivity4.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

}