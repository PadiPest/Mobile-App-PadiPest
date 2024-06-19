package com.example.padipest.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.padipest.data.Repository
import com.example.padipest.data.pref.UserModel
import com.example.padipest.data.response.Response
import com.example.padipest.data.response.UserResponse
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginViewModel (private val repository: Repository) : ViewModel() {

    private val _result = MutableLiveData<UserResponse>()
    val result: LiveData<UserResponse> = _result

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            repository.saveSession(user)
        }
    }

    fun getUser(id: String) {
        viewModelScope.launch {

            try {
                _result.value = repository.getUser(id)
                _isLoading.value = false
            }catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, UserResponse::class.java)
                Log.e(TAG, "sign: $errorResponse")
                Log.e(TAG, "sign: $errorBody")
                _result.value = errorResponse
                _isLoading.value = false
            }

        }
    }

    companion object {
        private const val TAG = "LoginViewModel"
    }

}