package com.proptit.todohive.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.proptit.todohive.R
import com.proptit.todohive.databinding.ActivityHomeBinding
import com.proptit.todohive.ui.home.task.add.AddTaskSheet

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initBinding()
        setupNavigation()
        setupFabAdd()
        setUpWindowInsets()
    }

    private fun initBinding() {
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupNavigation() {
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController
        binding.bottomNavigationView.setupWithNavController(navController)

        if (binding.bottomNavigationView.menu.size() >= 3) {
            binding.bottomNavigationView.menu.getItem(2).isEnabled = false
        }
    }

    private fun setupFabAdd() {
        binding.fabAdd.setOnClickListener {
            AddTaskSheet().show(supportFragmentManager, "add_task_sheet")
        }
    }

    private fun setUpWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(sys.left, sys.top, sys.right, -16)
            insets
        }
    }
}