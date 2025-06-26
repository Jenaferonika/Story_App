package com.example.storyapp.register

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.storyapp.R
import com.example.storyapp.User.UserPreference
import com.example.storyapp.ViewModelFactory
import com.example.storyapp.databinding.ActivityRegisterBinding
import com.example.storyapp.login.LoginActivity
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var userPreference: UserPreference

    private val viewModel by viewModels<RegisterViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        setupView()
        observeViewModel()
        actionListener()
        playAnimation()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun actionListener() {
        binding.btnRegister.setOnClickListener {
            val name = binding.edRegisterName.text.toString()
            val email = binding.edRegisterEmail.text.toString()
            val password = binding.edRegisterPassword.text.toString()

            var valid = true
            when {
                name.isEmpty() -> binding.edRegisterName.setErrorIfEmpty(getString(R.string.field_empty))
                    .also { valid = false }

                email.isEmpty() -> binding.edRegisterEmail.setErrorIfEmpty(getString(R.string.field_empty))
                    .also { valid = false }

                password.isEmpty() -> binding.edRegisterPassword.setErrorIfEmpty(getString(R.string.field_empty))
                    .also { valid = false }

                password.length < 8 -> {
                    binding.edRegisterPassword.error = "Password harus berisi minimal 8 karakter"
                    valid = false
                }
            }

            val emailPattern = Patterns.EMAIL_ADDRESS
            if (email.isEmpty()) {
                binding.edRegisterEmail.setErrorIfEmpty(getString(R.string.field_empty))
                valid = false
            } else if (!emailPattern.matcher(email).matches()) {
                binding.edRegisterEmail.error = "Format email tidak valid"
                valid = false
            }


            if (valid) {
                viewModel.userRegister(name, email, password)
            }
        }
    }

    private fun EditText.setErrorIfEmpty(message: String) {
        if (text.isEmpty()) {
            error = message
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("Sukses")
            setMessage("Registrasi Berhasil")
            setPositiveButton("Lanjut") { _, _ ->
                val mainIntent = Intent(context, LoginActivity::class.java)
                mainIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(mainIntent)
                finish()
            }
            create()
            show()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.registerState.collect { state ->
                when (state) {
                    is RegisterState.Loading -> {

                    }
                    is RegisterState.Success -> {
                        showToast(state.response.message ?: "Registrasi berhasil")
                        showDialog()
                    }
                    is RegisterState.Error -> {
                        showToast(state.message)
                    }
                    RegisterState.Idle -> {

                    }
                }
            }
        }
    }

    private fun playAnimation() {
        val cardViewAnim = ObjectAnimator.ofFloat(binding.cardView, "alpha", 0f, 1f).apply {
            duration = 1500
            interpolator = AccelerateDecelerateInterpolator()
        }

        val buttonAnim = ObjectAnimator.ofFloat(binding.btnRegister, "translationY", 100f, 0f).apply {
            duration = 2000
            interpolator = OvershootInterpolator()
        }

        val textAnim = ObjectAnimator.ofFloat(binding.hiWelcome, "translationX", -200f, 0f).apply {
            duration = 2000
            interpolator = AccelerateDecelerateInterpolator()
        }

        val animSet = AnimatorSet().apply {
            playTogether(cardViewAnim, buttonAnim, textAnim)
        }

        animSet.start()
    }
}
