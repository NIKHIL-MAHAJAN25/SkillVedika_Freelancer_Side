package com.nikhil.sellerapp.mailretro

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface Mailfetch {
    @POST("send-welcome")
    fun sendWelcome(@Body data:Map<String,String>): Call<ApiResponse>
    @POST("send-otp")
    fun sendOtp(@Body data: Map<String, String>): Call<ApiResponse>
}