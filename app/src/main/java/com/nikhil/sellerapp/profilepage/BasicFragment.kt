package com.nikhil.sellerapp.profilepage

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import com.nikhil.sellerapp.ClientProfileView.ReviewAdapter
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.nikhil.sellerapp.R
import com.nikhil.sellerapp.certificate.CertAdapter
import com.nikhil.sellerapp.databinding.FragmentBasicBinding
import com.nikhil.sellerapp.dataclasses.Certification
import com.nikhil.sellerapp.dataclasses.Freelancer
import com.nikhil.sellerapp.dataclasses.Qualification
import com.nikhil.sellerapp.qualification.QualAdapter

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BasicFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BasicFragment : Fragment() {
    private var _binding:FragmentBasicBinding?=null
    private var param1: String? = null
    private var param2: String? = null
    private val binding get()=_binding!!
    private val db=Firebase.firestore
    private var flag: Boolean =false
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var reviewListener: ListenerRegistration? = null
    private var qualListener: ListenerRegistration? = null
    private var certListener: ListenerRegistration? = null
    private val uid=auth.currentUser?.uid
    var userlist= arrayListOf<Certification>()
    val qlist= arrayListOf<Qualification>()
    lateinit var qadapter:QualAdapter
    lateinit var adapterr: CertAdapter
    private lateinit var reviewAdapter: ReviewAdapter

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
       _binding=FragmentBasicBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapterr= CertAdapter(userlist){certification ->
            deletecert(certification)

        }
        binding.recylercert.layoutManager=LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL,false)
        binding.recylercert.adapter=adapterr
        userlist.clear()
        setupReviewRecycler()





        qadapter= QualAdapter(qlist){ qualification ->
            deletequal(qualification)
        }
        binding.recylerqual.layoutManager=LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recylerqual.adapter=qadapter

        qlist.clear()
        startlisten()
        startlistencert()

        binding.btnCertEdit.setOnClickListener {
            findNavController().navigate(R.id.prof_to_cert)
        }
        binding.btnQualEdit.setOnClickListener {
            findNavController().navigate(R.id.prof_to_qual)
        }
    }
    private fun deletecert(cert: Certification) {

        if (uid == null) return

        db.collection("Freelancers")
            .document(uid)
            .update(
                "certification",
                com.google.firebase.firestore.FieldValue.arrayRemove(cert)
            )
            .addOnSuccessListener {
                if (_binding == null || !isAdded) return@addOnSuccessListener

                Log.d("DELETE", "Experience removed")
                Toast.makeText(requireContext(), "Certificate Removed", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Log.e("DELETE", "Failed", it)
            }
    }
    private fun deletequal(qual: Qualification) {

        if (uid == null) return

        db.collection("Freelancers")
            .document(uid)
            .update(
                "qualification",
                com.google.firebase.firestore.FieldValue.arrayRemove(qual)
            )
            .addOnSuccessListener {
                if (_binding == null) return@addOnSuccessListener
                Log.d("DELETE", "Experience removed")
                Toast.makeText(requireContext(), "Qualification Removed", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                if (_binding == null) return@addOnFailureListener

                Log.e("DELETE", "Failed", it)
            }
    }
    private fun setupReviewRecycler() {

        reviewAdapter = ReviewAdapter()

        binding.recylerReview.apply {

            adapter = reviewAdapter

            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )

            isNestedScrollingEnabled = false
        }

        startReviewListener()
    }
    private fun startReviewListener() {

        if (uid != null) {

            reviewListener = db.collection("Freelancers")
                .document(uid)

                .addSnapshotListener { snapshot, error ->


                    if (_binding == null) return@addSnapshotListener

                    if (error != null) {

                        Log.e(
                            "Firestore Review",
                            "Review listener failed",
                            error
                        )

                        return@addSnapshotListener
                    }


                    if (snapshot != null && snapshot.exists()) {

                        val freelancer =
                            snapshot.toObject(Freelancer::class.java)

                        // RATING
                        val rating =
                            freelancer?.rating ?: 0.0

                        if (rating > 0) {

                            binding.tvRating.text =
                                "⭐ %.1f".format(rating)

                        } else {

                            binding.tvRating.text =
                                "No ratings"
                        }

                        // REVIEWS
                        val reviews =
                            freelancer?.reviews ?: emptyList()

                        if (reviews.isNotEmpty()) {

                            val sortedReviews =
                                reviews.sortedByDescending {
                                    it.timestamp
                                }

                            reviewAdapter.submitList(
                                sortedReviews
                            )

                        } else {

                            reviewAdapter.submitList(
                                emptyList()
                            )
                        }

                    } else {

                        Log.d(
                            "Firestore Review",
                            "Freelancer document missing"
                        )
                    }
                }
        }
    }
    private fun startlistencert(){
        if(uid!=null){
            certListener = db.collection("Freelancers").document(uid).addSnapshotListener{snapshot,error->
                if (_binding == null) return@addSnapshotListener

                if(error!=null){
                    Log.e("Firestore error","Listen failed",error)
                    return@addSnapshotListener
                }
                if(snapshot!=null && snapshot.exists()){
                    val freelancer=snapshot.toObject(Freelancer::class.java)
                    val newcert=freelancer?.certification?: emptyList()
                    adapterr.updateData(newcert)
                }else{
                    Log.d("Firestore Info", "Current data: null")

                }
            }
        }
    }
private fun startlisten(){
    if(uid!=null){
        qualListener = db.collection("Freelancers").document(uid).addSnapshotListener{snapshot,error->
            if (_binding == null) return@addSnapshotListener

            if(error!=null){
                Log.e("Firestore error","Listen failed",error)
                return@addSnapshotListener
            }
            if(snapshot!=null && snapshot.exists()){
                val freelancer=snapshot.toObject(Freelancer::class.java)
                val newqual=freelancer?.qualification?: emptyList()
                qadapter.updateData(newqual)
            }else{
                Log.d("Firestore Info", "Current data: null")

            }
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
         * @return A new instance of fragment BasicFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BasicFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onDestroyView() {
        Log.e("LISTENER_DEBUG", "BasicFragment destroyed")

        reviewListener?.remove()
        qualListener?.remove()
        certListener?.remove()

        reviewListener = null
        qualListener = null
        certListener = null
        _binding=null
        super.onDestroyView()
    }
}