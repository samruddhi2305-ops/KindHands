package com.kindhands.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.kindhands.app.network.ApiService;
import com.kindhands.app.network.RetrofitClient;
import com.kindhands.app.utils.FileUtils; // Assuming this might be needed, or I'll implement a helper

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterOrganizationActivity extends AppCompatActivity {

    private EditText etName, etEmail, etContact, etAddress, etPincode, etPassword;
    private Spinner spinnerOrgType;
    private Button btnRegister, btnUploadDocument;
    private TextView tvSelectedFileName;
    private String selectedFilePath;

    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_organization);

        // Initialize Views
        etName = findViewById(R.id.etOrgName);
        etEmail = findViewById(R.id.etOrgEmail);
        etContact = findViewById(R.id.etOrgContact);
        etAddress = findViewById(R.id.etOrgAddress);
        etPincode = findViewById(R.id.etOrgPincode);
        etPassword = findViewById(R.id.etOrgPassword);
        spinnerOrgType = findViewById(R.id.spinnerOrgType);
        btnRegister = findViewById(R.id.btnOrgRegister);
        btnUploadDocument = findViewById(R.id.btnUploadDocument);
        tvSelectedFileName = findViewById(R.id.tvSelectedFileName);

        // Setup Spinner
        String[] orgTypes = {"Orphanage", "Old Age Home"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, orgTypes);
        spinnerOrgType.setAdapter(adapter);

        // File Picker Setup
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            // Copy file to cache to get a real path/file object
                            File file = getFileFromUri(uri);
                            if (file != null) {
                                selectedFilePath = file.getAbsolutePath();
                                tvSelectedFileName.setText(file.getName());
                            } else {
                                Toast.makeText(this, "Could not get file path", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );

        btnUploadDocument.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*"); // Allow all file types, maybe restrict to pdf/images
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            filePickerLauncher.launch(Intent.createChooser(intent, "Select Certificate"));
        });

        btnRegister.setOnClickListener(v -> registerOrganization());
    }

    private File getFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File tempFile = new File(getCacheDir(), "upload_temp_" + System.currentTimeMillis());
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (Exception e) {
            Log.e("FILE_ERROR", "Error converting uri to file", e);
            return null;
        }
    }

    private void registerOrganization() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String pincode = etPincode.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        String rawType = spinnerOrgType.getSelectedItem().toString();
        String type = "Orphanage".equals(rawType) ? "ORPHANAGE" : "OLD_AGE_HOME";

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || contact.isEmpty() || selectedFilePath == null) {
            Toast.makeText(this, "Please fill all required fields and upload document", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(selectedFilePath);

        // Prepare RequestBody
        RequestBody rbName = RequestBody.create(MediaType.parse("text/plain"), name);
        RequestBody rbEmail = RequestBody.create(MediaType.parse("text/plain"), email);
        RequestBody rbPassword = RequestBody.create(MediaType.parse("text/plain"), password);
        RequestBody rbContact = RequestBody.create(MediaType.parse("text/plain"), contact);
        RequestBody rbType = RequestBody.create(MediaType.parse("text/plain"), type);
        RequestBody rbAddress = RequestBody.create(MediaType.parse("text/plain"), address);
        RequestBody rbPincode = RequestBody.create(MediaType.parse("text/plain"), pincode);

        // Prepare File Part
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part document = MultipartBody.Part.createFormData("document", file.getName(), requestFile);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<String> call = apiService.registerOrganization(
                rbName, rbEmail, rbPassword, rbContact, rbType, rbAddress, rbPincode, document
        );

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterOrganizationActivity.this, "Registration Successful! Please wait for approval.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(RegisterOrganizationActivity.this, "Registration Failed: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(RegisterOrganizationActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
