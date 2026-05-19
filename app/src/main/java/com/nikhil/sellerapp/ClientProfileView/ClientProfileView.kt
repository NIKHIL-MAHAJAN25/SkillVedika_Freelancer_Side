package com.nikhil.sellerapp.ClientProfileView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.nikhil.sellerapp.R
import com.nikhil.sellerapp.databinding.FragmentClientProfileViewBinding
import com.nikhil.sellerapp.dataclasses.Client

class ClientProfileView : Fragment() {

    private var _binding: FragmentClientProfileViewBinding? = null
    private val binding get() = _binding!!

    private val db = Firebase.firestore
    private var clientUid: String? = null

    private var userLoaded = false
    private var clientLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        clientUid = arguments?.getString("uid")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientProfileViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.shimmerLayout.startShimmer()
        binding.AddReviews.setOnClickListener {
            val bundle = Bundle().apply {
                putString("uid", clientUid)
            }
            findNavController().navigate(R.id.addrev,bundle)
        }
        loadClientInfo()
    }

    private fun checkAndShowUI() {
        if (userLoaded && clientLoaded) {
            binding.shimmerLayout.stopShimmer()
            binding.shimmerLayout.visibility = View.GONE
            binding.profileCard.visibility = View.VISIBLE
            binding.paymentCard.visibility = View.VISIBLE
            binding.reviewsCard.visibility = View.VISIBLE
        }
    }

    private fun loadClientInfo() {
        val uid = clientUid ?: return

        // CLIENT DATA
        db.collection("Client")
            .document(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val client = snapshot.toObject<Client>()

                binding.tvCompanyName.text = client?.companyName

                updatePaymentChips(client?.paymentMethods ?: emptyList())

                val rating = client?.rating ?: 0.0
                if (rating > 0) {
                    binding.tvRating.text = rating.toString()
                    binding.tvNoRating.visibility = View.GONE
                } else {
                    binding.tvRating.visibility = View.GONE
                }

                val reviewCount = client?.reviews?.size ?: 0
                if (reviewCount > 0) {
                    binding.tvReviewCount.text = reviewCount.toString()
                    binding.tvNoReviewscount.visibility = View.GONE
                } else {
                    binding.tvReviewCount.visibility = View.GONE
                }

                clientLoaded = true
                checkAndShowUI()
            }
            .addOnFailureListener {
                clientLoaded = true
                checkAndShowUI()
            }

        // USER DATA
        db.collection("Users")
            .document(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val fullName = snapshot.getString("fullName")
                val imageUrl = snapshot.getString("profilePictureUrl")

                binding.tvName.text = fullName

                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(binding.profileImage)

                userLoaded = true
                checkAndShowUI()
            }
            .addOnFailureListener {
                userLoaded = true
                checkAndShowUI()
            }
    }

    private fun updatePaymentChips(methods: List<String>) {
        binding.chipPaymentMethods.removeAllViews()

        if (methods.isEmpty()) {
            binding.emptyPaymentState.visibility = View.VISIBLE
            return
        }

        binding.emptyPaymentState.visibility = View.GONE

        methods.forEach { method ->
            val chip = Chip(requireContext(), null, R.style.MyCustomChip)
            chip.text = method
            chip.isClickable = false
            chip.isCheckable = false
            binding.chipPaymentMethods.addView(chip)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}