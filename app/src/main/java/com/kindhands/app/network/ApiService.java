package com.kindhands.app.network;

import com.kindhands.app.model.DonationRequest;
import com.kindhands.app.model.Organization;
import com.kindhands.app.model.OrganizationLoginRequest;
import com.kindhands.app.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
// import retrofit2.http.Query; // Use this if you need query params

public interface ApiService {

    // --- REQUEST CONTROLLER ---
    
    // 1. Create a request (Organization asks for donation OR Donor offers donation - depends on usage)
    @POST("requests/create")
    Call<DonationRequest> createRequest(@Body DonationRequest requestBody);

    // 2. Get all open requests
    @GET("requests/open")
    Call<List<DonationRequest>> getOpenRequests();

    // 3. Accept a request (Donor accepts to fulfill)
    @PUT("requests/{id}/accept/{donorId}")
    Call<DonationRequest> acceptRequest(@Path("id") Long id, @Path("donorId") Long donorId);

    // 4. Reject a request
    @PUT("requests/{id}/reject")
    Call<DonationRequest> rejectRequest(@Path("id") Long id);

    // 5. Get Organization's requests
    @GET("requests/organization/{orgId}")
    Call<List<DonationRequest>> getOrgRequests(@Path("orgId") Long orgId);

    // 6. Mark as Delivered
    @PUT("requests/{id}/delivered")
    Call<DonationRequest> markRequestDelivered(@Path("id") Long id);

    // 7. Complete Request
    @PUT("requests/{id}/complete")
    Call<DonationRequest> completeRequest(@Path("id") Long id);


    // --- AUTH CONTROLLER (General) ---
    @POST("auth/register")
    Call<Object> registerUser(@Body User user);

    @POST("auth/login")
    Call<User> loginUser(@Body User user);


    // --- DONOR CONTROLLER ---
    @POST("donors/register")
    Call<User> registerDonor(@Body User user);

    @POST("donors/login")
    Call<User> loginDonor(@Body User user);


    // --- ORGANIZATION CONTROLLER ---
    @POST("organizations/register")
    Call<Organization> registerOrganization(@Body Organization org);

    @POST("organizations/login")
    Call<Organization> loginOrganization(@Body OrganizationLoginRequest loginRequest);
}
