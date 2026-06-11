package com.nikhil.sellerapp.homeSkill

import android.graphics.Paint
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.nikhil.sellerapp.R
import com.nikhil.sellerapp.databinding.ProjectitemBinding
import com.nikhil.sellerapp.dataclasses.Project
import com.nikhil.sellerapp.dataclasses.ProjectStatus
import kotlin.or

class JobAdapter (private val onContactClicked: (Project) -> Unit, private val onProfileClicked: (String) -> Unit) : ListAdapter<Project, JobAdapter.ViewHolder>(JobDiffCallback) {

    // 1. THE FRAME BUILDER (ViewHolder)
    inner class ViewHolder(val binding:ProjectitemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(project: Project) {
            binding.apply {
                binding.tvClientName.paintFlags =
                    binding.tvClientName.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                tvTitle.text = project.title
                tvDescription.text = project.description
                tvBudget.text = "₹${project.budget.toInt()}" // Format as currency
                tvClientName.text = "Posted by ${project.clientName}"

                // 2. TIME AGO LOGIC (e.g., "2 hours ago")
//                val timeMillis = project.postedAt?.toDate()?.time ?: System.currentTimeMillis()
//                tvPostedTime.text = android.text.format.DateUtils.getRelativeTimeSpanString(
//                    timeMillis,
//                    System.currentTimeMillis(),
//                    android.text.format.DateUtils.MINUTE_IN_MILLIS
//                )

                // 3. STATUS CHIP COLORING
                chipStatus.text = project.status
                if (project.status == ProjectStatus.OPEN.name) {
                    chipStatus.setChipBackgroundColorResource(R.color.bgdark) // Define in colors.xml
                    chipStatus.setTextColor(root.context.getColor(R.color.darkgreen))
                } else {
                    chipStatus.setChipBackgroundColorResource(R.color.bgdark)
                    chipStatus.setTextColor(root.context.getColor(R.color.black))
                }

                // 4. DYNAMIC SKILLS CHIPS
                // Important: Remove old views because RecyclerView reuses layouts!
                chipGroupSkills.removeAllViews()

                // Take only top 3 skills to prevent overcrowding
                project.requiredSkills.take(3).forEach { skillName ->
                    val chip = Chip(root.context)
                    chip.text = skillName
                    chip.textSize = 12f
                    chip.setEnsureMinTouchTargetSize(false) // Makes chip smaller/compact
                    chipGroupSkills.addView(chip)
                }

                // 5. CLICK LISTENER
                btncontact.setOnClickListener {

                    onContactClicked(project)
                }

                tvClientName.setOnClickListener {

                    onProfileClicked(project.clientuid)
                }
            }
        }
    }




    // 2. INFLATE XML
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ProjectitemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    // 3. BIND DATA
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val project = getItem(position)
        holder.bind(project)
    }

    // 4. DIFF UTIL (Efficiency Check)
    companion object JobDiffCallback : DiffUtil.ItemCallback<Project>() {
        override fun areItemsTheSame(oldItem: Project, newItem: Project): Boolean {
            return oldItem.projectid == newItem.projectid
        }

        override fun areContentsTheSame(oldItem: Project, newItem: Project): Boolean {
            return oldItem == newItem
        }
    }
}


