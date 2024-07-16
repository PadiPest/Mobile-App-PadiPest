package com.example.padipest.ui.fragment.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.example.padipest.R
import com.example.padipest.databinding.FragmentProfileBinding
import com.example.padipest.ui.editProfile.EditProfileActivity
import com.example.padipest.ui.SplashScreenActivity
import com.example.padipest.ui.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    private lateinit var firebaseAuth: FirebaseAuth

    private val viewModel by viewModels<ProfileViewModel> {
        ViewModelFactory.getInstance(requireContext())
    }

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewModel.getSession().observe(viewLifecycleOwner) { user ->

            binding.profileName.text = user.name
            binding.profileEmail.text = user.email

            Log.d("TAG", "onCreateView: ${user.imageUrl}")

            activity?.let { it ->
                Glide.with(it)
                    .load(user.imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .transition(withCrossFade())
                    .into(binding.profileImage)
            }

        }

        firebaseAuth = FirebaseAuth.getInstance()

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

                            viewModel.logout()

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