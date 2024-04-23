package com.vtc3pl.prnapp2024v2;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

public class MainActivity2 extends AppCompatActivity {

    private static final Pattern LR_NUMBER_PATTERN = Pattern.compile("[A-Z]{3,4}[0-9]{10}+");
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private TableLayout tableLayout;
    private EditText lrEditText;
    private Set<String> lrNumbersSet = new HashSet<>();
    private SurfaceView cameraView;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);

        TextView showUserNameTextView = findViewById(R.id.showUserNameTextView);
        tableLayout = findViewById(R.id.tableDisplay);
        lrEditText = findViewById(R.id.lrEditText);


        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> addRowToTable());

        Intent intent = getIntent();
        if (intent != null) {
            String username = intent.getStringExtra("username");
            String depo = intent.getStringExtra("depo");
            String year = intent.getStringExtra("year");

            // Set the fetched username to the TextView
            if (username != null) {
                showUserNameTextView.setText("User name: " + username);
            }
        }

        cameraView = findViewById(R.id.showCameraSurfaceView);
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(1600, 1024)
                .build();

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ContextCompat.checkSelfPermission(MainActivity2.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ActivityCompat.requestPermissions(MainActivity2.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    String lrNumber = barcodes.valueAt(0).displayValue;
                    runOnUiThread(() -> addLRNumberToTable(lrNumber));
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void addRowToTable() {
        String lrNumber = lrEditText.getText().toString().trim();
        if (LR_NUMBER_PATTERN.matcher(lrNumber).matches() && !lrNumber.isEmpty() && !lrNumbersSet.contains(lrNumber)) {
            TableRow newRow = new TableRow(this);
            TextView textView = new TextView(this);
            textView.setText(lrNumber);
            newRow.addView(textView);

            Button deleteButton = new Button(this);
            deleteButton.setText("Delete");
            deleteButton.setOnClickListener(v -> {
                // Remove the row when delete button is clicked
                tableLayout.removeView(newRow);
                // Remove LR number from the set
                lrNumbersSet.remove(lrNumber);
            });
            newRow.addView(deleteButton);

            tableLayout.addView(newRow);

            // Add LR number to the set
            lrNumbersSet.add(lrNumber);
            // Clear the lrEditText after adding the row
            lrEditText.setText("");
        } else {
            // Show Toast message if LR number format is invalid or duplicate
            Toast.makeText(this, "LR number format is invalid or duplicate", Toast.LENGTH_SHORT).show();
        }
    }

    private void addLRNumberToTable(String lrNumber) {
        if (LR_NUMBER_PATTERN.matcher(lrNumber).matches() && !lrNumbersSet.contains(lrNumber)) {
            TableRow newRow = new TableRow(this);
            TextView textView = new TextView(this);
            textView.setText(lrNumber);
            newRow.addView(textView);

            Button deleteButton = new Button(this);
            deleteButton.setText("Delete");
            deleteButton.setOnClickListener(v -> {
                // Remove the row when delete button is clicked
                tableLayout.removeView(newRow);
                // Remove LR number from the set
                lrNumbersSet.remove(lrNumber);
            });
            newRow.addView(deleteButton);

            tableLayout.addView(newRow);

            // Add LR number to the set
            lrNumbersSet.add(lrNumber);
        } else {
            // Show Toast message if LR number format is invalid or duplicate
            Toast.makeText(this, "LR number format is invalid or duplicate", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                cameraSource.start(cameraView.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Permission was denied
            // You may inform the user or handle the situation appropriately
            Toast.makeText(this, "Camera permission was denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }


}