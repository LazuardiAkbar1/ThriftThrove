package com.dicoding.capstonui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dicoding.capstonui.view.AddFragment
import com.dicoding.capstonui.view.CartFragment
import com.dicoding.capstonui.view.ExploreFragment
import com.dicoding.capstonui.view.UserFragment
import com.dicoding.capstonui.view.RecommendationFragment

import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavMenu: BottomNavigationView

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavMenu = findViewById(R.id.bottomNavMenu)



        // Set initial fragment
        loadFragment(ExploreFragment())

        bottomNavMenu.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.exploreFragment -> {
                    loadFragment(ExploreFragment())
                    true
                }
                R.id.addFragment -> {
                    loadFragment(AddFragment())
                    true
                }
                R.id.cartFragment -> {
                    loadFragment(CartFragment())
                    true
                }
                R.id.recommenderFragment -> {
                    loadFragment(RecommendationFragment())
                    true
                }
                R.id.userFragment -> {
                    loadFragment(UserFragment())
                    true
                }
                else -> false
            }
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Handle back press navigation
        when (val currentFragment = supportFragmentManager.findFragmentById(R.id.nav_fragment)) {
            is AddFragment -> {
                if (!currentFragment.findNavController().navigateUp()) {
                    super.onBackPressed()
                }
            }
            else -> super.onBackPressed()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_fragment, fragment)
            .commit()
    }
}
