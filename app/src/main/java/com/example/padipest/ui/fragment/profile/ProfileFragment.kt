package com.example.padipest.ui.fragment.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.padipest.R
import com.example.padipest.databinding.FragmentHomeBinding
import com.example.padipest.databinding.FragmentProfileBinding
import com.example.padipest.ui.EditProfileActivity
import com.example.padipest.ui.SplashScreenActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    private lateinit var firebaseAuth: FirebaseAuth

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        firebaseAuth = FirebaseAuth.getInstance()

        val user = firebaseAuth.currentUser

        user?.let {
            val name = it.displayName
            val email = it.email
            val photoUrl = it.photoUrl

            binding.profileEmail.text = email
            binding.profileName.text = name
            activity?.let { it1 ->
                Glide.with(it1)
                    .load(photoUrl)
                    .error(R.drawable.baseline_account_circle_24)
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .into(binding.profileImage)
            }

        }

        binding.btnEditProfile.setOnClickListener{
            startActivity(Intent(activity, EditProfileActivity::class.java))
        }

        binding.btnLogout.setOnClickListener{
            context?.let {
                AlertDialog.Builder(it).apply {
                    setTitle("Logout")
                    setMessage("Yakin mau logout?")
                    setPositiveButton("Ya") { _, _ ->
                        lifecycleScope.launch {
                            val credentialManager = context.let { it1 -> CredentialManager.create(it1) }

                            firebaseAuth.signOut()
                            credentialManager.clearCredentialState(ClearCredentialStateRequest())
                            startActivity(Intent(activity, SplashScreenActivity::class.java))
                            activity?.finish()
                        }
                    }
                    setNegativeButton("Tidak") { dialog, _ ->
                        dialog.cancel()
                    }
                    create()
                    show()
                }
            }

        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}