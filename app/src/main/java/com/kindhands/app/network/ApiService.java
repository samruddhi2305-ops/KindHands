package com.kindhands.app.network;

import com.kindhands.app.model.DonationRequest;
import com.kindhands.app.model.Organization;
import com.kindhands.app.model.OrganizationLoginRequest;
import com.kindhands.app.model.User;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // --- REQUEST CONTROLLER ---
    @POST("requests/create")
    Call<DonationRequest> createRequest(@Body DonationRequest requestBody);

    @GET("requests/open")
    Call<List<DonationRequest>> getOpenRequests();

    @PUT("requests/{id}/accept/{donorId}")
    Call<DonationRequest> acceptRequest(@Path("id") Long id, @Path("donorId") Long donorId);

    @PUT("requests/{id}/reject")
    Call<DonationRequest> rejectRequest(@Path("id") Long id);

    @GET("requests/organization/{orgId}")
    Call<List<DonationRequest>> getOrgRequests(@Path("orgId") Long orgId);

    @PUT("requests/{id}/delivered")
    Call<DonationRequest> markRequestDelivered(@Path("id") Long id);

    @PUT("requests/{id}/complete")
    Call<DonationRequest> completeRequest(@Path("id") Long id);


    // --- AUTH CONTROLLER ---
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
    
    // OLD JSON METHOD (Commented out to avoid conflict)
    // @POST("organizations/register")
    // Call<Organization> registerOrganization(@Body Organization org);

    // NEW MULTIPART METHOD
    @Multipart
    @POST("organizations/register")
    Call<String> registerOrganization(
            @Part("name") RequestBody name,
            @Part("email") RequestBody email,
            @Part("password") RequestBody password,
            @Part("contact") RequestBody contact,
            @Part("type") RequestBody type,
            @Part("address") RequestBody address,
            @Part("pincode") RequestBody pincode,
            @Part MultipartBody.Part document
    );

    @POST("organizations/login")
    Call<Organization> loginOrganization(@Body OrganizationLoginRequest loginRequest);


    // --- ADMIN PANEL ---
    @GET("organizations/pending") 
    Call<List<Organization>> getPendingOrganizations(); 

    @PUT("organizations/{id}/status")
    Call<Organization> updateOrgStatus(@Path("id") Long id, @Query("status") String status);
}
