package com.nikhil.sellerapp.home

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    lateinit var serviceAdapter: ServiceAdapter
    private lateinit var jobAdapter: JobAdapter
    private lateinit var searchResultsAdapter: JobAdapter  // reusing JobAdapter for search results

    private val db = Firebase.firestore
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
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setup()
        setupRecyclerView()
        setupSearchResultsRecyclerView()
        loadJobPostings()
        loadinfo()
        setupSearch()
        setupBackPress()
    }

    private fun setupBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.rvSearchResults.visibility == View.VISIBLE) {
                        binding.etSearch.text?.clear()
                        toggleSearch(false)
                        hideKeyboard()
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            })
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    toggleSearch(true)
                    performSkillSearch(query)
                } else {
                    toggleSearch(false)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                true
            } else {
                false
            }
        }
    }

    private fun performSkillSearch(query: String) {
        // arrayContains does exact match on one element of the list.
        // To support partial/case-insensitive matching, you'd need Algolia or store skills lowercased.
        // This does exact match — works well if user picks from chip/autocomplete.
        // For prefix-style typing, we search where any skill starts with the query using >= and <=.
        // Since arrayContains doesn't support prefix, we use a workaround:
        // fetch OPEN projects and filter client-side for partial match.

        db.collection("Projects")
            .whereEqualTo("status", ProjectStatus.OPEN.name)
            .get()
            .addOnSuccessListener { snapshot ->
                if (_binding == null) return@addOnSuccessListener

                if (snapshot.isEmpty) {
                    searchResultsAdapter.submitList(emptyList())
                    return@addOnSuccessListener
                }

                val lowerQuery = query.lowercase()

                val filtered = snapshot.toObjects(Project::class.java).filter { project ->
                    project.requiredSkills.any { skill ->
                        skill.lowercase().contains(lowerQuery)
                    }
                }

                searchResultsAdapter.submitList(filtered)
            }
            .addOnFailureListener { e ->
                Log.e("SkillSearch", "Search failed", e)
            }
    }

    private fun setupSearchResultsRecyclerView() {
        searchResultsAdapter = JobAdapter(
            onContactClicked = { project ->
                db.collection("Users").document(project.clientuid).get()
                    .addOnSuccessListener { doc ->
                        val image = doc.getString("profilePictureUrl") ?: ""
                        val bundle = Bundle().apply {
                            putString("receiverUid", project.clientuid)
                            putString("receiverName", project.clientName)
                            putString("receiverImage", image)
                        }
                        findNavController().navigate(R.id.chatlist, bundle)
                    }
            },
            onProfileClicked = { clientUid ->
                val bundle = Bundle().apply { putString("uid", clientUid) }
                findNavController().navigate(R.id.ClientProfile, bundle)
            }
        )

        binding.rvSearchResults.apply {
            adapter = searchResultsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun toggleSearch(isSearching: Boolean) {
        if (isSearching) {
            binding.homeContentGroup.visibility = View.GONE
            binding.rvSearchResults.visibility = View.VISIBLE
        } else {
            binding.homeContentGroup.visibility = View.VISIBLE
            binding.rvSearchResults.visibility = View.GONE
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun setupRecyclerView() {
        jobAdapter = JobAdapter(
            onContactClicked = { project ->
                db.collection("Users").document(project.clientuid).get()
                    .addOnSuccessListener { doc ->
                        Log.d("CLIENT_UID", project.clientuid)
                        Log.d("DOC_EXISTS", doc.exists().toString())
                        Log.d("FULL_DOC", doc.data.toString())
                        Log.d("IMAGE_FIELD", doc.getString("profilePictureUrl").toString())
                        val image = doc.getString("profilePictureUrl") ?: ""
                        val bundle = Bundle().apply {
                            putString("receiverUid", project.clientuid)
                            putString("receiverName", project.clientName)
                            putString("receiverImage", image)
                        }
                        findNavController().navigate(R.id.chatlist, bundle)
                    }
            },
            onProfileClicked = { clientUid ->
                val bundle = Bundle().apply { putString("uid", clientUid) }
                findNavController().navigate(R.id.ClientProfile, bundle)
            }
        )

        binding.recyclerjobs.apply {
            adapter = jobAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }
    }

    private fun loadJobPostings() {
        firestoreListener?.remove()

        val query = db.collection("Projects")
            .whereEqualTo("status", ProjectStatus.OPEN.name)

        firestoreListener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("JobFeed", "Listen failed", error)
                return@addSnapshotListener
            }
            if (_binding == null) return@addSnapshotListener

            if (snapshot != null && !snapshot.isEmpty) {
                jobAdapter.submitList(snapshot.toObjects(Project::class.java))
            } else {
                Log.d("JobFeed", "No open jobs found")
                jobAdapter.submitList(emptyList())
            }
        }
    }

    private fun setup() {
        serviceAdapter = ServiceAdapter { clicked ->
            val bundle = Bundle().apply { putString("skill", clicked.title) }
            findNavController().navigate(R.id.searchtoprojects, bundle)
        }
        binding.recyclerservices.apply {
            adapter = serviceAdapter
        }
    }

    private fun loadinfo() {
        db.collection("Skills").addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            if (snapshot != null && !snapshot.isEmpty) {
                serviceAdapter.submitList(snapshot.toObjects(DataSkill::class.java))
            } else {
                serviceAdapter.submitList(emptyList())
            }
        }
    }

    companion object {
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
        firestoreListener?.remove()
        _binding = null
    }
}