package com.kindhands.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kindhands.app.model.Organization;
import com.kindhands.app.model.OrganizationLoginRequest;
import com.kindhands.app.model.User;
import com.kindhands.app.network.ApiService;
import com.kindhands.app.network.RetrofitClient;
import com.kindhands.app.utils.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Spinner spinnerLoginUserType;
    private Button btnLogin;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // CHECK IF ALREADY LOGGED IN
        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, AddDonationActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.login);

        // Initialize views
        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        spinnerLoginUserType = findViewById(R.id.spinnerLoginUserType);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvGoToRegister);

        // Setup Spinner
        String[] userTypes = {"Donor", "Orphanage", "Old Age Home"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, userTypes);
        spinnerLoginUserType.setAdapter(adapter);

        // Login Button Click
        btnLogin.setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String selectedType = spinnerLoginUserType.getSelectedItem().toString();

            // Validation
            if (email.isEmpty()) {
                etEmail.setError("Email is required");
                etEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                etPassword.setError("Password is required");
                etPassword.requestFocus();
                return;
            }

            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

            if ("Donor".equalsIgnoreCase(selectedType)) {
                // DONOR LOGIN
                User loginUser = new User(email, password);
                Call<User> call = apiService.loginDonor(loginUser);

                call.enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            User user = response.body();
                            Toast.makeText(LoginActivity.this, "Welcome Donor " + user.getName(), Toast.LENGTH_SHORT).show();
                            
                            // SAVE USER SESSION
                            SharedPrefManager.getInstance(LoginActivity.this).saveUser(user.getName(), user.getEmail(), "DONOR");

                            // Navigate to Dashboard
                            Intent intent = new Intent(LoginActivity.this, AddDonationActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Login Failed: Invalid Credentials", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                // ORGANIZATION LOGIN
                OrganizationLoginRequest loginRequest = new OrganizationLoginRequest(email, password);
                Call<Organization> call = apiService.loginOrganization(loginRequest);

                call.enqueue(new Callback<Organization>() {
                    @Override
                    public void onResponse(Call<Organization> call, Response<Organization> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Organization org = response.body();
                            Toast.makeText(LoginActivity.this, "Welcome " + org.getName(), Toast.LENGTH_SHORT).show();
                            
                            // SAVE USER SESSION
                            SharedPrefManager.getInstance(LoginActivity.this).saveUser(org.getName(), org.getEmail(), "ORGANIZATION");

                            // Navigate to Dashboard
                            Intent intent = new Intent(LoginActivity.this, AddDonationActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Login Failed: " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Organization> call, Throwable t) {
                        Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Register Click
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
