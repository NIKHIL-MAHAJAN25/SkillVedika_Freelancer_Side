package com.nikhil.sellerapp.mailretro

import com.nikhil.sellerapp.GeminiClient.GemResponse
import com.nikhil.sellerapp.GeminiClient.ResumeRequest
import com.nikhil.sellerapp.GeminiClient.ResumeResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface Mailfetch {
    @POST("send-welcome")
    fun sendWelcome(@Body data:Map<String,String>): Call<ApiResponse>
    @POST("send-otp")
    fun sendOtp(@Body data: Map<String, String>): Call<ApiResponse>
    @POST("analyze-resume")
    fun analyzeResume(@Body request: ResumeRequest):Call<ResumeResponse>
}