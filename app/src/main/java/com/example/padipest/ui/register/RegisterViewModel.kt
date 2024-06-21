package com.example.padipest.ui.register

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.padipest.data.Repository
import com.example.padipest.data.response.Response
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class RegisterViewModel (private val repository: Repository) : ViewModel() {

    private val _result = MutableLiveData<Response>()
    val result: LiveData<Response> = _result

    companion object {
        private const val TAG = "RegisterViewModel"
    }

    fun profiles(file: MultipartBody.Part, name: RequestBody, id: RequestBody) {

        viewModelScope.launch {

            try {
                _result.value = repository.profiles(file, name, id)
            }catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, Response::class.java)
                Log.e(TAG, "upload: $errorResponse")
                _result.value = errorResponse
            }

        }

    }
}