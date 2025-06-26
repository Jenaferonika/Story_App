package com.example.storyapp.addStory

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SwitchCompat
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.storyapp.main.MainActivity
import com.example.storyapp.main.MainViewModel
import com.example.storyapp.R
import com.example.storyapp.ViewModelFactory
import com.example.storyapp.addStory.addStoryHelper.Companion.compressImageToFile
import com.example.storyapp.databinding.ActivityAddStoryBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import android.Manifest

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private var imageUri: Uri? = null
    private lateinit var locationSwitch: SwitchCompat
    private var isLocationEnabled = false


    private val viewModel: addStoryViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    private val mainViewModel: MainViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationSwitch = findViewById(R.id.switch_location)

        locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            isLocationEnabled = isChecked
            if (isChecked) {
                getCurrentLocationPermission()
            } else {
                Toast.makeText(this, "Location disabled", Toast.LENGTH_SHORT).show()
            }
        }

        setupListeners()
    }

    private fun getCurrentLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            Toast.makeText(this, "Location enabled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        binding.btnGallery.setOnClickListener { openGallery() }
        binding.btnCamera.setOnClickListener { openCamera() }
        binding.buttonAdd.setOnClickListener { uploadImage() }
    }

    private fun uploadImage() {
        imageUri?.let { uri ->
            val imageFile = addStoryHelper.convertUriToFile(this, uri).compressImageToFile()
            val description = binding.edAddDescription.text.toString()

            if (description.isEmpty()) {
                showToast("Please enter a description")
                return
            }

            showLoading(true)

            getCurrentLocation { lat, lon ->
                viewModel.addStory(imageFile, description, lat, lon) { result ->
                    showLoading(false)
                    if (result.error) {
                        showToast(result.message)
                    } else {
                        showToast("Upload successful: ${result.message}")
                        finish()
                    }
                }
            }
        } ?: showToast("Please select an image")
    }

    private fun getCurrentLocation(onLocationResult: (Double?, Double?) -> Unit) {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener(OnSuccessListener { location ->
                    if (location != null) {
                        onLocationResult(location.latitude, location.longitude)
                    } else {
                        onLocationResult(null, null)
                    }
                })
                .addOnFailureListener(OnFailureListener {
                    showToast("Failed to get location")
                    onLocationResult(null, null)
                })
        } catch (e: SecurityException) {
            showToast("Location permission is not granted")
            onLocationResult(null, null)
        }
    }

    private fun openGallery() {
        galleryPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun openCamera() {
        imageUri = addStoryHelper.generateImageUri(this)
        cameraCapture.launch(imageUri!!)
    }

    private fun previewImage() {
        imageUri?.let {
            binding.previewImage.setImageURI(it)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMainPage() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        setResult(RESULT_OK)
        startActivity(intent)
    }

    private val galleryPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            previewImage()
        } ?: Log.d("Gallery", "Tidak ada gambar yang dipilih")
    }

    private val cameraCapture = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) previewImage()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }
}
