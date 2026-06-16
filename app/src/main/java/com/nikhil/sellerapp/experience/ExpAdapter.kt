package com.nikhil.sellerapp.experience

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nikhil.sellerapp.R
import com.nikhil.sellerapp.databinding.ExperienceitemBinding
import com.nikhil.sellerapp.dataclasses.Experience
import com.nikhil.sellerapp.dataclasses.Freelancer

class ExpAdapter( val list:MutableList<Experience>, private val onDeleteClick:(Experience)->Unit):RecyclerView.Adapter<ExpAdapter.ViewHolder>() {
    inner class ViewHolder(val binding:ExperienceitemBinding):RecyclerView.ViewHolder(binding.root)
    {
        fun binddata(position: Int){
            val item=list[position]
            binding.txtdates.setText("${list[position].startDate} - ${list[position].endDate}")
            binding.txtCompany.setText(list[position].companyname)
            binding.txtDescription.setText(list[position].description)
            binding.txtDesignation.setText(list[position].designation)
            Glide.with(itemView.context)

                .load(item.cologo)
                .circleCrop()
                .placeholder(R.drawable.baseline_error_24)
                .into(binding.imgCompanyLogo)
            binding.btnDelete.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpAdapter.ViewHolder {
        val binding=ExperienceitemBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpAdapter.ViewHolder, position: Int) {
        holder.binddata(position)
    }

    override fun getItemCount(): Int {
        return list.size
    }
   fun updateData(newList: List<Experience>)
    {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }


}