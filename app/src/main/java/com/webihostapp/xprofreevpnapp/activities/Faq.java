package com.webihostapp.xprofreevpnapp.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.webihostapp.xprofreevpnapp.R;


public class Faq extends AppCompatActivity {

    ImageView backToActivity;
    TextView activity_name;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);


        activity_name = findViewById(R.id.activity_name);
        backToActivity = findViewById(R.id.finish_activity);
        activity_name.setText("Faq");
        backToActivity.setOnClickListener(view -> finish());
    }
}
