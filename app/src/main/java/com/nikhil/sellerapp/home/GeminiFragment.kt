package com.nikhil.sellerapp.home

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.nikhil.sellerapp.R
import com.nikhil.sellerapp.Utils.GeminiClient
import com.nikhil.sellerapp.Utils.snack
import com.nikhil.sellerapp.databinding.FragmentGeminiBinding
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GeminiFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GeminiFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var _binding: FragmentGeminiBinding? = null
    private val binding get() = _binding!!
    private var extracted: String = ""
    private val pdflauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()

    ) { uri: Uri? ->
        if (uri != null) {
            extractext(uri)
        } else {
            snack("No file selected")
        }
    }
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
        // Inflate the layout for this fragment
        _binding = FragmentGeminiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnUploadPdf.setOnClickListener {
            pdflauncher.launch(arrayOf("application/pdf"))
        }
        binding.btnAnalyze.setOnClickListener {
            val jobdesc = binding.etJobDesc.text.toString()
            if (extracted.isBlank()) {
                snack("Please upload a pdf")
                return@setOnClickListener
            }
            if (jobdesc.isBlank()) {
                snack("Please paste a job description")
                return@setOnClickListener
            }
            performAnalysis(extracted, jobdesc)
        }
    }

    private fun extractext(uri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvFileName.text = "Reading Pdf"
        binding.tvFileName.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO)
        {
            try {
                val inputstream =
                    requireContext().contentResolver.openInputStream(uri) //opening a file stream
                val document = PDDocument.load(inputstream) //loading
                val stripper = PDFTextStripper()
                val fulltext = stripper.getText(document)
                document.close()
                inputstream?.close()
                withContext(Dispatchers.Main)
                {
                    if (_binding == null) return@withContext
                    binding.progressBar.visibility = View.GONE
                    extracted = fulltext
                    binding.tvFileName.text = "Resume loaded"
                    binding.btnUploadPdf.text = "Change pdf"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main)
                {
                    if (_binding == null) return@withContext
                    binding.progressBar.visibility = View.GONE
                    binding.tvFileName.text = "Error reading PDF"
                    e.printStackTrace()
                    snack("Failed to read PDF. Is it password protected?")

                }
            }
        }
    }

    private fun performAnalysis(resume: String, job: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.resultLayout.visibility = View.GONE // Hide old results
        binding.btnAnalyze.isEnabled = false // Prevent double clicks

        lifecycleScope.launch {
            // Call our Object (Step 1 code)
            val jsonResponse = GeminiClient.analyzeresume(resume, job)
            if (_binding == null) return@launch

            binding.progressBar.visibility = View.GONE
            binding.btnAnalyze.isEnabled = true

            if (jsonResponse != null) {
                parseAndShowResult(jsonResponse)
            } else {
                snack("AI Analysis Failed. Check Internet.")
            }
        }
    }
    private fun parseAndShowResult(rawJson: String) {
        if (_binding == null) return
        try {
            // A. Clean the string (Gemini sometimes adds ```json markers)
            val cleanJson = rawJson.replace("```json", "")
                .replace("```", "")
                .trim()

            // B. Parse into JSON Object
            val obj = JSONObject(cleanJson)

            // C. Extract Data
            val score = obj.optInt("score", 0)
            val feedback = obj.optString("summary", "No feedback provided.")

            // D. Update UI
            binding.resultLayout.visibility = View.VISIBLE
            binding.tvScore.text = "Match Score: $score%"
            binding.tvFeedback.text = feedback

            // E. Color Coding
            if (score > 75) {
                binding.tvScore.setTextColor(android.graphics.Color.parseColor("#4CAF50")) // Green
            } else {
                binding.tvScore.setTextColor(android.graphics.Color.parseColor("#F44336")) // Red
            }

        } catch (e: Exception) {
            e.printStackTrace()
            snack("AI format error. Try again.")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }





    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GeminiFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GeminiFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}