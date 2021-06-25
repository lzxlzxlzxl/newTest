package com.android.myandroid.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.android.myandroid.R;

public class NameActivity extends AppCompatActivity {


    EditText editText;

    String mDevicename = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        editText = findViewById(R.id.name);
        if (getIntent() != null) {
            editText.setText(mDevicename = getIntent().getStringExtra("name"));
        }

        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothAdapter.getDefaultAdapter().setName(mDevicename);
                finish();
            }
        });
    }
}
