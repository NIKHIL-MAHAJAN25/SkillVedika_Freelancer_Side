package com.nikhil.sellerapp.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.nikhil.sellerapp.MainActivity
import com.nikhil.sellerapp.R
import com.nikhil.sellerapp.comprofile.supabasefile
import com.nikhil.sellerapp.databinding.FragmentProfileBinding
import com.nikhil.sellerapp.dataclasses.Freelancer
import com.nikhil.sellerapp.dataclasses.User
import com.nikhil.sellerapp.profilepage.BasicFragment
import com.nikhil.sellerapp.profilepage.ExperienceFragment
import com.nikhil.sellerapp.profilepage.SkillsFragment
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.UploadStatus
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.uploadAsFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    private var _binding:FragmentProfileBinding?=null
    private var param1: String? = null
    private var param2: String? = null
    private var profileListener:ListenerRegistration?=null // for holding listener
    private var plink=null
    private lateinit var supabaseClient: SupabaseClient

    val auth:FirebaseAuth=FirebaseAuth.getInstance()
    private val uid=auth.currentUser?.uid
    val db=Firebase.firestore
    private val pickImagelauncher= registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    )
    {
            uri ->
        if(uri!=null)
        {
            uploadImageToSupabase(uri)
        }
    }
    private val binding get() = _binding!!//to prevent memory leaks
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        requireActivity().getSharedPreferences("hints", Context.MODE_PRIVATE).edit().clear().apply()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadinfo()
        loadotherinfo()
        supabaseClient = (requireActivity().application as supabasefile).supabaseClient

        binding.logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()

        }
        binding.cameraFab.setOnClickListener {
            pickImagelauncher.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }


        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.prof_to_edit)
        }
        binding.chipskills.setOnCheckedStateChangeListener{group,checkedIds->
            val checkedId=checkedIds.firstOrNull()?:return@setOnCheckedStateChangeListener
            when (checkedId) {
                R.id.basichip -> replaceFragment(BasicFragment())
                R.id.skills -> replaceFragment(SkillsFragment())
                R.id.exp -> replaceFragment(ExperienceFragment())
            }
        }
        if (savedInstanceState == null) {
            binding.basichip.isChecked = true
        }
        showEditProfileHintIfNeeded()
        }
    private fun showEditProfileHintIfNeeded() {
        val prefs = requireActivity().getSharedPreferences("hints", Context.MODE_PRIVATE)
        val shown = prefs.getBoolean("edit_profile_hint_shown", false)
        if (shown) return

        binding.root.postDelayed({
            if (_binding == null) return@postDelayed

            TapTargetView.showFor(
                requireActivity(),
                TapTarget.forBounds(
                    android.graphics.Rect(
                        binding.root.width / 2 - 1,      // center X
                        binding.root.height,              // bottom of screen
                        binding.root.width / 2 + 1,      // center X
                        binding.root.height + 1                            // tiny height = dome emerges from top
                    ),
                    "Complete Your Profile",
                    "Add skills, experience and rate to increase your visibility to clients"
                )
                    .outerCircleColor(R.color.black)
                    .outerCircleAlpha(0.90f)
                    .targetCircleColor(R.color.black)
                    .titleTextColor(android.R.color.white)
                    .descriptionTextColor(android.R.color.white)
                    .drawShadow(true)
                    .cancelable(true)
                    .tintTarget(false)
                    .id(203),
                object : TapTargetView.Listener() {
                    override fun onTargetClick(view: TapTargetView) {
                        super.onTargetClick(view)
                        prefs.edit()
                            .putBoolean("edit_profile_hint_shown", true)
                            .apply()
                    }
                    override fun onOuterCircleClick(view: TapTargetView) {
                        super.onOuterCircleClick(view)
                        prefs.edit()
                            .putBoolean("edit_profile_hint_shown", true)
                            .apply()
                        view.dismiss(false)
                    }
                }
            )
        }, 500L)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    private fun uploadImageToSupabase(uri: Uri) {

        val byteArray = uriToByteArray(requireContext(), uri)

        if (byteArray.size > 5 * 1024 * 1024) {

            Toast.makeText(
                requireContext(),
                "Image must be under 5 MB",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        binding.cameraFab.isEnabled = false
        val fileName = "uploads/${System.currentTimeMillis()}.jpg"

        val bucket = supabaseClient.storage.from("sample") // Choose your bucket name

        // Use lifecycleScope for safe coroutine usage
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Upload image and handle the response
                bucket.uploadAsFlow(fileName, byteArray).collect { status ->
                    withContext(Dispatchers.Main) {
                        when (status) {
                            is UploadStatus.Progress -> {
//                                val progress = (status.totalBytesSent.toFloat() / status.contentLength * 100)
                                Log.d("Upload", "Progress%")
                            }

                            is UploadStatus.Success -> {

                                binding.cameraFab.isEnabled = true

                                Log.d("Upload ", "Upload Success")

                                handleUploadSuccess(bucket, fileName)
                            }

                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.cameraFab.isEnabled = true

                    Log.e("Upload", "Error uploading image: ${e.message}")

                }
            }
        }
    }
    private fun uriToByteArray(
        context: Context,
        uri: Uri
    ): ByteArray {

        return context.contentResolver
            .openInputStream(uri)
            ?.use {
                it.readBytes()
            }
            ?: throw Exception("Unable to read image")
    }
    private fun handleUploadSuccess(bucket: Any, fileName: String) {
        try {
            val imageUrl = supabaseClient.storage.from("sample").publicUrl(fileName)
            Log.d("ProfileFragment", "Generated public URL: $imageUrl")

            val currentUser = auth.currentUser
            if (currentUser != null) {
                Log.d("ProfileFragment", "Updating Firestore with new image URL")
                db.collection("Users")
                    .document(currentUser.uid)
                    .update("profilePictureUrl",imageUrl)
                    .addOnSuccessListener {
                        Log.d("ProfileFragment", "Firestore update successful")
                        Toast.makeText(requireContext(), "Profile image updated!", Toast.LENGTH_SHORT).show()
                        Glide.with(this)
                            .load(imageUrl)
                            .error(R.drawable.ic_launcher_background)
                            .into(binding.profileImage)
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirestoreUpdate", "Failed to update profile image URL: ${e.message}", e)
                        Toast.makeText(requireContext(), "Failed to update profile image: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Log.e("ProfileFragment", "Current user is null")
                Toast.makeText(requireContext(), "User authentication error", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error in handleUploadSuccess: ${e.message}", e)
            Toast.makeText(requireContext(), "Error processing upload success: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun loadotherinfo() {
        if (uid != null) {
            db.collection("Freelancers").document(uid)
                .addSnapshotListener { snapshot, error ->
                    //Now what happens is we are getting npe exception ie firestore add on listener run asyncronoulsy and giving result even when fragment is destroyed so we make a local
                    //copy of binding
                    val b = _binding ?: return@addSnapshotListener
                    if (error != null) {
                        // Handle error, maybe log it
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val user = snapshot.toObject<Freelancer>()
                        b.tvtitle.setText(user?.primaryskill)
                        val rate = user?.projectRate ?: 0.0
                        b.tvrate.text = "₹$rate/hour"
                    }
                }
        }
    }
    private fun loadinfo(){
        if(uid!=null){
            db.collection("Users").document(uid)
                .addSnapshotListener { snapshot,error->
                    //Now what happens is we are getting npe exception ie firestore add on listener run asyncronoulsy and giving result even when fragment is destroyed so we make a local
                    //copy of binding
                    val b=_binding?:return@addSnapshotListener
                    if (error != null) {
                        // Handle error, maybe log it
                        return@addSnapshotListener
                    }
                    if(snapshot != null && snapshot.exists()){
                      val user=snapshot.toObject<User>()
                        b.tvname.text=user?.fullName
                        Glide.with(this@ProfileFragment)
                            .load(user?.profilePictureUrl)
                            .error(R.drawable.ic_launcher_background)
                            .into(binding.profileImage)



                    }
                }
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        // 1. Get the specialized manager for nested fragments.
        val fragmentManager = childFragmentManager
        // 2. Start a transaction.
        val transaction = fragmentManager.beginTransaction()
        // 3. Replace the content of the container with the new fragment.
        transaction.replace(R.id.framelayout, fragment)
        // 4. Commit the transaction to make it happen.
        transaction.commit()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}