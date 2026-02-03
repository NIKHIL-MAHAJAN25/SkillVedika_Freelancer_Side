package com.nikhil.sellerapp.homeSkill
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil

import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nikhil.sellerapp.R
import com.nikhil.sellerapp.databinding.ServiceItemBinding

class ServiceAdapter(private val onServiceClick:(DataSkill) -> Unit):ListAdapter<DataSkill, ServiceAdapter.ViewHolder>(ServiceDiffCallback()){
    inner class ViewHolder(private val binding: ServiceItemBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(service:DataSkill){
            binding.serviceTitle.text=service.title
            Glide.with(itemView.context)
                .load(service.url)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(binding.serviceImage)
            binding.root.setOnClickListener{
                onServiceClick(service)
            }
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ServiceAdapter.ViewHolder {
        val binding = ServiceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ServiceAdapter.ViewHolder, position: Int) {
        val service=getItem(position)
        holder.bind(service)
    }
}
class ServiceDiffCallback : DiffUtil.ItemCallback<DataSkill>() {
    override fun areItemsTheSame(oldItem: DataSkill, newItem: DataSkill): Boolean {

        return oldItem.title==newItem.title
    }

    override fun areContentsTheSame(oldItem: DataSkill, newItem: DataSkill): Boolean {
        // Check if the item's data has changed. Kotlin data classes' `equals` works perfectly here.
        return oldItem == newItem
    }
}