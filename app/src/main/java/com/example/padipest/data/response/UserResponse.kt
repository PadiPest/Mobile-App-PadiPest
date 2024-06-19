package com.example.padipest.data.response

import com.google.gson.annotations.SerializedName

data class UserResponse(

	@field:SerializedName("data")
	val data: DataUser,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("status")
	val status: String
)

data class DataUser(

	@field:SerializedName("pictureUrl")
	val pictureUrl: String,

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("userId")
	val userId: String
)
