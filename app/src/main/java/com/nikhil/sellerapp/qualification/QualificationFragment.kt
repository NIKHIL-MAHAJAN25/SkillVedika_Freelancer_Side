package com.nikhil.sellerapp.qualification

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
import com.nikhil.sellerapp.databinding.FragmentQualificationBinding
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
 * Use the [QualificationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class QualificationFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var _binding:FragmentQualificationBinding?=null
    private val binding get()=_binding!!
    private var auth:FirebaseAuth=FirebaseAuth.getInstance()
    val db= Firebase.firestore
    val uid=auth.currentUser?.uid
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
       _binding= FragmentQualificationBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.checkcurrent.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                binding.etend.setText("")
                binding.etend.isEnabled = false
                binding.tvend.isEnabled = false
            } else {
                binding.etend.isEnabled = true
                binding.tvend.isEnabled = true
            }
        }
        binding.btnSave.setOnClickListener {
            saveinfo()
        }
        binding.etend.setOnClickListener {

            if (!binding.checkcurrent.isChecked) {
                showdate(binding.etend, "Select ending Date")
            }
        }
    }

private fun showdate(dateInput: TextInputEditText,title:String){
    val builder=MaterialDatePicker.Builder.datePicker()
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
private fun saveinfo(){
    val inst=binding.etcollege.text.toString()
    val roll=binding.etenroll.text.toString()
    val degree=binding.etskill.text.toString()
    val agg=binding.etaggregate.text.toString()
    val max=binding.etmax.text.toString()
    val edate = if (binding.checkcurrent.isChecked) {
        "Present"
    } else {
        binding.etend.text.toString()
    }
    binding.tvcollege.error = null
    binding.tvenroll.error = null
    binding.tvskill.error = null
    binding.tvagreggate.error = null
    binding.tvmax.error = null
    binding.tvend.error = null

    when {
        inst.isEmpty() -> {
            binding.tvcollege.error = "Institution name is required"
            binding.etcollege.requestFocus()
            return
        }
        inst.length < 3 -> {
            binding.tvcollege.error = "Enter a valid institution name"
            binding.etcollege.requestFocus()
            return
        }
        roll.isEmpty() -> {
            binding.tvenroll.error = "Enrollment number is required"
            binding.etenroll.requestFocus()
            return
        }
        !binding.checkcurrent.isChecked && edate.isBlank() -> {
            binding.tvend.error = "Select graduation year"
            binding.etend.requestFocus()
            return
        }
        degree.isEmpty() -> {
            binding.tvskill.error = "Degree is required"
            binding.etskill.requestFocus()
            return
        }
        agg.isEmpty() -> {
            binding.tvagreggate.error = "Aggregate is required"
            binding.etaggregate.requestFocus()
            return
        }
        agg.toFloatOrNull() == null -> {
            binding.tvagreggate.error = "Enter a valid number"
            binding.etaggregate.requestFocus()
            return
        }
        max.isEmpty() -> {
            binding.tvmax.error = "Max marks is required"
            binding.etmax.requestFocus()
            return
        }
        max.toFloatOrNull() == null -> {
            binding.tvmax.error = "Enter a valid number"
            binding.etmax.requestFocus()
            return
        }
        agg.toFloat() > max.toFloat() -> {
            binding.tvagreggate.error = "Can't exceed max marks"
            binding.etaggregate.requestFocus()
            return
        }
    }

    val details= mapOf(
        "instName" to inst,
                "rollNo" to roll,
                "endYear" to edate,
                "degree" to degree,
                "aggregate" to agg,
                "max" to max
    )
    if(uid!=null){

        db.collection("Freelancers").document(uid).update("qualification",FieldValue.arrayUnion(details)).addOnSuccessListener {
            showtoast("Details Saved")
            findNavController().navigateUp()
        }.addOnFailureListener {
            showtoast("Error saving details")
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
         * @return A new instance of fragment QualificationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            QualificationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    private fun showtoast(message:String){
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}