package com.nikhil.sellerapp.adminside

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nikhil.sellerapp.databinding.ItemAdminBinding
import com.nikhil.sellerapp.dataclasses.User
// here i am using a higher order function which passes button click function to activity which i am using ie instead of handling logic inside adpater i am
// telling activity to handle the click
class adminadapter(val list:List<User>,val approveclick:(User)->Unit): RecyclerView.Adapter<adminadapter.ViewHolder>(){
    inner class ViewHolder(val binding: ItemAdminBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val user = list[position]
            binding.useremail.text = user.email
            binding.username.text = user.fullName ?: ""
            binding.userole.text=user.userole.toString()
            binding.approveButton.setOnClickListener {
                approveclick(user)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): adminadapter.ViewHolder {
        val binding = ItemAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: adminadapter.ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

}