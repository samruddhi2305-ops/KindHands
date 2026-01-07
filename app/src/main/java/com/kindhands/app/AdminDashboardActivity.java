package com.kindhands.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kindhands.app.model.Organization;
import com.kindhands.app.network.ApiService;
import com.kindhands.app.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    PendingOrgsAdapter adapter;
    List<Organization> list = new ArrayList<>();
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        recyclerView = findViewById(R.id.rvPendingOrgs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        apiService = RetrofitClient.getClient().create(ApiService.class);

        loadPending();
    }

    private void loadPending() {
        apiService.getPendingOrganizations().enqueue(new Callback<List<Organization>>() {
            @Override
            public void onResponse(Call<List<Organization>> call, Response<List<Organization>> response) {
                if (response.isSuccessful()) {
                    list = response.body();
                    adapter = new PendingOrgsAdapter(list);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Organization>> call, Throwable t) {
                Toast.makeText(AdminDashboardActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ================= ADAPTER =================
    class PendingOrgsAdapter extends RecyclerView.Adapter<PendingOrgsAdapter.VH> {

        List<Organization> orgs;

        PendingOrgsAdapter(List<Organization> orgs) {
            this.orgs = orgs;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_pending_organization, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Organization o = orgs.get(pos);

            h.tvName.setText(o.getName());
            h.tvDetails.setText(o.getEmail() + " | " + o.getContact());

            // 🔥 VIEW DOCUMENT
            h.btnView.setOnClickListener(v -> {
                apiService.getDocumentUrl(o.getId()).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                            String url = response.body();
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Toast.makeText(AdminDashboardActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            h.btnApprove.setOnClickListener(v -> update(o.getId(), true));
            h.btnReject.setOnClickListener(v -> update(o.getId(), false));
        }

        private void update(Long id, boolean approve) {
            Call<Void> call = approve ?
                    apiService.approveOrg(id) :
                    apiService.rejectOrg(id);

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Toast.makeText(AdminDashboardActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                    loadPending();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(AdminDashboardActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return orgs.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvDetails;
            Button btnView, btnApprove, btnReject;

            VH(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvOrgName);
                tvDetails = v.findViewById(R.id.tvOrgDetails);
                btnView = v.findViewById(R.id.btnViewCertificate);
                btnApprove = v.findViewById(R.id.btnApprove);
                btnReject = v.findViewById(R.id.btnReject);
            }
        }
    }
}
