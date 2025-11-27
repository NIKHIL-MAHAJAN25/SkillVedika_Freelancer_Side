package com.nikhil.sellerapp.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.nikhil.sellerapp.R
import com.nikhil.sellerapp.databinding.FragmentSearchBinding
import com.nikhil.sellerapp.dataclasses.Project
import com.nikhil.sellerapp.dataclasses.ProjectStatus
import com.nikhil.sellerapp.homeSkill.DataSkill
import com.nikhil.sellerapp.homeSkill.JobAdapter
import com.nikhil.sellerapp.homeSkill.ServiceAdapter

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment() {
    private var _binding:FragmentSearchBinding?=null
    private val binding get()=_binding!!
    lateinit var serviceAdapter: ServiceAdapter
    private lateinit var jobAdapter: JobAdapter
    private val db= Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var param1: String? = null
    private var param2: String? = null
    private var firestoreListener: ListenerRegistration? = null

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
       _binding=FragmentSearchBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
        setupRecyclerView()
        loadJobPostings()
        loadinfo()

    }
    // --- 1. SETUP UI ---
    private fun setupRecyclerView() {
        // Initialize Adapter with Click Listener
        jobAdapter = JobAdapter { clickedProject ->
            // Handle what happens when a Freelancer clicks a Job
            // e.g., Navigate to Job Details Screen
            Toast.makeText(context, "Clicked: ${clickedProject.title}", Toast.LENGTH_SHORT).show()

            // Example Navigation:
            // val bundle = Bundle().apply { putString("projectId", clickedProject.projectId) }
            // findNavController().navigate(R.id.action_search_to_details, bundle)
        }

        binding.recyclerjobs.apply {
            adapter = jobAdapter
            layoutManager = LinearLayoutManager(requireContext()) // Vertical List
            setHasFixedSize(true)
        }
    }

    // --- 2. FETCH DATA (The Feed) ---
    private fun loadJobPostings() {
        // Safety: Remove old listener to prevent duplicates
        firestoreListener?.remove()

        // QUERY: "Give me all Projects where status is OPEN"
        // We don't want "ASSIGNED" or "COMPLETED" jobs in the feed.
        val query = db.collection("Projects")
            .whereEqualTo("status", ProjectStatus.OPEN.name)
        // Optional: Sort by newest first (Requires a Firestore Index)
        // .orderBy("postedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)

        firestoreListener = query.addSnapshotListener { snapshot, error ->
            // A. Error Handling
            if (error != null) {
                Log.e("JobFeed", "Listen failed", error)
                return@addSnapshotListener
            }

            // B. Lifecycle Safety (Prevent Crash if view is gone)
            if (_binding == null) return@addSnapshotListener

            // C. Process Data
            if (snapshot != null && !snapshot.isEmpty) {
                val jobList = snapshot.toObjects(Project::class.java)
                jobAdapter.submitList(jobList)

                // Optional: Hide "Empty State" view if you have one
                // binding.tvEmptyState.visibility = View.GONE
            } else {
                Log.d("JobFeed", "No open jobs found")
                jobAdapter.submitList(emptyList())

                // Optional: Show "No Jobs Available" text
                // binding.tvEmptyState.visibility = View.VISIBLE
            }
        }
    }
    private fun setup(){
    serviceAdapter=ServiceAdapter()
    binding.recyclerservices.apply {
        adapter=serviceAdapter
        }
    }
    private fun loadinfo(){
        db.collection("Skills").addSnapshotListener{ snapshot,error ->
            if(error!=null){
                return@addSnapshotListener
            }
            if(snapshot!=null && !snapshot.isEmpty){
                val skill=snapshot.toObjects(DataSkill::class.java)
                serviceAdapter.submitList(skill)
            }else{
                Log.d("Firestore Info", "Current skills data: null or empty")
                // If the collection is empty, submit an empty list to clear the UI.
                serviceAdapter.submitList(emptyList())
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
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