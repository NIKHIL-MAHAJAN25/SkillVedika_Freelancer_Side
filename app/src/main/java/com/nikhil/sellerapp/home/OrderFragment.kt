package com.nikhil.sellerapp.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.nikhil.sellerapp.R
import com.nikhil.sellerapp.databinding.FragmentOrderBinding
import com.nikhil.sellerapp.dataclasses.Project
import com.nikhil.sellerapp.dataclasses.ProjectStatus

class OrderFragment : Fragment() {

    private var _binding: FragmentOrderBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: OrderAdapter
    private var allProjects = listOf<Project>()

    private val db = FirebaseFirestore.getInstance()
    private var firestoreListener: ListenerRegistration? = null
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentOrderBinding.inflate(
                inflater,
                container,
                false
            )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        setupRecycler()

        setupTabs()

        loadProjects()
    }

    // ---------------------------------------------------
    // RECYCLER
    // ---------------------------------------------------

    private fun setupRecycler() {

        adapter = OrderAdapter(

            onClicked = { project ->

                Log.d(
                    "PROJECT_CLICK",
                    project.title
                )
            },

            onMarkCompleted = { project ->

                markProjectCompleted(project)
            },

            onCancelProject = { project ->

                cancelProject(project)
            },
            onLeaveReview = { project ->
                if (!isAdded || _binding == null) return@OrderAdapter

                val bundle = Bundle().apply {
                    putString("uid", project.clientuid ?: "")
                    putString("freeName", project.freename ?: "Freelancer")
                    putString("projectId", project.projectid)
                }
                findNavController().navigate(R.id.addrev, bundle)
            }
        )

        binding.projectrecycler.apply {

            this.adapter = this@OrderFragment.adapter

            layoutManager =
                LinearLayoutManager(requireContext())



            isNestedScrollingEnabled = false
        }



    }

    // ---------------------------------------------------
    // TAB FILTER
    // ---------------------------------------------------

    private fun setupTabs() {

        binding.tabLayoutStatus.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {

                override fun onTabSelected(
                    tab: TabLayout.Tab?
                ) {

                    applyFilter(
                        tab?.position ?: 0
                    )
                }

                override fun onTabUnselected(
                    tab: TabLayout.Tab?
                ) {
                }

                override fun onTabReselected(
                    tab: TabLayout.Tab?
                ) {
                }
            }
        )
    }

    // ---------------------------------------------------
    // FILTER
    // ---------------------------------------------------

    private fun applyFilter(
        tabPosition: Int
    ) {

        val status =
            when (tabPosition) {

                0 ->
                    ProjectStatus.ASSIGNED.name

                1 ->
                    ProjectStatus.COMPLETED.name

                2 ->
                    ProjectStatus.CANCELLED.name

                else ->
                    ProjectStatus.ASSIGNED.name
            }

        val filtered =
            allProjects.filter {

                it.status == status
            }

        Log.d(
            "FILTERED_PROJECTS",
            filtered.toString()
        )
        Log.d("APPLY_FILTER", "tab=$tabPosition status=$status projects=${allProjects.map { it.status }}")
        adapter.submitList(filtered)
        if (filtered.isEmpty()) {
            val (title, sub) = when (tabPosition) {
                0 -> Pair("No Active Orders", "Contact Clients to get Started")
                1 -> Pair("No Completed Projects", "Finish your assigned work to see it here")
                2 -> Pair("No Cancelled Projects", "Hope it stays that way!")
                else -> Pair("No Orders Yet", "Contact Clients to get Started")
            }
            binding.tvEmpty.text = title
            binding.tvEmptySub.text = sub
            binding.tvEmpty.visibility = View.VISIBLE
            binding.tvEmptySub.visibility = View.VISIBLE
            binding.projectrecycler.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.tvEmptySub.visibility = View.GONE
            binding.projectrecycler.visibility = View.VISIBLE
        }
    }

    // ---------------------------------------------------
    // LOAD ALL PROJECTS
    // ---------------------------------------------------

    private fun loadProjects() {

        val freelancerUid =
            auth.currentUser?.uid ?: return

        Log.d(
            "FREELANCER_UID",
            freelancerUid
        )



        firestoreListener =
            db.collection("Projects")

                .whereEqualTo(
                    "freeuid",
                    freelancerUid
                )

                .addSnapshotListener { snapshot, error ->

                    if (error != null) {

                        Log.e(
                            "ORDER_FRAGMENT",
                            "Firestore Error",
                            error
                        )

                        return@addSnapshotListener
                    }

                    Log.d(
                        "SNAPSHOT_SIZE",
                        snapshot?.size().toString()
                    )

                    if (_binding == null)
                        return@addSnapshotListener

                    if (snapshot != null) {

                        val projectList =
                            mutableListOf<Project>()

                        snapshot.documents.forEach { doc ->

                            Log.d(
                                "DOC_DATA",
                                doc.data.toString()
                            )

                            val project =
                                doc.toObject(
                                    Project::class.java
                                )

                            if (project != null) {

                                project.projectid =
                                    doc.id

                                projectList.add(project)
                            }
                        }

                        Log.d(
                            "FINAL_PROJECT_LIST",
                            projectList.toString()
                        )

                        allProjects = projectList

                        applyFilter(
                            binding.tabLayoutStatus.selectedTabPosition
                        )
                    }
                }
    }

    // ---------------------------------------------------
    // COMPLETE
    // ---------------------------------------------------

    private fun markProjectCompleted(
        project: Project
    ) {

        db.collection("Projects")
            .document(project.projectid)

            .update(
                "status",
                ProjectStatus.COMPLETED.name
            )
    }

    // ---------------------------------------------------
    // CANCEL
    // ---------------------------------------------------

    private fun cancelProject(
        project: Project
    ) {

        db.collection("Projects")
            .document(project.projectid)

            .update(
                "status",
                ProjectStatus.CANCELLED.name
            )
    }

    override fun onDestroyView() {
        super.onDestroyView()

        firestoreListener?.remove()

        _binding = null
    }
}