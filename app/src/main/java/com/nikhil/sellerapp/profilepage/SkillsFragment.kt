package com.nikhil.sellerapp.profilepage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.nikhil.sellerapp.R
import com.nikhil.sellerapp.databinding.CustomDialogBinding
import com.nikhil.sellerapp.databinding.FragmentSkillsBinding
import com.nikhil.sellerapp.dataclasses.Freelancer
import com.nikhil.sellerapp.skills.Skill
import com.nikhil.sellerapp.skills.SkillData
import com.nikhil.sellerapp.skills.SkillsCat
import com.nikhil.sellerapp.skills.skillsadapter

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SkillsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SkillsFragment : Fragment() {
    private var _binding:FragmentSkillsBinding?=null
    private var param1: String? = null
    private var param2: String? = null
    val db= Firebase.firestore
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    var userlist= arrayListOf<SkillsCat>()
    val uid=auth.currentUser?.uid
    private var sclist= mutableListOf<SkillsCat>()
    lateinit var skadapter:skillsadapter
    private val binding get()=_binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding=FragmentSkillsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setuprecycler()
        loadskills()
        binding.btnAddskill.setOnClickListener {
            showdialog()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SkillsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SkillsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    private fun setuprecycler(){
        skadapter = skillsadapter(sclist,
            onDeleteCategory = { showDeleteConfirmation(it) },
            onDeleteSkill = { category, skill -> deleteSkill(category, skill) }
        )
        binding.recyclerskills.apply {
            layoutManager=LinearLayoutManager(requireContext())
            adapter=skadapter
        }
    }
    private fun deleteSkill(categoryName: String, skillName: String) {
        if (uid == null) return
        val skillToRemove = Skill(name = skillName, category = categoryName)
        db.collection("Freelancers").document(uid)
            .update("skills", FieldValue.arrayRemove(skillToRemove))
            .addOnSuccessListener {
                showSnackbar("'$skillName' removed")
                loadskills()
            }
            .addOnFailureListener {
                showSnackbar("Failed to remove skill")
            }
    }
    private fun showDeleteConfirmation(category: SkillsCat) {
        MaterialAlertDialogBuilder(requireContext(), R.style.MySimpleDialogStyle)
            .setTitle("Remove Skills")
            .setMessage("Remove all \"${category.categoryName}\" skills from your profile?")
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Remove") { _, _ ->
                deleteSkillCategory(category)
            }
            .show()
    }

    private fun deleteSkillCategory(category: SkillsCat) {
        if (uid == null) return
        // Rebuild the Skill objects for this category to remove them
        val skillsToRemove = category.skills.map { skillName ->
            Skill(name = skillName, category = category.categoryName)
        }
        val batch = db.batch()
        val docRef = db.collection("Freelancers").document(uid)
        skillsToRemove.forEach { skill ->
            batch.update(docRef, "skills", FieldValue.arrayRemove(skill))
        }
        batch.commit()
            .addOnSuccessListener {
                showSnackbar("${category.categoryName} skills removed")
                loadskills()
            }
            .addOnFailureListener {
                showSnackbar("Failed to remove skills")
            }
    }
    // in function we convert freelancer to object to fetch info
    private fun loadskills(){
      if(uid==null){
          return
      }
        db.collection("Freelancers").document(uid).get()
            .addOnSuccessListener { document->
                if(document.exists()){
                    val free=document.toObject<Freelancer>()
                    val flatskilllist=free?.skills?: emptyList()
                    val groupedmap=flatskilllist.groupBy { it.category }
                    val catlist=groupedmap.map { (categoryName,skillsInCat)->
                        SkillsCat(
                            categoryName=categoryName,
                            skills = skillsInCat.map{it.name}
                        )
                    }
                    skadapter.updatedata(catlist)
                    // Toggle empty state
                    if (catlist.isEmpty()) {
                        binding.emptyState.visibility = View.VISIBLE
                        binding.recyclerskills.visibility = View.GONE
                    } else {
                        binding.emptyState.visibility = View.GONE
                        binding.recyclerskills.visibility = View.VISIBLE
                    }

                }
            }
            .addOnFailureListener {
                showSnackbar("Error fetching skills:")
            }
    }
    private fun showdialog(){
        val dialogBinding=CustomDialogBinding.inflate(LayoutInflater.from(requireContext()))
        val asc=SkillData.getSkillCategories()
        val cname=asc.map{it.categoryName}
        val catadapter=ArrayAdapter(requireContext(),R.layout.skill_dropdown,cname)
        dialogBinding.categoryAutoComplete.setAdapter(catadapter)
        dialogBinding.categoryAutoComplete.setOnItemClickListener {  parent, _, position, _ ->
            val selectedCategoryName = parent.getItemAtPosition(position) as String
            val selectedCategory = asc.find { it.categoryName == selectedCategoryName }
            val skillsForCategory = selectedCategory?.skills ?: emptyList()

            val skillAdapter = ArrayAdapter(requireContext(),R.layout.skill_dropdown, skillsForCategory)
            dialogBinding.skillAutoComplete.setText("", false)
            dialogBinding.skillAutoComplete.setAdapter(skillAdapter)
            dialogBinding.skillInputLayout.isEnabled = true
        }
        MaterialAlertDialogBuilder(requireContext(),R.style.MySimpleDialogStyle)
            .setTitle("Add New Skill")

            .setView(dialogBinding.root) // Set your custom view
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Add") { dialog, _ ->
                val selectedSkill = dialogBinding.skillAutoComplete.text.toString()
                val category=dialogBinding.categoryAutoComplete.text.toString()
                if (selectedSkill.isNotEmpty() && category.isNotEmpty()) {
                val newskill= Skill(name= selectedSkill , category = category)

                    if (uid != null) {
                        db.collection("Freelancers").document(uid).update("skills",FieldValue.arrayUnion(newskill)).addOnSuccessListener {
                            showSnackbar("Added to your skills!")
                            loadskills()
                        }
                            .addOnFailureListener { e ->
                                showSnackbar("Error saving skill: ${e.message}")
                            }
                    }
                    showSnackbar("'$selectedSkill' added!")
                } else {
                    showSnackbar("Please select a skill.")
                }
            }
            .show()
    }
    private fun showSnackbar(message:String){
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }


}

