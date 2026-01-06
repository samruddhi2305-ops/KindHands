package com.kindhands.app.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Use the IP address of the OTHER laptop (the backend server)
            // Make sure to include the port number (e.g., :8080)
            String BASE_URL = "http://10.73.80.94:8080/"; // <--- REPLACE WITH YOUR BACKEND LAPTOP'S IP

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
