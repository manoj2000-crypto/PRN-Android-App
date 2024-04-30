package com.vtc3pl.prnapp2024v2;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class MainActivity3 extends AppCompatActivity {

    private EditText editTextFromDateActivityThree, editTextToDateActivityThree;
    private Calendar fromCalendar, toCalendar;
    private DatePickerDialog.OnDateSetListener fromDateSetListener, toDateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main3);

        editTextFromDateActivityThree = findViewById(R.id.editTextFromDateActivityThree);
        editTextToDateActivityThree = findViewById(R.id.editTextToDateActivityThree);

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
        editTextFromDateActivityThree.setText(android.text.format.DateFormat.format(dateFormat, fromCalendar));
    }

    private void updateToDate() {
        String dateFormat = "yyyy/MM/dd";
        editTextToDateActivityThree.setText(android.text.format.DateFormat.format(dateFormat, toCalendar));
    }
}