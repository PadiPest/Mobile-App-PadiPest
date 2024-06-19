package com.example.padipest.data.api

import com.example.padipest.data.response.Response
import com.example.padipest.data.response.UploadResponse
import com.example.padipest.data.response.UserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    @Multipart
    @POST("predict")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
    ): UploadResponse

    @GET("profile/{id}")
    suspend fun getUser(
        @Path("id") id: String
    ): UserResponse

    @Multipart
    @POST("upload")
    suspend fun uploadUser(
        @Part file: MultipartBody.Part,
        @Part("userId") userId: RequestBody,
        @Part("name") name: RequestBody,
    ): Response

}