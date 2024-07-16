package com.example.padipest.ui.editProfile

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.example.padipest.R
import com.example.padipest.data.pref.UserModel
import com.example.padipest.databinding.ActivityEditProfileBinding
import com.example.padipest.reduceFileImage
import com.example.padipest.ui.GlideApp
import com.example.padipest.ui.MainActivity
import com.example.padipest.ui.ViewModelFactory
import com.example.padipest.ui.login.LoginViewModel
import com.example.padipest.uriToFile
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    private var currentImageUri: Uri? = null

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var name: String
    private lateinit var password: String
    private lateinit var email: String
    private lateinit var imageUrl: String
    private lateinit var id: String

    private val viewModel by viewModels<EditProfileViewModel> {
        ViewModelFactory.getInstance(this)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.hide()

        viewModel.getSession().observe(this) { user ->

            name = user.name
            email = user.email
            password = user.password
            imageUrl = user.imageUrl
            id = user.userId

            binding.nameDisplay.setText(name)
            binding.emailInput.text = email
            binding.passwordInput.setText(password)

            Glide.with(this)
                .load(imageUrl)
                .centerCrop()
                .placeholder(R.drawable.baseline_account_circle_24)
                .transition(withCrossFade())
                .into(binding.profileImage)

        }

        binding.editProfileImageButton.setOnClickListener { startGallery() }

        firebaseAuth = FirebaseAuth.getInstance()

        binding.saveButton.setOnClickListener {

            binding.progressBar.visibility = View.VISIBLE
            val inputPassword = binding.passwordInput.text.toString()
            val inputName = binding.nameDisplay.text.toString()

            if (password != inputPassword) {

                if (inputPassword.length < 8) {
                    binding.progressBar.visibility = View.GONE
                    binding.passwordInput.error = "Password tidak boleh kurang dari 8 karakter"
                } else if (inputName != name && currentImageUri != null) {
                    updateAll(inputPassword, inputName)
                } else {
                    changePassword(inputPassword)
                }

            } else if (inputName != name && currentImageUri != null) {
                updateImageAndName(inputPassword, inputName)
            } else if (inputName != name) {
                updateName(inputPassword, inputName)
            } else if (currentImageUri != null) {
                updateImage(inputPassword)
            }

        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun updateAll(pass: String, name: String) {

        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            Log.d("Image File", "showImage: ${imageFile.path}")

            val requestBody = name.toRequestBody("text/plain".toMediaType())
            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData(
                "image",
                imageFile.name,
                requestImageFile
            )

            viewModel.update(multipartBody, requestBody, id)

            viewModel.result.observe(this) { result ->

                if (result.status == "success") {

                    val user = firebaseAuth.currentUser

                    val credential = EmailAuthProvider
                        .getCredential(email, password)

                    user?.reauthenticate(credential)?.addOnCompleteListener {
                        if (it.isSuccessful) {
                            user.updatePassword(pass)
                                .addOnCompleteListener{ task ->
                                    if (task.isSuccessful) {
                                        viewModel.getUser(id)

                                        viewModel.resultGet.observe(this) { dataResult ->

                                            Log.d(TAG, "updateImageAndName: ${dataResult.data.pictureUrl} ${dataResult.data.name}")
                                            viewModel.saveSession(UserModel(dataResult.data.userId, dataResult.data.name, dataResult.data.pictureUrl, email, pass))
                                            dialog(result.message)

                                        }
                                    }
                                    else {
                                        dialog("Error password not updated")
                                        binding.progressBar.visibility = View.GONE
                                    }
                                }
                        }
                    }

                }else {
                    dialog("${result.status} \n ${result.message}")
                }

            }

        } ?: dialog("gagal")

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun updateImage(pass: String) {
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            Log.d("Image File", "showImage: ${imageFile.path}")

            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData(
                "image",
                imageFile.name,
                requestImageFile
            )

            viewModel.updateImage(multipartBody, id)

            viewModel.result.observe(this) { result ->

                if (result.status == "success") {

                    viewModel.getUser(id)

                    viewModel.resultGet.observe(this) { dataResult ->

                        Log.d(TAG, "updateImageAndName: ${dataResult.data.pictureUrl} ${dataResult.data.name}")

                        viewModel.saveSession(UserModel(dataResult.data.userId, dataResult.data.name, dataResult.data.pictureUrl, email, pass))

                        dialog(result.message)


                    }

                }else {
                    dialog("${result.status} \n ${result.message}")
                }

            }

        } ?: dialog("gagal")
    }

    private fun updateName(pass: String, name: String) {
        val requestBody = name.toRequestBody("text/plain".toMediaType())

        viewModel.updateName(requestBody, id)

        viewModel.result.observe(this) { result ->

            if (result.status == "success") {

                viewModel.getUser(id)

                viewModel.resultGet.observe(this) { dataResult ->

                    Log.d(TAG, "updateImageAndName: ${dataResult.data.pictureUrl} ${dataResult.data.name}")

                    viewModel.saveSession(UserModel(dataResult.data.userId, dataResult.data.name, dataResult.data.pictureUrl, email, pass))

                    dialog(result.message)

                }

            }else {
                dialog("${result.status} \n ${result.message}")
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun updateImageAndName(pass: String, name: String) {

        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            Log.d("Image File", "showImage: ${imageFile.path}")

            val requestBody = name.toRequestBody("text/plain".toMediaType())
            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData(
                "image",
                imageFile.name,
                requestImageFile
            )

            viewModel.update(multipartBody, requestBody, id)

            viewModel.result.observe(this) { result ->

                if (result.status == "success") {

                    viewModel.getUser(id)

                    viewModel.resultGet.observe(this) { dataResult ->

                        Log.d(TAG, "updateImageAndName: ${dataResult.data.pictureUrl} ${dataResult.data.name}")

                        viewModel.saveSession(UserModel(dataResult.data.userId, dataResult.data.name, dataResult.data.pictureUrl, email, pass))

                        dialog(result.message)


                    }

                }else {
                    dialog("${result.status} \n ${result.message}")
                }

            }

        } ?: dialog("gagal")

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
            binding.profileImage.setImageURI(it)
        }
    }

    private fun changePassword(newPassword : String)  {

        val user = firebaseAuth.currentUser

        val credential = EmailAuthProvider
            .getCredential(email, password)

        user?.reauthenticate(credential)?.addOnCompleteListener {
            if (it.isSuccessful) {
                user.updatePassword(newPassword)
                    .addOnCompleteListener{ task ->
                        if (task.isSuccessful) {
                            viewModel.saveSession(UserModel(id, name, imageUrl, email, newPassword))
                            dialog("Update Password Success")
                            binding.progressBar.visibility = View.GONE
                        }
                        else {
                            dialog("Error password not updated")
                            binding.progressBar.visibility = View.GONE
                        }
                    }
            }
        }

    }

    private fun dialog(text: String) {
        AlertDialog.Builder(this@EditProfileActivity).apply {
            setTitle("Update")
            setMessage(text)
            setPositiveButton("Ok") { _, _ ->
                finish()
            }
            create()
            show()
        }
    }

    companion object {
        private const val TAG = "EditProfileActivity"
    }

}