package com.proptit.todohive.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.proptit.todohive.R
import com.proptit.todohive.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        keepSplashFor(splash, 500L)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpWindowInsets()

        lifecycleScope.launch {
            val didAuto = tryAutoLogin()
            if (!didAuto) {
                setupStartGraph()
            }
        }
    }

    private fun keepSplashFor(splash: SplashScreen, millis: Long) {
        var keep = true
        splash.setKeepOnScreenCondition { keep }
        lifecycleScope.launch {
            delay(millis)
            keep = false
        }
    }

    private fun setUpWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupStartGraph() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val inflater = navController.navInflater

        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val hasSeenOnboarding = prefs.getBoolean("onboarding_done", false)

        val startGraphRes = if (!hasSeenOnboarding) {
            R.navigation.nav_onboarding
        } else {
            R.navigation.nav_auth
        }

        navController.graph = inflater.inflate(startGraphRes)
    }

    private suspend fun tryAutoLogin(): Boolean {
        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val remember = prefs.getBoolean("remember_me", false)
        if (!remember) return false

        val username = prefs.getString("remember_username", null) ?: return false
        val savedHash = prefs.getString("remember_hash", null) ?: return false

        val dao = com.proptit.todohive.data.local.AppDatabase.get(this).userDao()
        val user = with(kotlinx.coroutines.Dispatchers.IO) {
            kotlinx.coroutines.withContext(this) { dao.findByUsername(username) }
        } ?: return false

        return if (user.password_hash == savedHash) {
            startActivity(android.content.Intent(this, com.proptit.todohive.ui.HomeActivity::class.java))
            finish()
            true
        } else {
            prefs.edit()
                .remove("remember_me")
                .remove("remember_user_id")
                .remove("remember_username")
                .remove("remember_hash")
                .apply()
            false
        }
    }
}