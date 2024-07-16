package com.example.padipest.ui.register

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.padipest.R
import com.example.padipest.databinding.ActivityRegisterBinding
import com.example.padipest.ui.ViewModelFactory
import com.example.padipest.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private val viewModel by viewModels<RegisterViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.hide()

        firebaseAuth = FirebaseAuth.getInstance()

        binding.tvLogin.setOnClickListener{

            finish()

        }

        binding.btnDaftar.setOnClickListener{

            val name = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val pass = binding.passwordEditText.text.toString()
            val confirmPass = binding.confirmPasswordEditText.text.toString()

            binding.progressBar.visibility = View.VISIBLE

            if (email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty() || name.isEmpty()) {
                binding.progressBar.visibility = View.GONE
                AlertDialog.Builder(this@RegisterActivity).apply {
                    setTitle("Register")
                    setMessage("Input tidak boleh kosong!")
                    setPositiveButton("Ok") { dialog, _ ->
                        dialog.cancel()
                    }
                    create()
                    show()
                }
            }
            else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.progressBar.visibility = View.GONE
                binding.emailEditText.error = "Format email salah"
            }
            else if (pass.length < 8) {
                binding.progressBar.visibility = View.GONE
                binding.passwordEditText.error = "Password tidak boleh kurang dari 8 karakter"
            }
            else {
                if (pass == confirmPass){

                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener{

                        if (it.isSuccessful) {

                            val file = convertDrawableToFile(this, R.drawable.baseline_account_circle_24, "profile.jpg")
                            if (file != null) {
                                val requestBodyName = name.toRequestBody("text/plain".toMediaType())
                                val requestBodyId = firebaseAuth.currentUser?.uid?.toRequestBody("text/plain".toMediaType())
                                val requestImageFile = file.asRequestBody("image/jpeg".toMediaType())
                                val multipartBody = MultipartBody.Part.createFormData(
                                    "image",
                                    file.name,
                                    requestImageFile
                                )

                                if (requestBodyId != null) {
                                    viewModel.profiles(multipartBody, requestBodyName, requestBodyId)
                                }

                                viewModel.result.observe(this) {

                                    binding.progressBar.visibility = View.GONE

                                    firebaseAuth.signOut()

                                    AlertDialog.Builder(this@RegisterActivity).apply {
                                        setTitle("Register")
                                        setMessage("Register success!")
                                        setPositiveButton("Ok") { _, _ ->
                                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                            startActivity(intent)
                                            finish()
                                        }
                                        create()
                                        show()
                                    }

                                }


                            } else {
                                Log.d("image", "onCreate: $file")
                            }

                        }
                        else {
                            binding.progressBar.visibility = View.GONE
                            AlertDialog.Builder(this@RegisterActivity).apply {
                                setTitle("Register")
                                setMessage("Register Gagal")
                                setPositiveButton("Ok") { dialog, _ ->
                                    dialog.cancel()
                                }
                                create()
                                show()
                            }
                        }

                    }

                }
                else {
                    binding.progressBar.visibility = View.GONE
                    AlertDialog.Builder(this@RegisterActivity).apply {
                        setTitle("Register")
                        setMessage("Password tidak sama!")
                        setPositiveButton("Ok") { dialog, _ ->
                            dialog.cancel()
                        }
                        create()
                        show()
                    }
                }
            }

        }

    }

    fun convertDrawableToFile(context: Context, drawableId: Int, fileName: String): File? {
        val drawable: Drawable? = ContextCompat.getDrawable(context, drawableId)
        if (drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
            return saveBitmapToFile(context, bitmap, fileName)
        } else {
            val bitmap = drawable?.let {
                Bitmap.createBitmap(it.intrinsicWidth, it.intrinsicHeight, Bitmap.Config.ARGB_8888).apply {
                    val canvas = android.graphics.Canvas(this)
                    it.setBounds(0, 0, canvas.width, canvas.height)
                    it.draw(canvas)
                }
            }
            return bitmap?.let { saveBitmapToFile(context, it, fileName) }
        }
    }

    private fun saveBitmapToFile(context: Context, bitmap: Bitmap, fileName: String): File? {
        val file = File(context.getExternalFilesDir(null), fileName)
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            return file
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

}