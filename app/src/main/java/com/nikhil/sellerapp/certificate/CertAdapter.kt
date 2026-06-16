package com.nikhil.sellerapp.certificate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nikhil.sellerapp.databinding.CertificationItemBinding
import com.nikhil.sellerapp.databinding.ExperienceitemBinding
import com.nikhil.sellerapp.dataclasses.Certification
import kotlinx.coroutines.flow.combineTransform

class CertAdapter(val list:MutableList<Certification>, val onDeleteClick:(Certification)->Unit):RecyclerView.Adapter<CertAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: CertificationItemBinding):RecyclerView.ViewHolder(binding.root){
        fun bindata(position: Int){
            val item=list[position]
            binding.tvCertNo.setText(list[position].certNo)
            binding.tvIssueDate.setText(list[position].issuedate)
            binding.tvSkillName.setText(list[position].skillname)
            binding.tvIssuingCompany.setText(list[position].issuingcompany)
            binding.btnMenu.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CertAdapter.ViewHolder {
        val binding=CertificationItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CertAdapter.ViewHolder, position: Int) {
        holder.bindata(position)
    }

    override fun getItemCount(): Int {
        return list.size
    }
    fun updateData(newList: List<Certification>){
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}