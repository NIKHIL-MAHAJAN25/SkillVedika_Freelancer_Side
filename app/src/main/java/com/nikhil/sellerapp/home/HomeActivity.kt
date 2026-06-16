package com.nikhil.sellerapp.home

import android.content.SharedPreferences
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nikhil.sellerapp.R
import com.nikhil.sellerapp.databinding.ActivityHomeBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class HomeActivity : AppCompatActivity() {

    lateinit var binding: ActivityHomeBinding

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

        val prefs = getSharedPreferences("hints", MODE_PRIVATE)
        binding.root.post {
            lifecycleScope.launch {
                delay(3000)
                showHintsInSequence(prefs)
            }
        }
    }

    private fun showHintsInSequence(prefs: SharedPreferences) {
        binding.bottomNavigation.post {
            val menu = binding.bottomNavigation.getChildAt(0) as? ViewGroup ?: return@post
            val aiShown = prefs.getBoolean("ai_hint_shown", false)
            val profileShown = prefs.getBoolean("freelancer_full_profile_hint_shown", false)
            val ordershown = prefs.getBoolean("order_hint_shows",false)
            val chatshown = prefs.getBoolean("chat_hint_shows",false)
            val homeshown= prefs.getBoolean("home_hint_shows",false)




            when {
                !homeshown -> showHomeHint(prefs, menu)
                !chatshown -> showChatHint(prefs, menu)
                !ordershown -> showOrderHint(prefs, menu)
                !aiShown -> showAiHint(prefs, menu)
                !profileShown -> showProfileHint(prefs, menu)
            }
        }
    }
    private fun showHomeHint(prefs: SharedPreferences, menu: ViewGroup) {
        val aiTarget = menu.getChildAt(0) ?: return  // index 3 = AI/Gemini

        TapTargetView.showFor(
            this@HomeActivity,
            TapTarget.forView(
                aiTarget,
                "Start Exploring",
                "Find Projects and discover opportunities"
            )
                .outerCircleColor(R.color.black)
                .outerCircleAlpha(0.85f)
                .targetCircleColor(android.R.color.white)
                .titleTextColor(android.R.color.white)
                .descriptionTextColor(android.R.color.white)
                .tintTarget(true)
                .drawShadow(true)
                .cancelable(false)
                .id(201),
            object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView) {
                    super.onTargetClick(view)
                    prefs.edit().putBoolean("home_hint_shows", true).apply()
                    // Show profile hint right after AI hint is dismissed
                    showChatHint(prefs,menu)
                }
            }
        )
    }
    private fun showChatHint(prefs: SharedPreferences, menu: ViewGroup) {
        val aiTarget = menu.getChildAt(1) ?: return  // index 3 = AI/Gemini

        TapTargetView.showFor(
            this@HomeActivity,
            TapTarget.forView(
                aiTarget,
                "Connect with People",
                "All your conversations appear here. Reply quickly, discuss project details, and build trust with clients."
            )
                .outerCircleColor(R.color.black)
                .outerCircleAlpha(0.85f)
                .targetCircleColor(android.R.color.white)
                .titleTextColor(android.R.color.white)
                .descriptionTextColor(android.R.color.white)
                .tintTarget(true)
                .drawShadow(true)
                .cancelable(false)
                .id(201),
            object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView) {
                    super.onTargetClick(view)
                    prefs.edit().putBoolean("chat_hint_shows", true).apply()
                    // Show profile hint right after AI hint is dismissed
                    showOrderHint(prefs, menu)
                }
            }
        )
    }
    private fun showOrderHint(prefs: SharedPreferences, menu: ViewGroup) {
        val aiTarget = menu.getChildAt(2) ?: return  // index 3 = AI/Gemini

        TapTargetView.showFor(
            this@HomeActivity,
            TapTarget.forView(
                aiTarget,
                "Manage Your Projects",
                "Track all your projects here. Use the tabs to view Open, In Progress, Completed, and Cancelled work."
            )
                .outerCircleColor(R.color.black)
                .outerCircleAlpha(0.85f)
                .targetCircleColor(android.R.color.white)
                .titleTextColor(android.R.color.white)
                .descriptionTextColor(android.R.color.white)
                .tintTarget(true)
                .drawShadow(true)
                .cancelable(false)
                .id(201),
            object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView) {
                    super.onTargetClick(view)
                    prefs.edit().putBoolean("order_hint_shows", true).apply()
                    // Show profile hint right after AI hint is dismissed
                    showAiHint(prefs, menu)
                }
            }
        )
    }

    private fun showAiHint(prefs: SharedPreferences, menu: ViewGroup) {
        val aiTarget = menu.getChildAt(3) ?: return  // index 3 = AI/Gemini

        TapTargetView.showFor(
            this@HomeActivity,
            TapTarget.forView(
                aiTarget,
                "Check Job Compatibility",
                "Paste any job description and AI will match it against your profile instantly"
            )
                .outerCircleColor(R.color.black)
                .outerCircleAlpha(0.85f)
                .targetCircleColor(android.R.color.white)
                .titleTextColor(android.R.color.white)
                .descriptionTextColor(android.R.color.white)
                .tintTarget(true)
                .drawShadow(true)
                .cancelable(false)
                .id(201),
            object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView) {
                    super.onTargetClick(view)
                    prefs.edit().putBoolean("ai_hint_shown", true).apply()
                    // Show profile hint right after AI hint is dismissed
                    val profileShown = prefs.getBoolean("freelancer_full_profile_hint_shown", false)
                    if (!profileShown) {
                        showProfileHint(prefs, menu)
                    }
                }
            }
        )
    }

    private fun showProfileHint(prefs: SharedPreferences, menu: ViewGroup) {
        val profileTarget = menu.getChildAt(4) ?: return  // index 4 = Profile

        TapTargetView.showFor(
            this@HomeActivity,
            TapTarget.forView(
                profileTarget,
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
                    prefs.edit()
                        .putBoolean("freelancer_full_profile_hint_shown", true)
                        .apply()
                }
            }
        )
    }

    private fun hideBottomNav() {
        binding.bottomNavigation.visibility = android.view.View.GONE
        val layoutParams = binding.bottomNavigation.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.behavior = null
    }

    private fun showBottomNav() {
        binding.bottomNavigation.visibility = android.view.View.VISIBLE
        val layoutParams = binding.bottomNavigation.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.behavior = HideBottomViewOnScrollBehavior<BottomNavigationView>()
    }
}