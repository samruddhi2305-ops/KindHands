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

    private RecyclerView recyclerView;
    private PendingOrgsAdapter adapter;
    private List<Organization> pendingOrgs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        recyclerView = findViewById(R.id.rvPendingOrgs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Disable nested scrolling to work within a NestedScrollView
        recyclerView.setNestedScrollingEnabled(false);

        fetchPendingOrganizations();
    }

    private void fetchPendingOrganizations() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<Organization>> call = apiService.getPendingOrganizations();

        call.enqueue(new Callback<List<Organization>>() {
            @Override
            public void onResponse(Call<List<Organization>> call, Response<List<Organization>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    pendingOrgs = response.body();
                    adapter = new PendingOrgsAdapter(pendingOrgs);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(AdminDashboardActivity.this, "Failed to fetch requests", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Organization>> call, Throwable t) {
                Toast.makeText(AdminDashboardActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- ADAPTER CLASS ---
    private class PendingOrgsAdapter extends RecyclerView.Adapter<PendingOrgsAdapter.ViewHolder> {
        private List<Organization> orgs;

        public PendingOrgsAdapter(List<Organization> orgs) {
            this.orgs = orgs;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pending_organization, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Organization org = orgs.get(position);
            holder.tvOrgName.setText(org.getName());
            holder.tvOrgDetails.setText(String.format("%s | %s | %s", org.getEmail(), org.getContact(), org.getAddress()));

            holder.btnViewCertificate.setOnClickListener(v -> {
                // The 'document' field contains the URI string saved during registration
                String docUriString = org.getDocument();
                if (docUriString != null && !docUriString.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(docUriString));
                    startActivity(intent);
                } else {
                    Toast.makeText(v.getContext(), "No certificate link provided", Toast.LENGTH_SHORT).show();
                }
            });

            holder.btnApprove.setOnClickListener(v -> updateStatus(org.getId(), "APPROVED"));
            holder.btnReject.setOnClickListener(v -> updateStatus(org.getId(), "REJECTED"));
        }

        private void updateStatus(Long orgId, String status) {
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            Call<Organization> call = apiService.updateOrgStatus(orgId, status);
            call.enqueue(new Callback<Organization>() {
                @Override
                public void onResponse(Call<Organization> call, Response<Organization> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminDashboardActivity.this, "Status updated to " + status, Toast.LENGTH_SHORT).show();
                        fetchPendingOrganizations(); // Refresh list
                    } else {
                        Toast.makeText(AdminDashboardActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Organization> call, Throwable t) {
                    Toast.makeText(AdminDashboardActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return orgs.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvOrgName, tvOrgDetails;
            Button btnViewCertificate, btnApprove, btnReject;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvOrgName = itemView.findViewById(R.id.tvOrgName);
                tvOrgDetails = itemView.findViewById(R.id.tvOrgDetails);
                btnViewCertificate = itemView.findViewById(R.id.btnViewCertificate);
                btnApprove = itemView.findViewById(R.id.btnApprove);
                btnReject = itemView.findViewById(R.id.btnReject);
            }
        }
    }
}
