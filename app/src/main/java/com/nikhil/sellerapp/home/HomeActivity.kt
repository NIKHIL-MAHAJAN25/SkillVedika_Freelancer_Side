package com.nikhil.sellerapp.home

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView

import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.nikhil.sellerapp.R
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior

import com.nikhil.sellerapp.databinding.ActivityEntercodeBinding
import com.nikhil.sellerapp.databinding.ActivityHomeBinding
import com.nikhil.sellerapp.dataclasses.Freelancer
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeActivity : AppCompatActivity() {

    lateinit var binding: ActivityHomeBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.setPadding(0, statusBars.top, 0, 0)
            insets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.host) as NavHostFragment
        val navController: NavController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.search,
                R.id.message,
                R.id.order -> showBottomNav()
                else -> hideBottomNav()
            }
        }

        checkprof()
    }

    private fun checkprof() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            lifecycleScope.launch {
                try {
                    val docu = db.collection("Freelancers").document(uid).get().await()
                    val ans = docu.getBoolean("profcomp") ?: false
                    if (!ans && binding.bottomNavigation.visibility == View.VISIBLE) {
                        showprofilemark()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun showprofilemark() {
        val prefs = getSharedPreferences("hints", MODE_PRIVATE)
        val shownBefore = prefs.getBoolean("freelancer_full_profile_hint_shown", false)
        if (shownBefore) return
        if (binding.bottomNavigation.visibility != View.VISIBLE) return

        binding.bottomNavigation.post {
            val menu = binding.bottomNavigation.getChildAt(0) as? ViewGroup ?: return@post
            val profile = menu.childCount - 1
            val target = menu.getChildAt(profile) ?: return@post
            TapTargetView.showFor(
                this@HomeActivity,
                TapTarget.forView(
                    target,
                    "Complete your Profile",
                    "Add your skills, experience, level and rate to start getting orders"
                )
                    .outerCircleColor(R.color.black)
                    .outerCircleAlpha(0.85f)
                    .targetCircleColor(android.R.color.white)
                    .titleTextColor(android.R.color.white)
                    .descriptionTextColor(android.R.color.white)
                    .tintTarget(true)
                    .drawShadow(true)
                    .cancelable(false)
                    .id(202),
                object : TapTargetView.Listener() {
                    override fun onTargetClick(view: TapTargetView) {
                        super.onTargetClick(view)
                        binding.bottomNavigation.selectedItemId = R.id.profile
                        prefs.edit().putBoolean("freelancer_full_profile_hint_shown", true).apply()
                    }
                }
            )
        }
    }

    private fun hideBottomNav() {
        binding.bottomNavigation.visibility = View.GONE
        val layoutParams = binding.bottomNavigation.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.behavior = null
    }

    private fun showBottomNav() {
        binding.bottomNavigation.visibility = View.VISIBLE
        val layoutParams = binding.bottomNavigation.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.behavior = HideBottomViewOnScrollBehavior<BottomNavigationView>()
    }
}