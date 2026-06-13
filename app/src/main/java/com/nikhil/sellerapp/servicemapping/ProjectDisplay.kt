package com.nikhil.sellerapp.servicemapping

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.nikhil.sellerapp.R
import com.nikhil.sellerapp.databinding.FragmentProjectDisplayBinding
import com.nikhil.sellerapp.dataclasses.Project
import com.nikhil.sellerapp.homeSkill.JobAdapter
import com.nikhil.sellerapp.homeSkill.ServiceAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProjectDisplay.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProjectDisplay : Fragment() {
    // TODO: Rename and change types of parameters
    private var firestoreListener: ListenerRegistration? = null
    private var _binding:FragmentProjectDisplayBinding?=null
    // step 1 for adapters
    lateinit var projadapter:JobAdapter
    lateinit var seveadapter:ServiceAdapter
    val binding get()=_binding!!
    val db=Firebase.firestore
    lateinit var skill:String
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        skill=requireArguments().getString("skill")!!
        Log.e("DEBUG","${skill}")
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       _binding= FragmentProjectDisplayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvresults.text = skill
        setuprecycler()

        fetch()


    }

    fun setuprecycler()
    {
        projadapter= JobAdapter(onContactClicked =  {  project ->
            db.collection("Users").document(project.clientuid).get().addOnSuccessListener { doc->
                Log.d("CLIENT_UID", project.clientuid)

                Log.d("DOC_EXISTS", doc.exists().toString())

                Log.d("FULL_DOC", doc.data.toString())

                Log.d("IMAGE_FIELD", doc.getString("profilePictureUrl").toString())
                val image = doc.getString("profilePictureUrl")?:""
                val bundle = Bundle().apply {

                    putString("receiverUid", project.clientuid)

                    putString("receiverName", project.clientName)

                    putString("receiverImage", image)
                }
                findNavController().navigate(
                    R.id.chatlist,
                    bundle
                )

            }
        },
            onProfileClicked = { clientUid ->

                val bundle = Bundle().apply {

                    putString("uid", clientUid)
                }

                findNavController().navigate(
                    R.id.ClientProfile,
                    bundle
                )
            }
        )

        binding.recyclerquery.apply {
            adapter = projadapter
            layoutManager = LinearLayoutManager(requireContext()) // Vertical List
            setHasFixedSize(true)
        }
    }
    fun fetch()
    {
        Log.e("fire","${skill}")
        Log.e("Firestore", "Category length: ${skill.length}")
        db.collection("Projects").whereEqualTo("category", skill).get().addOnSuccessListener { snapshot->

            if (snapshot.isEmpty) {
                Log.e("Firestore", "No projects found for category:$skill")
                showSnackbar("No projects found for this skill")
            } else {
                val projects = snapshot.toObjects(Project::class.java)
                projadapter.submitList(null) {
                    projadapter.submitList(projects) {
                        // Force UI refresh
                        binding.recyclerquery.post {
                            binding.recyclerquery.requestLayout()
                        }
                    }
                }
                Log.e("fire", "${skill}")
                Log.e("Firestore", "Query success:${projects}")
            }
        }.addOnFailureListener {

            Log.e("Firestore", "Query failed")
        }

    }
    private fun showSnackbar(message:String){
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProjectDisplay.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProjectDisplay().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}