package com.vtc3pl.prnapp2024v2;
//After clicking on button "Create PRN" on second page this page will open with two options "Auto", ""
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity9 extends AppCompatActivity {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private String username = "", depo = "", year = "";
    private Button createPrnAutoButton, companyWiseButton;
    private TextView showUserNameActivityNineTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main9);

        createPrnAutoButton = findViewById(R.id.createPrnAutoButton);
        companyWiseButton = findViewById(R.id.companyWiseButton);
        showUserNameActivityNineTextView = findViewById(R.id.showUserNameActivityNineTextView);

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
            depo = intent.getStringExtra("depo");
            year = intent.getStringExtra("year");

            if (username != null) {
                String usernameText = getString(R.string.user_name_prefix, username);
                showUserNameActivityNineTextView.setText(usernameText);
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
        OkHttpClient client = new OkHttpClient();

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("userName", username);

        Request request = new Request.Builder().url("https://vtc3pl.com/page_access_prn_app.php").post(formBuilder.build()).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    new AlertDialog.Builder(MainActivity9.this).setTitle("Error").setMessage("Error fetching page access. Please wait for some time or try again later.").setPositiveButton("OK", null).show();
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
                            new AlertDialog.Builder(MainActivity9.this)
                                    .setTitle("Error")
                                    .setMessage(result.optString("error"))
                                    .setPositiveButton("OK", null)
                                    .show();
                        });
                    } else {
                        runOnUiThread(() -> setupButtonListeners(result));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        new AlertDialog.Builder(MainActivity9.this).setTitle("Error").setMessage("Error parsing page access data.").setPositiveButton("OK", null).show();
                    });
                }
            }
        });
    }

    private void setupButtonListeners(JSONObject accessData) {
        try {
            final boolean createPRNAutoAccess = accessData.getInt("createPrnAuto") == 1;
            final boolean companyWiseAccess = accessData.getInt("companyWise") == 1;

            createPrnAutoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (createPRNAutoAccess) {
                        Intent intent = new Intent(MainActivity9.this, MainActivity2.class);
                        intent.putExtra("username", username);
                        intent.putExtra("depo", depo);
                        intent.putExtra("year", year);
                        startActivity(intent);
                    } else {
                        showAccessDeniedAlert();
                    }
                }
            });

            companyWiseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (companyWiseAccess) {
                        Intent intent = new Intent(MainActivity9.this, MainActivity3.class);
                        intent.putExtra("username", username);
                        intent.putExtra("depo", depo);
                        intent.putExtra("year", year);
                        startActivity(intent);
                    } else {
                        showAccessDeniedAlert();
                    }
                }
            });
        } catch (JSONException e) {
            Log.e("MainActivity9", "Error parsing access data", e);
        }
    }

    private void showAccessDeniedAlert() {
        new AlertDialog.Builder(MainActivity9.this)
                .setTitle("Access Denied")
                .setMessage("You are not allowed to view this page.")
                .setPositiveButton("OK", null)
                .setNegativeButton("Return to Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity9.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .show();
    }


}