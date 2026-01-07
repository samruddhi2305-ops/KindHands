package com.kindhands.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class  MainActivity extends AppCompatActivity {

    Button btnDonate, btnNGO;
    TextView txtRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View rootView = findViewById(android.R.id.content);
        rootView.startAnimation(
                android.view.animation.AnimationUtils.loadAnimation(
                        this, R.anim.fade_in));


        btnDonate = findViewById(R.id.btnDonate);
        btnNGO = findViewById(R.id.btnNGO);
        txtRegister = findViewById(R.id.txtRegister);

        btnDonate.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this,
                    "Donate Items Clicked", Toast.LENGTH_SHORT).show();
             startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });

        btnNGO.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this,
                    "NGO Login Clicked", Toast.LENGTH_SHORT).show();

            // Later open NGO Login
            // startActivity(new Intent(MainActivity.this, NGOLoginActivity.class));
        });

        txtRegister.setOnClickListener(v -> {
             startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });
    }
}
