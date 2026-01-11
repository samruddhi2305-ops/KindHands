package com.kindhands.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.kindhands.app.network.ApiService;
import com.kindhands.app.network.RetrofitClient;

import java.io.*;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterOrganizationActivity extends AppCompatActivity {

    EditText etName, etEmail, etPassword, etContact, etAddress, etPincode;
    Spinner spinnerType;
    TextView tvFile;
    Button btnUpload, btnRegister;

    String selectedFilePath = null;

    ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_organization);

        etName = findViewById(R.id.etOrgName);
        etEmail = findViewById(R.id.etOrgEmail);
        etPassword = findViewById(R.id.etOrgPassword);
        etContact = findViewById(R.id.etOrgContact);
        etAddress = findViewById(R.id.etOrgAddress);
        etPincode = findViewById(R.id.etOrgPincode);
        spinnerType = findViewById(R.id.spinnerOrgType);
        tvFile = findViewById(R.id.tvSelectedFileName);
        btnUpload = findViewById(R.id.btnUploadDocument);
        btnRegister = findViewById(R.id.btnOrgRegister);

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        File file = copyUriToFile(uri);
                        if (file != null) {
                            selectedFilePath = file.getAbsolutePath();
                            tvFile.setText(file.getName());
                        }
                    }
                }
        );

        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            launcher.launch(intent);
        });

        btnRegister.setOnClickListener(v -> register());
    }

    private File copyUriToFile(Uri uri) {
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            File file = new File(getCacheDir(), "doc_" + System.currentTimeMillis());
            FileOutputStream out = new FileOutputStream(file);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            in.close();
            out.close();
            return file;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void register() {

        // ✅ VALIDATION (MOST IMPORTANT)
        if (etName.getText().toString().trim().isEmpty()
                || etEmail.getText().toString().trim().isEmpty()
                || etPassword.getText().toString().trim().isEmpty()
                || etContact.getText().toString().trim().isEmpty()
                || etAddress.getText().toString().trim().isEmpty()
                || etPincode.getText().toString().trim().isEmpty()) {

            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedFilePath == null) {
            Toast.makeText(this, "Please upload document", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(selectedFilePath);

        String rawType = spinnerType.getSelectedItem().toString();
        String type = rawType.equalsIgnoreCase("Orphanage")
                ? "ORPHANAGE"
                : "OLD_AGE_HOME";

        ApiService api = RetrofitClient.getClient().create(ApiService.class);

        Call<String> call = api.registerOrganization(
                rb(etName.getText().toString().trim()),
                rb(etEmail.getText().toString().trim()),
                rb(etPassword.getText().toString().trim()),
                rb(etContact.getText().toString().trim()),
                rb(type),
                rb(etAddress.getText().toString().trim()),
                rb(etPincode.getText().toString().trim()),
                rb("1"), // TEMP userId
                MultipartBody.Part.createFormData(
                        "document",
                        file.getName(),
                        RequestBody.create(MediaType.parse("multipart/form-data"), file)
                )
        );

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterOrganizationActivity.this,
                            "Registered – wait for admin approval",
                            Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(RegisterOrganizationActivity.this,
                            "Failed : " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("ORG_REGISTER_ERROR", t.getMessage());
                Toast.makeText(RegisterOrganizationActivity.this,
                        "Error : " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private RequestBody rb(String s) {
        return RequestBody.create(MediaType.parse("text/plain"), s);
    }
}
