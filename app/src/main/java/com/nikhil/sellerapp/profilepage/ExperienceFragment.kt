package com.nikhil.sellerapp.profilepage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.nikhil.sellerapp.R
import com.nikhil.sellerapp.databinding.FragmentAddExperienceBinding
import com.nikhil.sellerapp.databinding.FragmentExperienceBinding
import com.nikhil.sellerapp.dataclasses.Experience
import com.nikhil.sellerapp.dataclasses.Freelancer
import com.nikhil.sellerapp.experience.AddExperience
import com.nikhil.sellerapp.experience.ExpAdapter

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ExperienceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ExperienceFragment : Fragment() {
    private var _binding: FragmentExperienceBinding?=null
    private var param1: String? = null
    private var param2: String? = null
    lateinit var adapterr:ExpAdapter
    var userlist= arrayListOf<Experience>()
    private val binding get()=_binding!!
    val db= Firebase.firestore

    private val auth:FirebaseAuth=FirebaseAuth.getInstance()
    private val uid=auth.currentUser?.uid
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
       _binding=FragmentExperienceBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapterr= ExpAdapter(userlist){experience ->
            deleteexp(experience)

        }
        binding.recycler.layoutManager=LinearLayoutManager(requireContext())
        binding.recycler.adapter=adapterr
        userlist.clear()
        startlisten()


        binding.btnAddtask.setOnClickListener {

            findNavController().navigate(R.id.action_prof_to_exp)//you can define actions to go from district()experience to a main city defined in nav_graph
        }

    }
    private fun deleteexp(exp: Experience) {

        if (uid == null) return

        db.collection("Freelancers")
            .document(uid)
            .update(
                "experience",
                com.google.firebase.firestore.FieldValue.arrayRemove(exp)
            )
            .addOnSuccessListener {
                if (_binding == null) return@addOnSuccessListener
                Log.d("DELETE", "Experience removed")
                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                if (_binding == null) return@addOnFailureListener

                Log.e("DELETE", "Failed", it)
            }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ExperienceFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ExperienceFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    private fun startlisten() {
        if (uid != null) {
            db.collection("Freelancers").document(uid).addSnapshotListener { snapshot, error ->
                if (_binding == null) return@addSnapshotListener

                if (error != null) {
                    Log.e("Firestore Error", "Listen failed.", error)
                    return@addSnapshotListener
                }
                if(snapshot!=null && snapshot.exists()){
                    val freelancer=snapshot.toObject(Freelancer::class.java)
                    val newexp=freelancer?.experience?: emptyList()
                    adapterr.updateData(newexp)
                    if (newexp.isEmpty()) {
                        binding.emptyState.visibility = View.VISIBLE
                        binding.recycler.visibility = View.GONE
                    } else {
                        binding.emptyState.visibility = View.GONE
                        binding.recycler.visibility = View.VISIBLE
                    }
                }else{
                    binding.emptyState.visibility = View.VISIBLE
                    binding.recycler.visibility = View.GONE
                    Log.d("Firestore Info", "Current data: null")

                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}