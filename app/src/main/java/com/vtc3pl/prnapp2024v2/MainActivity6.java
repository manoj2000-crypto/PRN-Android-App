package com.vtc3pl.prnapp2024v2;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class MainActivity6 extends AppCompatActivity {

    private EditText editTextFromDateActivitySix, editTextToDateActivitySix, prnNumberEditText;
    private TextView textViewFromDateActivitySix, textViewToDateActivitySix, prnNumberTextView;
    private Calendar fromCalendar, toCalendar;
    private DatePickerDialog.OnDateSetListener fromDateSetListener, toDateSetListener;
    private Button searchButton;


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

        searchButton = findViewById(R.id.searchButton);

        fromCalendar = Calendar.getInstance();
        toCalendar = Calendar.getInstance();

        fromDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                fromCalendar.set(Calendar.YEAR, year);
                fromCalendar.set(Calendar.MONTH, month);
                fromCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateFromDate();
            }
        };

        toDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                toCalendar.set(Calendar.YEAR, year);
                toCalendar.set(Calendar.MONTH, month);
                toCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateToDate();
            }
        };

        setupRadioButtons();

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
            } else if (checkedId == radioButtonPRN.getId()) {
                showPRNViews();
                adjustSearchButtonPosition(prnNumberEditText);
            }
        });
    }

    private void showDateRangeViews() {
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

        findViewById(R.id.prnNumberTextView).setVisibility(View.VISIBLE);
        findViewById(R.id.prnNumberEditText).setVisibility(View.VISIBLE);
    }

    private void adjustSearchButtonPosition(View anchorView) {
        Button searchButton = findViewById(R.id.searchButton);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) searchButton.getLayoutParams();
        params.topToBottom = anchorView.getId();
        searchButton.setLayoutParams(params);
        searchButton.setVisibility(View.VISIBLE);
    }


}