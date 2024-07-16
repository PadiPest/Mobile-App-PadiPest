package com.example.padipest.ui.fragment.home

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.padipest.databinding.FragmentHomeBinding
import android.Manifest
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.OrientationEventListener
import android.view.Surface
import androidx.activity.result.PickVisualMediaRequest
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.padipest.data.api.ApiConfig
import com.example.padipest.data.response.UploadResponse
import com.example.padipest.reduceFileImage
import com.example.padipest.ui.CameraActivity
import com.example.padipest.ui.CameraActivity.Companion.CAMERAX_RESULT
import com.example.padipest.uriToFile
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    private var currentImageUri: Uri? = null

    private var imageCapture: ImageCapture? = null

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(activity, "Permission request granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(activity, "Permission request denied", Toast.LENGTH_LONG).show()
            }
        }

    private fun allPermissionsGranted() =
        context?.let {
            ContextCompat.checkSelfPermission(
                it,
                REQUIRED_PERMISSION
            )
        } == PackageManager.PERMISSION_GRANTED

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        binding.cameraButton.setOnClickListener{ startCameraX() }
        binding.galleryButton.setOnClickListener{ startGallery() }
        binding.deteksiButton.setOnClickListener{ uploadImage() }

        return root
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun startCameraX() {
        val intent = Intent(activity, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERAX_RESULT) {
            currentImageUri = it.data?.getStringExtra(CameraActivity.EXTRA_CAMERAX_IMAGE)?.toUri()
            showImage()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun uploadImage() {
        currentImageUri?.let { uri ->
            val imageFile = context?.let { uriToFile(uri, it).reduceFileImage() }
            Log.d("Image File", "showImage: ${imageFile?.path}")

            showLoading(true)

            val requestImageFile = imageFile?.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = requestImageFile?.let {
                MultipartBody.Part.createFormData(
                    "image",
                    imageFile.name,
                    it
                )
            }

            lifecycleScope.launch {
                try {
                    val apiService = ApiConfig.getApiService()
                    val successResponse = multipartBody?.let { apiService.uploadImage(it) }
                    if (successResponse != null) {
                        context?.let {
                            AlertDialog.Builder(it).apply {
                                setTitle("Deteksi")
                                setMessage(successResponse.message)
                                setPositiveButton("Ok") { dialog, _ ->
                                    dialog.cancel()
                                }
                                create()
                                show()
                            }
                        }
                        binding.hasil.text = setText(successResponse)
                    }
                    showLoading(false)
                } catch (e: HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, UploadResponse::class.java)
                    context?.let {
                        AlertDialog.Builder(it).apply {
                            setTitle("Deteksi")
                            setMessage(errorResponse.message)
                            setPositiveButton("Ok") { dialog, _ ->
                                dialog.cancel()
                            }
                            create()
                            show()
                        }
                    }
                    showLoading(false)
                }
            }

        } ?: context?.let {
            AlertDialog.Builder(it).apply {
                setTitle("Deteksi")
                setMessage("Gambar tidak boleh kosong!")
                setPositiveButton("Ok") { dialog, _ ->
                    dialog.cancel()
                }
                create()
                show()
            }
        }
    }

    private fun setText(successResponse: UploadResponse): String {

        val result = "Result : ${successResponse.data.result}"
        val confidenceScore = successResponse.data.confidenceScore
        val confidenceString = confidenceScore.toString().replace(".", ",")
        val convert = if (confidenceString.length >= 5) {
            confidenceString.substring(0, 5)
        } else {
            confidenceString
        }
        val confidence = "Confidence : $convert %"

        return "$result\n$confidence"
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private val orientationEventListener by lazy {
        object : OrientationEventListener(activity) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) {
                    return
                }
                val rotation = when (orientation) {
                    in 45 until 135 -> Surface.ROTATION_270
                    in 135 until 225 -> Surface.ROTATION_180
                    in 225 until 315 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                imageCapture?.targetRotation = rotation
            }
        }
    }

    override fun onStart() {
        super.onStart()
        orientationEventListener.enable()
    }

    override fun onStop() {
        super.onStop()
        orientationEventListener.disable()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }

}