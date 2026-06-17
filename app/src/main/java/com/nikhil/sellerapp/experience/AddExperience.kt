package com.nikhil.sellerapp.experience

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.nikhil.sellerapp.BuildConfig
import com.nikhil.sellerapp.R
import com.nikhil.sellerapp.brandfetch.BrandResponse
import com.nikhil.sellerapp.brandfetch.Brandname
import com.nikhil.sellerapp.brandfetch.RetroBrand
import com.nikhil.sellerapp.databinding.FragmentAddExperienceBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
 * Use the [AddExperience.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddExperience : Fragment() {
    private var _binding:FragmentAddExperienceBinding?=null
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var arradapter:ArrayAdapter<Brandname>
    private val binding get()=_binding!!
    private var companylogo: String? = null
    private var auth:FirebaseAuth=FirebaseAuth.getInstance()
    val db=Firebase.firestore
    val uid=auth.currentUser?.uid


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
        _binding=FragmentAddExperienceBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupauto()

        binding.imgbt.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.etstart.setOnClickListener {
            showdate(binding.etstart,"Select Joining Date")
        }
        binding.etend.setOnClickListener {
            showdate(binding.etend,"Select Ending Date")
        }
        binding.btnSave.setOnClickListener {
            savedata()
        }
        binding.checkcurrent.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.etend.isEnabled = false
                binding.etend.text = null
                binding.tvend.error = null  // clear error when toggled
            } else {
                binding.etend.isEnabled = true
            }
        }

        // Auto-clear errors on input
        listOf(
            binding.tvcompname to binding.etcompname,
            binding.tvdesig to binding.etdesig,
            binding.tvdesc2 to binding.etdesc
        ).forEach { (layout, editText) ->
            editText.addTextChangedListener(object : android.text.TextWatcher {
                override fun afterTextChanged(s: android.text.Editable?) { layout.error = null }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
        binding.etstart.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) { binding.tvstart.error = null }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        binding.etend.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) { binding.tvend.error = null }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

    }




    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddExperience.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddExperience().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    private fun setupauto(){
        arradapter = object : ArrayAdapter<Brandname>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line
        ) {
            override fun getFilter(): Filter {
                // This is our "do-nothing" filter. It tells the AutoCompleteTextView
                // not to perform any filtering on its own.
                return object : Filter() {
                    override fun performFiltering(constraint: CharSequence?): FilterResults {
                        return FilterResults()
                    }
                    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {}
                }
            }
        }
        binding.etcompname.setAdapter(arradapter)
        binding.etcompname.threshold=2
        binding.etcompname.addTextChangedListener(object:TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.length >= 2) {
                    // SIMPLIFIED: We call the API directly every time the text changes.
                    searchBrands(query)
                }

            }
        })
        binding.etcompname.setOnItemClickListener { parent, view, position, id ->
            // SIMPLIFIED: We get the full BrandSearchResult object directly from the adapter.
            val selectedBrand = arradapter.getItem(position)

            selectedBrand?.domain?.let { domain ->
                fetchlogo(domain)
            }
        }

    }
    private fun searchBrands(query:String){
        val apiKey = "Bearer ${BuildConfig.BRANDFETCH_API_KEY}"
        RetroBrand.instance.getname(query,apiKey).enqueue(object : Callback<List<Brandname>> {
            override fun onResponse(
                call: Call<List<Brandname>>,
                response: Response<List<Brandname>>
            ) {
                if (_binding == null) return
                if (response.isSuccessful) {
                    val brandResults = response.body().orEmpty()
                    arradapter.clear()
                    arradapter.addAll(brandResults)
                    arradapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<List<Brandname>>, t: Throwable) {
                Log.e("Addexp","Brand search failed")
            }

        })

    }
    private fun fetchlogo(domain:String){
        val apiKey="Bearer ${BuildConfig.BRANDFETCH_API_KEY}"
        RetroBrand.instance.getbrand(domain,apiKey).enqueue(object : Callback<BrandResponse>{
            override fun onResponse(call: Call<BrandResponse>, response: Response<BrandResponse>) {
                if (_binding == null || !isAdded) return
                if(response.isSuccessful){
                    val url=response.body()?.logos?.firstOrNull()?.formats?.firstOrNull()?.src
                    companylogo=url
                    if(!url.isNullOrBlank()){
                        Glide.with(requireContext())
                            .load(url)
                            .placeholder(R.drawable.outline_image_24)
                            .error(R.drawable.baseline_error_24)
                            .into(binding.compimage)

                    }
                    else {

                        showtoast("Could not find a logo for this company.")
                    }

                }
            }

            override fun onFailure(call: Call<BrandResponse>, t: Throwable) {
                showtoast("Request failed: ${t.localizedMessage}")
            }
        })

    }
    private fun showdate(dateInput:TextInputEditText,title:String){
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
    private fun savedata(){
        val cname=binding.etcompname.text.toString()
        val desig=binding.etdesig.text.toString()
        val desc=binding.etdesc.text.toString()
        val sdate=binding.etstart.text.toString()
        val isCurrentlyWorking = binding.checkcurrent.isChecked
        val logosave=companylogo
        val edate = binding.etend.text.toString().trim()
        // Clear previous errors
        binding.tvcompname.error = null
        binding.tvdesig.error = null
        binding.tvstart.error = null
        binding.tvend.error = null
        binding.tvdesc2.error = null

        when {
            cname.isEmpty() -> {
                binding.tvcompname.error = "Company name is required"
                binding.etcompname.requestFocus()
                return
            }
            desig.isEmpty() -> {
                binding.tvdesig.error = "Title/designation is required"
                binding.etdesig.requestFocus()
                return
            }
            sdate.isEmpty() -> {
                binding.tvstart.error = "Joining date is required"
                binding.etstart.requestFocus()
                return
            }
            !isCurrentlyWorking && edate.isEmpty() -> {
                binding.tvend.error = "Select an end date or mark as currently working"
                binding.etend.requestFocus()
                return
            }
            desc.isEmpty() -> {
                binding.tvdesc2.error = "Please describe your role"
                binding.etdesc.requestFocus()
                return
            }
        }
        val finalEndDate = if (isCurrentlyWorking) "Present" else edate

        val details= mapOf(
            "companyname" to cname,
            "designation" to desig,
            "description" to desc,
            "startDate" to sdate,
            "endDate" to finalEndDate,
            "cologo" to logosave
        )
        if (uid != null) {
            db.collection("Freelancers").document(uid).update("experience",FieldValue.arrayUnion(details)).addOnSuccessListener {
                if (_binding == null) return@addOnSuccessListener
                showtoast("Details saved")
                findNavController().navigateUp()
            }.addOnFailureListener {
                if (_binding == null) return@addOnFailureListener
                showtoast("Error saving details")
            }
        }

    }

    private fun showtoast(message:String){
        val b = _binding ?: return
        Snackbar.make(b.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
