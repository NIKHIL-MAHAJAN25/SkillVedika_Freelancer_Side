package com.nikhil.sellerapp.GeminiClient

data class ResumeRequest(
    val resumeText:String,
    val jobDesc:String
)
data class ResumeResponse(
    val success: Boolean,
    val data: GemResponse
)
