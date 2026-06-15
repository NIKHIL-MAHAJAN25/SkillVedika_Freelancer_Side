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
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var param1: String? = null
    private var param2: String? = null
    private var profileListener: ListenerRegistration? = null
    private lateinit var supabaseClient: SupabaseClient

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val uid = auth.currentUser?.uid
    val db = Firebase.firestore

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) uploadImageToSupabase(uri)
    }

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
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        supabaseClient = (requireActivity().application as supabasefile).supabaseClient

        loadinfo()
        loadotherinfo()

        binding.btnSettings.setOnClickListener {
            showSettingsDialog()
        }

        binding.cameraFab.setOnClickListener {
            pickImageLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.prof_to_edit)
        }

        binding.chipskills.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
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

    // ─── Settings ────────────────────────────────────────────────────────────

    private fun showSettingsDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_settings, null)

        val dialog = android.app.Dialog(requireContext()).apply {
            setContentView(dialogView)
            window?.apply {
                setBackgroundDrawable(
                    android.graphics.drawable.GradientDrawable().apply {
                        setColor(android.graphics.Color.WHITE)
                        cornerRadius = 32f * resources.displayMetrics.density
                    }
                )
                setLayout(
                    (resources.displayMetrics.widthPixels * 0.88).toInt(),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }

        dialogView.findViewById<android.widget.LinearLayout>(R.id.rowLogout).setOnClickListener {
            dialog.dismiss()
            logout()
        }

        dialogView.findViewById<android.widget.LinearLayout>(R.id.rowDeleteAccount).setOnClickListener {
            dialog.dismiss()
            showDeleteConfirmationDialog()
        }

        dialog.show()
    }

    private fun logout() {
        auth.signOut()
        navigateToMain()
    }

    private fun showDeleteConfirmationDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_delete_confirm, null)

        val dialog = android.app.Dialog(requireContext()).apply {
            setContentView(dialogView)
            window?.apply {
                setBackgroundDrawable(
                    android.graphics.drawable.GradientDrawable().apply {
                        setColor(android.graphics.Color.WHITE)
                        cornerRadius = 32f * resources.displayMetrics.density
                    }
                )
                setLayout(
                    (resources.displayMetrics.widthPixels * 0.88).toInt(),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }

        val etPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPasswordinside)

        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancel)
            .setOnClickListener { dialog.dismiss() }

        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDeleteConfirminside)
            .setOnClickListener {
                val password = etPassword.text.toString().trim()
                if (password.isEmpty()) {
                    Toast.makeText(requireContext(), "Enter your password", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                dialog.dismiss()
                deleteAccountCascade(password)
            }

        dialog.show()
    }

    private fun deleteAccountCascade(password: String) {
        val currentUid = uid ?: run {
            Toast.makeText(requireContext(), "Not signed in", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = auth.currentUser ?: run {
            Toast.makeText(requireContext(), "Not signed in", Toast.LENGTH_SHORT).show()
            return
        }

        val email = currentUser.email ?: run {
            Toast.makeText(requireContext(), "Email not found", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 0. Re-authenticate first
                val credential = com.google.firebase.auth.EmailAuthProvider
                    .getCredential(email, password)
                currentUser.reauthenticate(credential).await()

                // 1. Delete Projects where freeuid == uid (freelancer's projects)
                val projects = db.collection("Projects")
                    .whereEqualTo("freeuid", currentUid)
                    .get()
                    .await()

                projects.documents.chunked(500).forEach { chunk ->
                    val batch = db.batch()
                    chunk.forEach { batch.delete(it.reference) }
                    batch.commit().await()
                }

                // 2. Delete Chats where uid is a participant
                val chats = db.collection("Chats")
                    .whereArrayContains("participants", currentUid)
                    .get()
                    .await()

                chats.documents.chunked(500).forEach { chunk ->
                    val batch = db.batch()
                    chunk.forEach { batch.delete(it.reference) }
                    batch.commit().await()
                }

                // 3. Delete Freelancer document
                db.collection("Freelancers").document(currentUid).delete().await()

                // 4. Grab profile picture URL before deleting Users doc
                val userSnapshot = db.collection("Users").document(currentUid).get().await()
                val profilePicUrl = userSnapshot.getString("profilePictureUrl")

                // 5. Delete Users document
                db.collection("Users").document(currentUid).delete().await()

                // 6. Delete Supabase profile image if present
                profilePicUrl?.let { url ->
                    try {
                        val filePath = url.substringAfter("/object/public/sample/")
                        if (filePath.isNotEmpty()) {
                            supabaseClient.storage.from("sample").delete(listOf(filePath))
                        }
                    } catch (e: Exception) {
                        Log.e("DeleteAccount", "Supabase delete failed (non-fatal): ${e.message}")
                    }
                }

                // 7. Delete Firebase Auth user
                currentUser.delete().await()

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }

            } catch (e: com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Wrong password. Account not deleted.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("DeleteAccount", "Cascade delete failed: ${e.message}")
                    Toast.makeText(requireContext(), "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    // ─── Hint ─────────────────────────────────────────────────────────────────

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
                        binding.root.width / 2 - 1,
                        binding.root.height,
                        binding.root.width / 2 + 1,
                        binding.root.height + 1
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
                        prefs.edit().putBoolean("edit_profile_hint_shown", true).apply()
                    }
                    override fun onOuterCircleClick(view: TapTargetView) {
                        super.onOuterCircleClick(view)
                        prefs.edit().putBoolean("edit_profile_hint_shown", true).apply()
                        view.dismiss(false)
                    }
                }
            )
        }, 500L)
    }

    // ─── Data Loading ─────────────────────────────────────────────────────────

    private fun loadotherinfo() {
        if (uid == null) return
        db.collection("Freelancers").document(uid)
            .addSnapshotListener { snapshot, error ->
                val b = _binding ?: return@addSnapshotListener
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val user = snapshot.toObject<Freelancer>()
                b.tvtitle.setText(user?.primaryskill)
                val rate = user?.projectRate ?: 0.0
                b.tvrate.text = "₹$rate/hour"
            }
    }

    private fun loadinfo() {
        if (uid == null) return
        db.collection("Users").document(uid)
            .addSnapshotListener { snapshot, error ->
                val b = _binding ?: return@addSnapshotListener
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val user = snapshot.toObject<User>()
                b.tvname.text = user?.fullName
                Glide.with(this@ProfileFragment)
                    .load(user?.profilePictureUrl)
                    .error(R.drawable.ic_launcher_background)
                    .into(b.profileImage)
            }
    }

    // ─── Image Upload ─────────────────────────────────────────────────────────

    private fun uploadImageToSupabase(uri: Uri) {
        val byteArray = uriToByteArray(requireContext(), uri)

        if (byteArray.size > 5 * 1024 * 1024) {
            Toast.makeText(requireContext(), "Image must be under 5 MB", Toast.LENGTH_SHORT).show()
            return
        }

        binding.cameraFab.isEnabled = false
        val fileName = "uploads/${System.currentTimeMillis()}.jpg"
        val bucket = supabaseClient.storage.from("sample")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                bucket.uploadAsFlow(fileName, byteArray).collect { status ->
                    withContext(Dispatchers.Main) {
                        when (status) {
                            is UploadStatus.Progress -> Log.d("Upload", "In progress...")
                            is UploadStatus.Success -> {
                                binding.cameraFab.isEnabled = true
                                handleUploadSuccess(fileName)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.cameraFab.isEnabled = true
                    Log.e("Upload", "Error: ${e.message}")
                }
            }
        }
    }

    private fun handleUploadSuccess(fileName: String) {
        try {
            val imageUrl = supabaseClient.storage.from("sample").publicUrl(fileName)
            val currentUser = auth.currentUser ?: run {
                Toast.makeText(requireContext(), "User authentication error", Toast.LENGTH_SHORT).show()
                return
            }
            db.collection("Users").document(currentUser.uid)
                .update("profilePictureUrl", imageUrl)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Profile image updated!", Toast.LENGTH_SHORT).show()
                    Glide.with(this)
                        .load(imageUrl)
                        .error(R.drawable.ic_launcher_background)
                        .into(binding.profileImage)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to update image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Log.e("ProfileFragment", "handleUploadSuccess error: ${e.message}")
        }
    }

    private fun uriToByteArray(context: Context, uri: Uri): ByteArray {
        return context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw Exception("Unable to read image")
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.framelayout, fragment)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}