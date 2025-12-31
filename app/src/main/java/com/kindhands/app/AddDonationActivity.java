package com.kindhands.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class AddDonationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_donation);

        // Find CardViews
        View clothes = findViewById(R.id.cardClothes);
        View food = findViewById(R.id.cardFood);
        View books = findViewById(R.id.cardBooks);
        View medical = findViewById(R.id.cardMedical);
        View toys = findViewById(R.id.cardToys);
        View stationery = findViewById(R.id.cardStationery);

        // Set Click Listeners
        if (clothes != null) clothes.setOnClickListener(v -> openForm("clothes"));
        if (food != null) food.setOnClickListener(v -> openForm("food"));
        if (books != null) books.setOnClickListener(v -> openForm("books"));
        if (medical != null) medical.setOnClickListener(v -> openForm("medical"));
        if (toys != null) toys.setOnClickListener(v -> openForm("toys"));
        if (stationery != null) stationery.setOnClickListener(v -> openForm("stationery"));
    }

    private void openForm(String category) {
        Intent intent = new Intent(this, DonationDetailsActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
    }
}
