package com.example.padipest.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.padipest.R
import com.example.padipest.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

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

            val email = binding.emailEditText.text.toString()
            val pass = binding.passwordEditText.text.toString()
            val confirmPass = binding.confirmPasswordEditText.text.toString()

            binding.progressBar.visibility = View.VISIBLE

            if (email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
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
                            binding.progressBar.visibility = View.GONE
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
                        else {
                            binding.progressBar.visibility = View.GONE
                            AlertDialog.Builder(this@RegisterActivity).apply {
                                setTitle("Register")
                                setMessage(it.exception.toString())
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
}