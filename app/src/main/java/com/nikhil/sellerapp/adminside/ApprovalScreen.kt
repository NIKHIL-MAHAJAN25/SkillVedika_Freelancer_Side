package com.nikhil.sellerapp.adminside

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.nikhil.sellerapp.R
import com.nikhil.sellerapp.databinding.ActivityApprovalScreenBinding
import com.nikhil.sellerapp.dataclasses.User

class ApprovalScreen : AppCompatActivity() {
    lateinit var binding: ActivityApprovalScreenBinding
    private val db=Firebase.firestore
    private lateinit var adapter: adminadapter
    private val Users = arrayListOf<User>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityApprovalScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadusers()
        listen()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun loadusers(){
        adapter=adminadapter(Users){user->
            approveUser(user)

        }
        binding.adminrecycler.layoutManager = LinearLayoutManager(this)
        binding.adminrecycler.adapter = adapter
    }
    private fun listen(){
        db.collection("Users").whereEqualTo("approved",false).addSnapshotListener{snapshot,e->

            if(e!=null){
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener
            Users.clear()
            for(doc in snapshot.documents){
                val user=doc.toObject(User::class.java)
                user?.uid=doc.id
                if (user != null) {
                    Users.add(user)
                }
            }
            adapter.notifyDataSetChanged()

        }
    }
    private fun approveUser(user: User){
        val code = (100000..999999).random().toString()
        db.collection("Users").document(user.uid)
            .update("approvalCode", code)
            .addOnSuccessListener {
                Toast.makeText(this, "Code $code sent to ${user.email}", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error sending code", Toast.LENGTH_SHORT).show()
            }
    }
    }

