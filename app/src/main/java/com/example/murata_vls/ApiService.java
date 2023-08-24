package com.example.murata_vls;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @POST("/api/tulips/security/takephototwo") // Adjust the endpoint path based on your Laravel routes
    Call<UpdateResponse> checkUpdates();
}
