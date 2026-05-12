package com.nikhil.sellerapp.comprofile

import android.app.Application

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.nikhil.sellerapp.BuildConfig
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

class supabasefile: Application() {
    lateinit var supabaseClient: SupabaseClient
    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(applicationContext)
        val settings = firestoreSettings {
            isPersistenceEnabled = true
        }
        Firebase.firestore.firestoreSettings = settings
        supabaseClient= createSupabaseClient(
            BuildConfig.SUPABASE_URL
        ,BuildConfig.SUPABASE_KEY
        ){
            install(Storage)
        }
        // Now you can access Supabase Auth and Storage through the `supabaseClient`
//        val auth = supabaseClient.auth
        val bucket = supabaseClient.storage.from("sample")
    }
}