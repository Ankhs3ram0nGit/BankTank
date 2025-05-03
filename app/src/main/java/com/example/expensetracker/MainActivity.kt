package com.example.expensetracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.expensetracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var databaseHandler: DatabaseHandler
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main)
            ?.findNavController()

        if (navController != null) {
            binding.navView.setupWithNavController(navController)

            binding.navView.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_home -> {
                        navController.popBackStack(R.id.navigation_home, false)
                        navController.navigate(R.id.navigation_home)
                        true
                    }
                    R.id.tanksFragment -> {
                        navController.popBackStack(R.id.tanksFragment, false)
                        navController.navigate(R.id.tanksFragment)
                        true
                    }
                    R.id.navigation_notifications -> {
                        navController.popBackStack(R.id.navigation_notifications, false)
                        navController.navigate(R.id.navigation_notifications)
                        true
                    }
                    R.id.navigation_other -> {
                        navController.popBackStack(R.id.navigation_other, false)
                        navController.navigate(R.id.navigation_other)
                        true
                    }
                    else -> false
                }
            }
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }

        }

        val dbHandler = DatabaseHandler(this)
        dbHandler.ensureSavingsTankExists()
    }
}

