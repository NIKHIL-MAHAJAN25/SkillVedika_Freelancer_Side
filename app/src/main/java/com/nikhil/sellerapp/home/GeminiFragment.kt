package com.nikhil.sellerapp.home

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.nikhil.sellerapp.GeminiClient.GemResponse
import com.nikhil.sellerapp.GeminiClient.ResumeRequest

import com.nikhil.sellerapp.Utils.snack
import com.nikhil.sellerapp.databinding.FragmentGeminiBinding
import com.nikhil.sellerapp.mailretro.Retromail
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import retrofit2.awaitResponse

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
    private var extracted = ""
    private var isAnalyzing = false

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
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {

            if (isAnalyzing) {

                snack("Analysis in progress. Please wait.")

            } else {

                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()

            }
        }
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
        binding.btnAnalyze.isEnabled = false
        binding.btnUploadPdf.isEnabled = false
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
                    if (fulltext.isBlank()) {
                        binding.progressBar.visibility = View.GONE
                        binding.tvFileName.text = "No readable text found"
                        binding.btnUploadPdf.isEnabled = true
                        binding.btnAnalyze.isEnabled = false
                        snack("This PDF contains no selectable text.")
                        return@withContext
                    }
                    binding.tvFileName.text = "Resume loaded"
                    binding.btnUploadPdf.text = "Change pdf"
                    binding.btnAnalyze.isEnabled = true
                    binding.btnUploadPdf.isEnabled = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main)
                {
                    if (_binding == null) return@withContext
                    binding.progressBar.visibility = View.GONE
                    binding.tvFileName.text = "Error reading PDF"
                    binding.btnUploadPdf.isEnabled = true
                    binding.btnAnalyze.isEnabled = false
                    e.printStackTrace()
                    snack("Failed to read PDF. Is it password protected?")

                }
            }
        }
    }

    private fun performAnalysis(resume: String, job: String) {

        if (isAnalyzing) return

        isAnalyzing = true

        binding.progressBar.visibility = View.VISIBLE
        binding.resultLayout.visibility = View.GONE

        binding.btnAnalyze.isEnabled = false
        binding.btnUploadPdf.isEnabled = false
        binding.etJobDesc.isEnabled = false

        if (isAdded) {
            requireActivity().requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_LOCKED
        }

        lifecycleScope.launch {

            try {

                val response = Retromail.instance
                    .analyzeResume(
                        ResumeRequest(
                            resumeText = resume,
                            jobDesc = job
                        )
                    )
                    .awaitResponse()

                if (_binding == null) return@launch

                if (response.isSuccessful &&
                    response.body()?.success == true
                ) {

                    showResult(response.body()!!.data)

                } else {

                    snack("Analysis failed")

                }

            } catch (e: Exception) {

                e.printStackTrace()
                snack("Network Error")

            } finally {

                isAnalyzing = false

                val binding = _binding
                if (binding != null) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnAnalyze.isEnabled = true
                    binding.btnUploadPdf.isEnabled = true
                    binding.etJobDesc.isEnabled = true
                }

                if (isAdded) {
                    requireActivity().requestedOrientation =
                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }
        }
    }





    private fun showResult(result: GemResponse) {

        if (_binding == null) return

        binding.resultLayout.visibility = View.VISIBLE

        binding.tvScore.text =
            "Match Score: ${result.score}%"

        binding.tvFeedback.text =
            result.summary

        if (result.score >= 75) {

            binding.tvScore.setTextColor(
                android.graphics.Color.parseColor("#4CAF50")
            )

        } else {

            binding.tvScore.setTextColor(
                android.graphics.Color.parseColor("#F44336")
            )

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