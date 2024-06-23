package com.example.asistenvisual.data.retrofit

import com.example.asistenvisual.data.response.ApiConstants.AUTHORIZATION_HEADER
import com.example.asistenvisual.data.response.ApiConstants.CONTENT_TYPE_HEADER
import com.example.asistenvisual.data.response.MyData
import com.example.asistenvisual.data.response.ResultResponse
import com.example.asistenvisual.data.response.PredictResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path


interface ApiService {

    @Headers(
        AUTHORIZATION_HEADER,
        CONTENT_TYPE_HEADER
    )
    @POST("predictions")
    fun postTheImage(@Body data: MyData): Call<PredictResponse>

    @Headers(
        AUTHORIZATION_HEADER,
        CONTENT_TYPE_HEADER
    )
    @GET("predictions/{id}")
    fun getResult(
        @Path("id") id: String
    ): Call<ResultResponse>
}