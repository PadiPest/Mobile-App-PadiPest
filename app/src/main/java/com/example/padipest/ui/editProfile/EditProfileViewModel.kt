package com.example.padipest.ui.editProfile

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.padipest.data.Repository
import com.example.padipest.data.pref.UserModel

class EditProfileViewModel (private val repository: Repository) : ViewModel() {

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

}