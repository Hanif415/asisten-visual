package com.example.asistenvisual.data.response

import com.google.gson.annotations.SerializedName

data class ResultResponse(

	@field:SerializedName("output")
	val output: List<String?>? = null,

	@field:SerializedName("status")
	val status: String
)

