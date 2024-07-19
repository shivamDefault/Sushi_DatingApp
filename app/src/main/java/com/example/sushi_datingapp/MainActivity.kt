package com.example.sushi_datingapp

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import com.example.sushi_datingapp.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.deepGreen)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the ActionBar (Toolbar)
        setSupportActionBar(binding.toolbar)

        // Drawer setup
        actionBarDrawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            R.string.open,
            R.string.close
        )
        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Show the hamburger menu icon

        // Navigation controller setup
        navController = findNavController(R.id.nav_host_fragment)

        //  Correctly build AppBarConfiguration for your navigation graph
        appBarConfiguration = AppBarConfiguration.Builder(
            R.id.datingFragment,
            R.id.chatFragment,
            R.id.profileFragment
        )
            .setOpenableLayout(binding.drawerLayout)
            .build()

        // Link BottomNavigationView and NavigationView to the navigation controller
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController)
        NavigationUI.setupWithNavController(binding.navigationView, navController)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        // Handle NavigationView item clicks (optional - you can remove this if you're using NavigationUI)
        binding.navigationView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.rate -> {
                Toast.makeText(this, "Rate Us", Toast.LENGTH_SHORT).show()
            }

            R.id.termsCondition -> {
                Toast.makeText(this, "Terms and Conditions", Toast.LENGTH_SHORT).show()
            }

            R.id.privacy -> {
                Toast.makeText(this, "Privacy Policies", Toast.LENGTH_SHORT).show()
            }

            R.id.developers -> {
                Toast.makeText(this, "Developers", Toast.LENGTH_SHORT).show()
            }

            R.id.favourite -> {
                Toast.makeText(this, "Favourite", Toast.LENGTH_SHORT).show()
            }

            R.id.share -> {
                Toast.makeText(this, "Share App", Toast.LENGTH_SHORT).show()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}