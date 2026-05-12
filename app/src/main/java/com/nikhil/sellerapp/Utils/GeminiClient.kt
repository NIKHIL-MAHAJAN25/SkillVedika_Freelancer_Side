package com.nikhil.sellerapp.Utils

import com.google.ai.client.generativeai.GenerativeModel
import com.nikhil.sellerapp.BuildConfig

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GeminiClient {
    private val model= GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GOOGLE_KEY
    )
    suspend fun analyzeresume(resumetext:String,jobdesc:String):String?{
        return withContext(Dispatchers.IO)// 'withContext(Dispatchers.IO)' moves this work to a Background Thread.
        // This prevents the UI from freezing while we wait for Google to reply.
        {
            val prompt="""
                You are an expert HR Recruiter. Analyze this candidate profile against this job description.
                
                PROFILE:
                $resumetext
                
                JOB DESCRIPTION:
                $jobdesc
                
                Output ONLY a JSON string with this structure (no markdown code blocks):
                {
                  "score": 0-100,
                  "missing_keywords": ["skill1", "skill2"],
                  "summary": "Long summary with all the changes"
                }
                
            """.trimIndent()
            try{
                val response=model.generateContent(prompt)
                return@withContext response.text
            }catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }

    }
}