package com.example.storyapp.detail

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.storyapp.ViewModelFactory
import com.example.storyapp.databinding.ActivityDetailBinding
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private val viewModel: DetailViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val userId = intent.getStringExtra("EXTRA_ID") ?: return
        loadDetailStory(userId)
    }

    private fun loadDetailStory(userId: String) {
        lifecycleScope.launch {
            showLoading(true)
            try {
                val story = viewModel.fetchStoryById(userId)
                showLoading(false)
                bindDetailStory(story.story)
            } catch (e: Exception) {
                showLoading(false)
                showError(e.message ?: "An error occurred while fetching story details")
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        binding.tvDetailDescription.text = message
    }

    private fun bindDetailStory(story: Story) {
        binding.tvDetailName.text = story.name
        binding.tvDetailDescription.text = story.description
        Glide.with(this)
            .load(story.photoUrl)
            .into(binding.ivDetailPhoto)
    }
}