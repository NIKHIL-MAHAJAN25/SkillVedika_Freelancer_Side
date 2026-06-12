package com.nikhil.sellerapp.Signup

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.nikhil.sellerapp.Login.LoginActivity
import com.nikhil.sellerapp.R
import com.nikhil.sellerapp.comprofile.ProfileScreen1
import com.nikhil.sellerapp.databinding.ActivitySignUp2Binding
import com.nikhil.sellerapp.dataclasses.User
import com.nikhil.sellerapp.dataclasses.UserRole

class SignUpActivity2 : AppCompatActivity() {

    lateinit var binding: ActivitySignUp2Binding
    private var auth:FirebaseAuth=FirebaseAuth.getInstance()
    val db= Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivitySignUp2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.alrsignin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        binding.btnSignUP.setOnClickListener {
            val aemail=binding.etmailsignin2.text.toString()
            val password=binding.etpsswrdsignin2.text.toString()
            val confirm=binding.etpsswrdsignin3.text.toString()
            val date=FieldValue.serverTimestamp()
            if(password==confirm && aemail.isNotBlank() && password.isNotBlank() && confirm.isNotBlank()){
                auth.createUserWithEmailAndPassword(aemail,password).addOnSuccessListener {
                    val intent=Intent(this,SignUpActivity2::class.java)
                    val auid=auth.currentUser?.uid
                    if(auid!=null){
                        val user = mapOf(
                            "email" to aemail,
                            "uid" to auid,
                            "createdon" to date,
                            "profilecomplete" to false,
                            "approved" to false,
                            "userole" to UserRole.FREELANCER.name
                        )
                        db.collection("Users").document(auid).set(user, SetOptions.merge()).addOnSuccessListener {
                            Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, ProfileScreen1::class.java))

                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(this,"failed", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(this,"Enter all the fields Correctly", Toast.LENGTH_SHORT).show()
            }
        }
    }
}