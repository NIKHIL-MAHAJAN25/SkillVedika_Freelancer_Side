package com.nikhil.sellerapp.skills

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.nikhil.sellerapp.databinding.SkillCateoryItemBinding

class skillsadapter(val list:MutableList<SkillsCat>,val onDeleteCategory: (SkillsCat) -> Unit, val onDeleteSkill: (categoryName: String, skillName: String) -> Unit):RecyclerView.Adapter<skillsadapter.ViewHolder>() {
    inner class ViewHolder(val binding: SkillCateoryItemBinding):RecyclerView.ViewHolder(binding.root)
    {
        fun bindata(position: Int){
            val item=list[position]

             binding.tvct.text=list[position].categoryName
            binding.skillChipGroup.removeAllViews()
            for (sname in item.skills) {
                val chip = Chip(itemView.context)
                chip.text = sname
                chip.setOnLongClickListener {
                    onDeleteSkill(item.categoryName, sname)
                    true
                }
                binding.skillChipGroup.addView(chip)
            }
            // Long press on the whole item card to delete
            binding.root.setOnLongClickListener {
                onDeleteCategory(item)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val binding=SkillCateoryItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       holder.bindata(position)
    }
    fun updatedata(newList:List<SkillsCat>){
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}