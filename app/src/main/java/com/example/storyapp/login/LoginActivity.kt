package com.example.storyapp.login

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.example.storyapp.Injection
import com.example.storyapp.main.MainActivity
import com.example.storyapp.User.UserModel
import com.example.storyapp.User.UserPreference
import com.example.storyapp.ViewModelFactory
import com.example.storyapp.api.ApiConfig
import com.example.storyapp.databinding.ActivityLoginBinding
import com.example.storyapp.register.RegisterActivity
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var userPreference: UserPreference
    private val viewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreference = UserPreference.getInstance(applicationContext.dataStore)

        lifecycleScope.launch {
            userPreference.getSession().collect { user ->
                if (user.isLogin) {
                    navigateToMainActivity() // Arahkan ke MainActivity jika sudah login
                }
            }
        }

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        initializeUI()
        setButtonAction()
        playAnimation()
    }

    private fun initializeUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        supportActionBar?.hide()
    }

    private fun setButtonAction() {
        binding.btnLogin.setOnClickListener {
            val email = binding.edLoginEmail.text.toString().trim()
            val password = binding.edLoginPassword.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                showToast("Email dan Password tidak boleh kosong")
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        lifecycleScope.launch {
            try {
                showLoadingIndicator(true)

                val response = viewModel.userLogin(email, password)

                val userPreference = UserPreference.getInstance(applicationContext.dataStore)
                userPreference.setUserSession(
                    UserModel(
                        email = email,
                        token = response.loginResult.token,
                        isLogin = true
                    )
                )

                handleLoginSuccess(response)

            } catch (e: Exception) {
                showLoadingIndicator(false)
                showToast("Login gagal: ${e.message}")
            }
        }
    }

    private fun handleLoginSuccess(response: LoginResponse) {
        val token = response.loginResult.token
        viewModel.saveSession(UserModel(response.loginResult.name, token, isLogin = true))
        updateApiService(token)
        showToast(response.message)
        showLoadingIndicator(false)
        navigateToMainActivity()
    }

    private fun updateApiService(token: String) {
        val Repository = Injection.provideRepository(applicationContext)
        Repository.updateApiService(ApiConfig.getApiService(token))
    }

    private fun showLoadingIndicator(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMainActivity() {
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(mainIntent)
        finish()
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.gambar, View.TRANSLATION_Y, -50f, 0f).apply {
            duration = 1500
            start()
        }

        ObjectAnimator.ofFloat(binding.cardView, View.ALPHA, 0f, 1f).apply {
            duration = 1500
            startDelay = 500
            start()
        }

        ObjectAnimator.ofFloat(binding.btnLogin, View.SCALE_X, 0.8f, 1f).apply {
            duration = 1000
            startDelay = 2000
            start()
        }

        ObjectAnimator.ofFloat(binding.btnLogin, View.SCALE_Y, 0.8f, 1f).apply {
            duration = 1000
            startDelay = 2000
            start()
        }

        ObjectAnimator.ofFloat(binding.tvRegister, View.ALPHA, 0f, 1f).apply {
            duration = 1500
            startDelay = 3000
            start()
        }
    }
}
