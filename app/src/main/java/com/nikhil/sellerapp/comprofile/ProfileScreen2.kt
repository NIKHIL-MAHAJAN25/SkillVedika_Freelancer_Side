package com.nikhil.sellerapp.comprofile

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.chip.Chip
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.nikhil.sellerapp.R
import com.nikhil.sellerapp.databinding.ActivityProfileScreen2Binding

class ProfileScreen2 : AppCompatActivity() {
    lateinit var binding: ActivityProfileScreen2Binding
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val languages = listOf("Telugu",
        "English",
        "Assamese",
        "Konkani",
        "Dogri",
        "Tulu",
        "Gujarati",
        "Kannada",
        "Malayalam",
        "Marathi",
        "Manipuri",
        "Mizo",
        "Odia",
        "Punjabi",
        "Nepali",
        "Tamil",
        "Bengali",
        "Urdu",
        "Hindi",
        "Ladakhi")
    val db= Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityProfileScreen2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        populate()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.btnNext.setOnClickListener {
            savedata()
            startActivity(Intent(this,Entercode::class.java))
        }
    }
    private fun savedata(){
        val biotext=binding.etBio.text.toString()
        if (biotext.isEmpty()) {
            Toast.makeText(this, "Please enter your bio", Toast.LENGTH_SHORT).show()
            return
        }
        val selected= mutableListOf<String>()
        for(i in 0 until binding.chipGroupLang.childCount){
            val chip=binding.chipGroupLang.getChildAt(i) as Chip
            if(chip.isChecked){
                selected.add(chip.text.toString())
            }
        }
        if (selected.isEmpty()) {
            Toast.makeText(this, "Please select at least one language", Toast.LENGTH_SHORT).show()
            return
        }

        val user= mapOf(
            "bio" to biotext,
            "language" to selected,
            "profilecomplete" to true,
            "approved" to false
        )
        val uid=auth.currentUser?.uid

        if (uid != null) {
            db.collection("Users").document(uid).update(user).addOnSuccessListener {
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
            }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }
    private fun populate() {
        languages.forEach { language ->
            val chip = Chip(this, null, R.style.CustomChipStyle).apply {
                text = language
                isCheckable = true
                isClickable = true
                textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
                setPadding(32, 16, 32, 16)

                // Force background color — Material3 ignores XML chipBackgroundColor via constructor
                val bgColor = ContextCompat.getColor(context, R.color.bg)
                chipBackgroundColor = ColorStateList.valueOf(bgColor)
                setTextColor(ContextCompat.getColor(context, R.color.black))
            }
            binding.chipGroupLang.addView(chip)
        }
    }
}