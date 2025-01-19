package com.example.testwhatsapp.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.testwhatsapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        val window = window
        window.statusBarColor = resources.getColor(R.color.statusColor)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.chatListFragment || destination.id == R.id.loginFragment) {
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            } else {
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            }
            supportActionBar?.title = getString(R.string.app_name)
            invalidateOptionsMenu()
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            R.id.action_delete_account -> {
                deleteAccount()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteAccount() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "No user is currently logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val databaseReference = FirebaseDatabase.getInstance().getReference("users")
        databaseReference.child(userId).removeValue().addOnCompleteListener { removeTask ->
            if (removeTask.isSuccessful) {
                currentUser.delete().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "User deleted successfully.", Toast.LENGTH_SHORT).show()
                        FirebaseAuth.getInstance().signOut()
                        with(sharedPref.edit()) {
                            remove("rememberMe")
                            apply()
                        }
                        navController.navigate(R.id.action_chatListFragment_to_loginFragment)
                    } else {
                        Log.e("DeleteAccount", "Error deleting user: ${task.exception?.message}")
                        Toast.makeText(this, "Error deleting user. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.e("DeleteAccount", "Error removing user data: ${removeTask.exception?.message}")
                Toast.makeText(this, "Error removing user data. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        with(sharedPref.edit()) {
            remove("rememberMe")
            apply()
        }
        Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show()
        navController.navigate(R.id.action_chatListFragment_to_loginFragment)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        super.onPrepareOptionsMenu(menu)
        val currentDestination = navController.currentDestination?.id
        if (currentDestination == R.id.registerFragment || currentDestination == R.id.loginFragment) {
            menu?.findItem(R.id.action_delete_account)?.isVisible = false
            menu?.findItem(R.id.action_logout)?.isVisible = false
        } else {
            menu?.findItem(R.id.action_delete_account)?.isVisible = true
            menu?.findItem(R.id.action_logout)?.isVisible = true
        }
        return true
    }

    override fun onBackPressed() {
        val currentDestination = navController.currentDestination?.id
        if (currentDestination == R.id.chatListFragment) {
            showExitConfirmationDialog()
        } else if (currentDestination == R.id.chatFragment) {
            navController.navigateUp()
        } else {
            super.onBackPressed()
        }
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("Exit the Application")
            setMessage("Are you sure you want to exit the application?")
            setPositiveButton("Yes") { _, _ ->
                finish()
            }
            setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        }.create().show()
    }
}