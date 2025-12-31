package com.kindhands.app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kindhands.app.model.DonationRequest;
import com.kindhands.app.network.ApiService;
import com.kindhands.app.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DonationDetailsActivity extends AppCompatActivity {

    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_details);

        category = getIntent().getStringExtra("category");
        FrameLayout container = findViewById(R.id.formContainer);

        if (category == null) return;

        switch (category) {
            case "clothes":
                getLayoutInflater().inflate(R.layout.form_clothes, container);
                setupClothesForm();
                break;
            case "books":
                getLayoutInflater().inflate(R.layout.form_books, container);
                setupBooksForm();
                break;
            case "food":
                getLayoutInflater().inflate(R.layout.form_food, container);
                break;
            case "toys":
                getLayoutInflater().inflate(R.layout.form_toys, container);
                setupToysForm();
                break;
            case "medical":
                getLayoutInflater().inflate(R.layout.form_medical, container);
                break;
            case "stationery":
                getLayoutInflater().inflate(R.layout.form_stationery, container);
                setupStationeryForm();
                break;
        }

        Button btnSubmit = findViewById(R.id.btnSubmitDonation);
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> submitDonation());
        }
    }

    private void submitDonation() {
        // Collect data based on category
        String details = "";
        int quantity = 1; // Default or fetch from input
        String otherDetails = "";

        // Example: simple collection logic for clothes
        if ("clothes".equals(category)) {
             StringBuilder sb = new StringBuilder();
             CheckBox cbShirt = findViewById(R.id.cbShirt);
             CheckBox cbSaree = findViewById(R.id.cbSaree);
             CheckBox cbPants = findViewById(R.id.cbPants);
             
             if (cbShirt != null && cbShirt.isChecked()) sb.append("Shirt, ");
             if (cbSaree != null && cbSaree.isChecked()) sb.append("Saree, ");
             if (cbPants != null && cbPants.isChecked()) sb.append("Pants, ");
             
             details = sb.toString();
             // Remove trailing comma
             if (details.length() > 2) details = details.substring(0, details.length() - 2);
        } else {
            // For other categories, we might just set generic details for now
            // Ideally, implement extraction for each form type here
            details = category + " donation";
        }

        // Create Request Object
        DonationRequest request = new DonationRequest(category, details, quantity, otherDetails);
        
        // IMPORTANT: Set Organization ID if this is an Org creating a request
        // request.setOrganizationId(1L); // Example ID

        // Send to Backend
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        
        // Use createRequest based on the controller provided
        Call<DonationRequest> call = apiService.createRequest(request);

        call.enqueue(new Callback<DonationRequest>() {
            @Override
            public void onResponse(Call<DonationRequest> call, Response<DonationRequest> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DonationDetailsActivity.this, "Request Created Successfully!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(DonationDetailsActivity.this, "Submission Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DonationRequest> call, Throwable t) {
                Toast.makeText(DonationDetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_ERROR", t.getMessage());
            }
        });
    }

    private void setupClothesForm() {
        CheckBox cbOthers = findViewById(R.id.cbOthers);
        EditText etOtherClothes = findViewById(R.id.etOtherClothes);

        if (cbOthers != null && etOtherClothes != null) {
            cbOthers.setOnCheckedChangeListener((buttonView, isChecked) -> {
                etOtherClothes.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            });
        }
    }

    private void setupBooksForm() {
        CheckBox cbOtherBooks = findViewById(R.id.cbOtherBooks);
        EditText etOtherBooks = findViewById(R.id.etOtherBooks);

        if (cbOtherBooks != null && etOtherBooks != null) {
            cbOtherBooks.setOnCheckedChangeListener((buttonView, isChecked) -> {
                etOtherBooks.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            });
        }
    }

    private void setupToysForm() {
        Spinner spinnerToyType = findViewById(R.id.spinnerToyType);
        if (spinnerToyType != null) {
            String[] toyTypes = {"Soft Toys", "Educational Toys", "Action Figures", "Puzzles", "Board Games", "Electronic Toys", "Others"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, toyTypes);
            spinnerToyType.setAdapter(adapter);
        }
    }

    private void setupStationeryForm() {
        CheckBox cbOtherStationery = findViewById(R.id.cbOtherStationery);
        EditText etOtherStationery = findViewById(R.id.etOtherStationery);

        if (cbOtherStationery != null && etOtherStationery != null) {
            cbOtherStationery.setOnCheckedChangeListener((buttonView, isChecked) -> {
                etOtherStationery.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            });
        }
    }
}
