package com.example.gurupoint.data.api

import com.example.gurupoint.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("auth/login")
    suspend fun login(
        @Body request: Map<String, String>
    ): Response<ApiResponse<User>>

    @POST("auth/register")
    suspend fun register(
        @Body request: Map<String, String>
    ): Response<ApiResponse<User>>

    @GET("user/profile")
    suspend fun getUserProfile(
        @Query("userId") userId: String
    ): Response<ApiResponse<User>>

    @GET("games/list")
    suspend fun getGames(): Response<ApiResponse<List<Game>>>

    @POST("selections/submit")
    suspend fun submitSelection(
        @Body selection: Map<String, Any>
    ): Response<ApiResponse<Selection>>

    @GET("results/latest")
    suspend fun getLatestResults(): Response<ApiResponse<List<ResultItem>>>

    @GET("coins/transactions")
    suspend fun getTransactions(
        @Query("userId") userId: String
    ): Response<ApiResponse<List<CoinTransaction>>>
}
