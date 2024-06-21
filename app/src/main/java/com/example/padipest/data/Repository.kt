package com.example.padipest.data

import com.example.padipest.data.api.ApiService
import com.example.padipest.data.pref.UserModel
import com.example.padipest.data.pref.UserPreference
import com.example.padipest.data.response.Response
import com.example.padipest.data.response.UserResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import okhttp3.RequestBody

class Repository private constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference
)  {

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    suspend fun getUser(id: String) : UserResponse {
        return apiService.getUser(id)
    }

    suspend fun update(file: MultipartBody.Part, name: RequestBody, id: String) : Response {
        return apiService.update(id, file, name)
    }

    suspend fun updateImage(file: MultipartBody.Part, id: String) : Response {
        return apiService.updateImage(id, file)
    }

    suspend fun updateName( name: RequestBody, id: String) : Response {
        return apiService.updateName(id, name)
    }

    suspend fun profiles(file: MultipartBody.Part, name: RequestBody, id: RequestBody) : Response {
        return apiService.profiles(file, id, name)
    }

    companion object {
        @Volatile
        private var instance: Repository? = null
        fun getInstance(
            apiService: ApiService,
            userPreference: UserPreference
        ): Repository =
            instance ?: synchronized(this) {
                instance ?: Repository(apiService, userPreference)
            }.also { instance = it }
    }

}