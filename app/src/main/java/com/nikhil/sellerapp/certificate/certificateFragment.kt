package com.nikhil.sellerapp.certificate

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.nikhil.sellerapp.R
import com.nikhil.sellerapp.databinding.FragmentCertificateBinding
import com.nikhil.sellerapp.dataclasses.Certification
import com.nikhil.sellerapp.dataclasses.Experience
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [certificateFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class certificateFragment : Fragment() {
    private var _binding:FragmentCertificateBinding?=null
    private val binding get()=_binding!!
    val db=Firebase.firestore
    lateinit var adapterr:CertAdapter
    private val auth:FirebaseAuth=FirebaseAuth.getInstance()
    private val uid=auth.currentUser?.uid
    var userlist= arrayListOf<Certification>()
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
       _binding=FragmentCertificateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.btnSave.setOnClickListener {
            saveinfo()
        }
        binding.etend.setOnClickListener {
            showdate(binding.etend,"Select ending Date")
        }
        // Auto-clear errors on input
        listOf(
            binding.tvcompname to binding.etcompname,
            binding.tvdesig to binding.etdesig,
            binding.tvskill to binding.etskill
        ).forEach { (layout, editText) ->
            editText.addTextChangedListener(object : android.text.TextWatcher {
                override fun afterTextChanged(s: android.text.Editable?) { layout.error = null }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
        // Date field clears on pick — handled inside showdate already via setText
        binding.etend.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) { binding.tvend.error = null }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }


    private fun saveinfo(){
        if(uid!=null){
            var inst=binding.etcompname.text.toString()
            var roll=binding.etdesig.text.toString()
            var date=binding.etend.text.toString()
            var skill=binding.etskill.text.toString()
            var desc=binding.etdesc.text.toString()
            binding.tvcompname.error = null
            binding.tvdesig.error = null
            binding.tvend.error = null
            binding.tvskill.error = null

            when {
                inst.isEmpty() -> {
                    binding.tvcompname.error = "Institution name is required"
                    binding.etcompname.requestFocus()
                    return
                }
                inst.length < 3 -> {
                    binding.tvcompname.error = "Enter a valid institution name"
                    binding.etcompname.requestFocus()
                    return
                }
                roll.isEmpty() -> {
                    binding.tvdesig.error = "Certificate number is required"
                    binding.etdesig.requestFocus()
                    return
                }
                date.isEmpty() -> {
                    binding.tvend.error = "Issue date is required"
                    binding.etend.requestFocus()
                    return
                }
                skill.isEmpty() -> {
                    binding.tvskill.error = "Key skill is required"
                    binding.etskill.requestFocus()
                    return
                }
            }
            var details= mapOf(
                "skillname" to skill,
                "certNo" to roll,
                "issuingcompany" to inst,
                "issuedate" to date,
                "description" to desc
            )
            db.collection("Freelancers").document(uid).update("certification",FieldValue.arrayUnion(details)).addOnSuccessListener {
                showsnack("Details Saved")
                findNavController().navigateUp()
            }.addOnFailureListener {
                showsnack("Error Check Your Internet")
            }
        }
    }
    private fun showdate(dateInput: TextInputEditText, title:String){
        val builder= MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setTheme(R.style.Mydate)
        val picker=builder.build()
        // Listen for when the user clicks the "OK" button
        picker.addOnPositiveButtonClickListener { selection ->
            // The 'selection' is a Long representing the date in milliseconds.
            // We need to format it into a human-readable string.

            // Create a date formatter
            val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())

            // Important: The picker returns a date in UTC. We need to tell the formatter
            // to use UTC to avoid the date being off by one day.
            sdf.timeZone = TimeZone.getTimeZone("UTC")

            // Format the date and set it in the EditText
            val selectedDate = sdf.format(Date(selection))

            dateInput.setText(selectedDate)
        }

        // Show the date picker
        // We use childFragmentManager for fragments
        picker.show(childFragmentManager, "DATE_PICKER_TAG")
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment certificateFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            certificateFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
private fun showsnack(message:String){
    Snackbar.make(binding.root,message,Snackbar.LENGTH_SHORT).show()
}
    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}