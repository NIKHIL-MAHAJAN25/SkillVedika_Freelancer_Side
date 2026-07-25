package com.nikhil.sellerapp.GeminiClient
data class GemResponse(
    val score:Int,
    val missing_keywords:List<String>,
    val summary:String
)