package com.nikhil.sellerapp.comprofile

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.nikhil.sellerapp.R
import com.nikhil.sellerapp.databinding.ActivityEntercodeBinding
import com.nikhil.sellerapp.home.HomeActivity
import com.nikhil.sellerapp.mailretro.ApiResponse
import com.nikhil.sellerapp.mailretro.Retromail
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.random.Random

class Entercode : AppCompatActivity() {

    lateinit var binding: ActivityEntercodeBinding

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val auid = auth.currentUser?.uid

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityEntercodeBinding.inflate(layoutInflater)

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        val boxes = listOf(
            binding.etOtp1,
            binding.etOtp2,
            binding.etOtp3,
            binding.etOtp4,
            binding.etOtp5,
            binding.etOtp6
        )

        for (i in boxes.indices) {

            boxes[i].addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    if (s?.length == 1 && i < boxes.size - 1) {
                        boxes[i + 1].requestFocus()
                    }

                    else if (s.isNullOrEmpty() && i > 0) {
                        boxes[i - 1].requestFocus()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

        binding.btnResendCode.setOnClickListener {

            if (auid != null) {

                generate { code ->

                    val user = mapOf(
                        "approvalCode" to code
                    )

                    db.collection("Users")
                        .document(auid)
                        .update(user)
                        .addOnSuccessListener {

                            fetchmail(auid) { mail ->

                                if (mail != null) {
                                    sendOtp(
                                        mail,
                                        code.toString()
                                    )
                                }
                            }

                            Toast.makeText(
                                this,
                                "Security code sent on your mail",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->

                            Toast.makeText(
                                this,
                                "Failed: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
        }

        binding.btnVerifyOtp.setOnClickListener {

            val code = boxes.joinToString("") {
                it.text.toString()
            }

            if (auid != null) {

                fetchcode(auid) { securecode ->

                    if (securecode != null &&
                        securecode == code
                    ) {

                        db.collection("Users")
                            .document(auid)
                            .update("approved", true)

                        fetchname(auid) { name ->

                            val lancer = mapOf(
                                "name" to name,
                                "uid" to auid,
                                "profcomp" to false
                            )

                            db.collection("Freelancers")
                                .document(auid)
                                .set(
                                    lancer,
                                    SetOptions.merge()
                                )
                        }

                        Log.d("otp", "otp verified")

                        startActivity(
                            Intent(
                                this,
                                HomeActivity::class.java
                            )
                        )

                        finish()
                    }

                    else {

                        Toast.makeText(
                            this,
                            "Invalid OTP",
                            Toast.LENGTH_SHORT
                        ).show()

                        Log.d("otp", "Invalid OTP")
                    }
                }
            }
        }
    }

    fun fetchcode(
        auid: String,
        onResult: (String?) -> Unit
    ) {

        db.collection("Users")
            .document(auid)
            .get()
            .addOnSuccessListener { document ->

                if (document != null &&
                    document.exists()
                ) {

                    val securecode =
                        document.get("approvalCode")

                    val securecode2 =
                        securecode?.toString()

                    Log.d(
                        "code",
                        "Code:$securecode"
                    )

                    onResult(securecode2)
                }

                else {
                    onResult(null)
                }
            }
    }

    fun fetchname(
        auid: String,
        onResult: (String?) -> Unit
    ) {

        db.collection("Users")
            .document(auid)
            .get()
            .addOnSuccessListener { document ->

                if (
                    document != null &&
                    document.exists()
                ) {

                    val name =
                        document.getString("fullName")

                    onResult(name)
                }

                else {
                    onResult(null)
                }
            }
    }

    private fun fetchmail(
        uid: String,
        onResult: (String?) -> Unit
    ) {

        db.collection("Users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                if (
                    document.exists()
                ) {

                    val email =
                        document.getString("email")

                    onResult(email)
                }

                else {
                    onResult(null)
                }
            }
    }

    private fun generate(
        onResult: (Int?) -> Unit
    ) {

        val random =
            Random.nextInt(
                100000,
                999999
            )

        onResult(random)
    }

    private fun sendOtp(
        email: String,
        otp: String
    ) {

        val data = mapOf(
            "email" to email,
            "otp" to otp
        )

        Retromail.instance
            .sendOtp(data)
            .enqueue(
                object : Callback<ApiResponse> {

                    override fun onResponse(
                        call: Call<ApiResponse>,
                        response: Response<ApiResponse>
                    ) {

                        val res = response.body()

                        val msg =
                            res?.message
                                ?: "Unexpected response"

                        Log.d(
                            "mail",
                            "mail sent"
                        )

                        Toast.makeText(
                            this@Entercode,
                            msg,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onFailure(
                        call: Call<ApiResponse>,
                        t: Throwable
                    ) {

                        Log.e(
                            "mail",
                            "mail not sent"
                        )

                        Toast.makeText(
                            this@Entercode,
                            "Failed: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
    }
}