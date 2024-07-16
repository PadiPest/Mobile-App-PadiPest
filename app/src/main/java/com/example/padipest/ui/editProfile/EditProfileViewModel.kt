package com.example.padipest.ui.editProfile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.padipest.data.Repository
import com.example.padipest.data.pref.UserModel
import com.example.padipest.data.response.Response
import com.example.padipest.data.response.UserResponse
import com.example.padipest.ui.login.LoginViewModel
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class EditProfileViewModel (private val repository: Repository) : ViewModel() {

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            repository.saveSession(user)
        }
    }

    private val _result = MutableLiveData<Response>()
    val result: LiveData<Response> = _result

    private val _resultGet = MutableLiveData<UserResponse>()
    val resultGet: LiveData<UserResponse> = _resultGet

    companion object {
        private const val TAG = "EditViewModel"
    }

    fun update(file: MultipartBody.Part, name: RequestBody, id: String) {

        viewModelScope.launch {

            try {
                _result.value = repository.update(file, name, id)
            }catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, Response::class.java)
                Log.e(TAG, "update: $errorResponse")
                _result.value = errorResponse
            }

        }

    }

    fun updateImage(file: MultipartBody.Part, id: String) {

        viewModelScope.launch {

            try {
                _result.value = repository.updateImage(file, id)
            }catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, Response::class.java)
                Log.e(TAG, "update: $errorResponse")
                _result.value = errorResponse
            }

        }

    }

    fun updateName(name: RequestBody, id: String) {

        viewModelScope.launch {

            try {
                _result.value = repository.updateName(name, id)
            }catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, Response::class.java)
                Log.e(TAG, "update: $errorResponse")
                _result.value = errorResponse
            }

        }

    }

    fun getUser(id: String) {
        viewModelScope.launch {

            try {
                _resultGet.value = repository.getUser(id)
            }catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, UserResponse::class.java)
                Log.e(TAG, "sign: $errorResponse")
                Log.e(TAG, "sign: $errorBody")
                _resultGet.value = errorResponse
            }

        }
    }

}