package com.nikhil.sellerapp.profilepage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.nikhil.sellerapp.R
import com.nikhil.sellerapp.databinding.FragmentEditBinding
import com.nikhil.sellerapp.dataclasses.Freelancer
import com.nikhil.sellerapp.dataclasses.User

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditFragment : Fragment() {
    // TODO: Rename and change types of parameters
    val developerList=listOf(
        "Core Programming Software Development",
        "Frontend Development",
        "Backend Development",
        "Mobile App development",
        "Database and Data Management",
        "Cloud and DevOps",
        "Product Design (UI/UX)",
        "Project Management & Methodologies",
        "Data Science and Machine Learning",


    )
    private var _binding:FragmentEditBinding?=null
    private val auth:FirebaseAuth=FirebaseAuth.getInstance()
    private val uid=auth.currentUser?.uid
    val db=Firebase.firestore
    var oname:String?=null
    var obio:String?=null
    var oprimskill:String?=null
    var orate:String?=null
    private val binding get()=_binding!!
    private var param1: String? = null
    private var param2: String? = null

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
        _binding=FragmentEditBinding.inflate(inflater,container,false)
       return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadadapter()
        loadinfo()
        loadotherinfo()
        binding.btnSave.setOnClickListener {
            updateinfo()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EditFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    fun loadadapter()
    {
        val adapter = ArrayAdapter(requireContext(),R.layout.item_dropdown_interest,developerList)
        binding.etprim.setAdapter(adapter)
        binding.etprim.setOnClickListener {
            binding.etprim.showDropDown()
        }
    }
    private fun updateinfo(){
        if(uid!=null){
            val name=binding.etname.text.toString()
            val bio=binding.etdesc.text.toString()
            val primskill=binding.etprim.text.toString()
            val rate=binding.etproject.text.toString()
            val rated=rate.toDoubleOrNull()?:0.0
            val freelancer= mapOf(
                "primaryskill" to primskill,
                "projectRate" to rated,
                "name" to name,
            )

            val user= mapOf(
                "fullName" to name,
                "bio" to bio,
            )
            if(name!=oname){
                updateuser(user)
                updatefreelancer(freelancer)

            }
            if(primskill!=oprimskill || rate!=orate){
                updatefreelancer(freelancer)
            }
            if(bio!=obio){
                updateuser(user)
            }

        }
    }
    private fun updatefreelancer(details:Map<String, Any>){
        if(uid!=null){
            db.collection("Freelancers").document(uid).update(details).addOnSuccessListener {
                showtoast("Data Updated")

            }.addOnFailureListener {
                showtoast("Data Could not be updated")
            }
        }

    }
    private fun updateuser(details:Map<String, String>){
        if(uid!=null){
            db.collection("Users").document(uid).update(details).addOnSuccessListener {
                showtoast("Data Updated")

            }.addOnFailureListener {
                showtoast("Data Could not be updated")
            }
        }

    }
    private fun loadinfo(){
        if(uid!=null){
            db.collection("Users").document(uid).addSnapshotListener { snapshot, error ->
                // firestore makes an asynchornous call even when fragment is destroyed so handling it by making a copy of binding to persist even when binding is destroyed
                val b=_binding?:return@addSnapshotListener
                if(error!=null){
                    return@addSnapshotListener
                }
                if(snapshot!=null && snapshot.exists()){
                    val user=snapshot.toObject<User>()
                    b.etname.setText(user?.fullName)
                    b.etPhone.setText(user?.phoneNumber)
                    b.etemail.setText(user?.email)
                    b.etdesc.setText(user?.bio)
                    oname = user?.fullName
                    obio = user?.bio
                }

            }
        }
    }
    private fun loadotherinfo(){
        if(uid!=null){
            db.collection("Freelancers").document(uid).addSnapshotListener { snapshot, error ->
                // firestore makes an asynchornous call even when fragment is destroyed so handling it by making a copy of binding to persist even when binding is destroyed
                val b=_binding?:return@addSnapshotListener
                if(error!=null){
                    return@addSnapshotListener
                }
                if(snapshot!=null && snapshot.exists()){
                    val user=snapshot.toObject<Freelancer>()
                    b.etprim.setText(user?.primaryskill)
                    b.etproject.setText(user?.projectRate.toString())
                    oprimskill = user?.primaryskill
                   orate=user?.projectRate.toString()
                }

            }
        }
    }
    private fun showtoast(message:String){
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}