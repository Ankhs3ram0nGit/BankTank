package com.example.expensetracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.expensetracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Find NavController from FragmentContainerView
        val navController = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main)
            ?.findNavController()

        if (navController != null) {
            // Hook up the BottomNavigationView
            binding.navView.setupWithNavController(navController)
        }
    }
}
