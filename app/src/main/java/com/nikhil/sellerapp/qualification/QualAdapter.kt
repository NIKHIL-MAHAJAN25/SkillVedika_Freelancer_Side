package com.nikhil.sellerapp.qualification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nikhil.sellerapp.databinding.QualificationItemBinding
import com.nikhil.sellerapp.dataclasses.Experience
import com.nikhil.sellerapp.dataclasses.Qualification


class QualAdapter(val qlist:MutableList<Qualification>,private val onDeleteClick:(Qualification)->Unit):RecyclerView.Adapter<QualAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: QualificationItemBinding):RecyclerView.ViewHolder(binding.root)
    {
        fun bindData(position: Int){
            val item=qlist[position]
            binding.tvRollNo.setText(qlist[position].rollNo)
            binding.tvInstName.setText(qlist[position].instName)
            binding.tvAggregate.setText("${qlist[position].aggregate}/${qlist[position].max}")
            binding.tvEndYear.setText(qlist[position].endYear)
            binding.tvDegree.setText(qlist[position].degree)
            binding.btnMenu.setOnClickListener {
                onDeleteClick(item)
            }

        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QualAdapter.ViewHolder {
       val binding=QualificationItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
       return qlist.size
    }

    override fun onBindViewHolder(holder: QualAdapter.ViewHolder, position: Int) {
        holder.bindData(position)
    }
    fun updateData(newList: List<Qualification>)
    {
        qlist.clear()
        qlist.addAll(newList)
        notifyDataSetChanged()
    }

}