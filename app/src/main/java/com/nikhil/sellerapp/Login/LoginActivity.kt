package com.nikhil.sellerapp.Login

import android.content.Intent

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.nikhil.sellerapp.R
import com.nikhil.sellerapp.Signup.SignUpActivity2
import com.nikhil.sellerapp.adminside.ApprovalScreen
import com.nikhil.sellerapp.comprofile.Entercode
import com.nikhil.sellerapp.comprofile.ProfileScreen1
import com.nikhil.sellerapp.databinding.ActivityLoginBinding
import com.nikhil.sellerapp.home.HomeActivity

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private var auth:FirebaseAuth=FirebaseAuth.getInstance()
    val db=Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val oemail = "Nikhilmahajan8787@gmail.com"
        val opsswrd = "nikhil1234"
        binding.alrsignup.setOnClickListener {
            startActivity(Intent(this,SignUpActivity2::class.java))
            finish()
        }

        binding.btnSignin.setOnClickListener {
            val aemail = binding.etmailsignin2.text.toString()
            val psswrd = binding.etpsswrdsignin2.text.toString()
            if (aemail.isEmpty() || psswrd.isEmpty()) {
                Toast.makeText(this, "Enter email & password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //admin
            if (aemail.lowercase() == oemail.lowercase() && psswrd == opsswrd)
            {
                Toast.makeText(this, "Welcome", Toast.LENGTH_SHORT)
                startActivity(Intent(this, ApprovalScreen::class.java))
            }
            //admin
            else if(aemail!=oemail){
                auth.signInWithEmailAndPassword(aemail,psswrd).addOnSuccessListener {
                    val auid=auth.currentUser?.uid
                    if (auid != null) {
                        db.collection("Users").document(auid).get().addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
                                val filestatus = document.getBoolean("profilecomplete" )
                                val appstatus = document.getBoolean("approved")
                                when {
                                    !filestatus!! && !appstatus!! -> {
                                        Toast.makeText(
                                            this,
                                            "Please complete profile first",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        startActivity(Intent(this, ProfileScreen1::class.java))
                                    }

                                    filestatus && !appstatus!! -> {
                                        Toast.makeText(
                                            this,
                                            "Please enter code sent to your mail",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        startActivity(Intent(this, Entercode::class.java))
                                    }

                                    filestatus && appstatus == true -> {
                                        startActivity(Intent(this, HomeActivity::class.java))
                                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT)
                                            .show()

                                    }

                                    else -> {
                                        Toast.makeText(
                                            this,
                                            "Update your profile",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } else {
                                Toast.makeText(this, "Account doesn't exist", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(this,"check credentials again",Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(this, "Not Welcome", Toast.LENGTH_SHORT)
            }
        }
    }
}