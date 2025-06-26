package com.example.storyapp.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.R
import com.example.storyapp.User.UserPreference
import com.example.storyapp.User.dataStore
import com.example.storyapp.ViewModelFactory
import com.example.storyapp.addStory.AddStoryActivity
import com.example.storyapp.databinding.ActivityMainBinding
import com.example.storyapp.login.LoginActivity
import com.example.storyapp.maps.MapsActivity
import com.example.storyapp.story.ListStoryAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var listStoryAdapter: ListStoryAdapter
    private lateinit var userPreference: UserPreference
    private val mainViewModel: MainViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    companion object {
        private const val REQUEST_ADD_STORY = 1
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreference = UserPreference.getInstance(applicationContext.dataStore)
        val listStoryAdapter = ListStoryAdapter()

        setupRecyclerView()

        mainViewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                navigateToLogin()
            } else {
                Log.d(TAG, "User logged in: ${user.token}")
            }
        }

        mainViewModel.story.observe(this) { pagingData ->
            Log.d("PagingData", "Data Received: $pagingData")
            listStoryAdapter.submitData(lifecycle, pagingData)
        }

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivityForResult(intent, REQUEST_ADD_STORY)
        }

        setupFullScreenView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ADD_STORY && resultCode == RESULT_OK) {
            listStoryAdapter.refresh() // Refresh data after adding a story
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_logout, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            R.id.action_maps -> {
                startActivity(Intent(this, MapsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupFullScreenView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private fun setupRecyclerView() {
        listStoryAdapter = ListStoryAdapter()
        binding.rvStoryList.layoutManager = LinearLayoutManager(this)
        binding.rvStoryList.adapter = listStoryAdapter.withLoadStateFooter(
            footer = LoadingAdapter {
                listStoryAdapter.retry()
            }
        )

        showLoading(true)
        mainViewModel.story.observe(this) { pagingData ->
        listStoryAdapter.submitData(lifecycle, pagingData)
            showLoading(false)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("Konfirmasi Logout")
            setMessage("Apakah Anda yakin ingin keluar?")
            setPositiveButton("Ya") { _, _ ->
                mainViewModel.logout()
                navigateToLogin()
            }
            setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            create()
            show()
        }
    }
}
