package com.example.padipest.injection

import android.content.Context
import com.example.padipest.data.Repository
import com.example.padipest.data.api.ApiConfig
import com.example.padipest.data.pref.UserPreference
import com.example.padipest.data.pref.dataStore

object Injection {
    fun provideRepository(context: Context): Repository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiUser()
        return Repository.getInstance(apiService, pref)
    }
}