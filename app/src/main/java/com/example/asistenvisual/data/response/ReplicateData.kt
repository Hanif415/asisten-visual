package com.example.asistenvisual.data.response


data class MyData(
    var input: Inputs,
    var version: String
)

data class Inputs(
    var image: String,
    var prompt: String,
    var maxNewToken: Int,
    var temperature: Double,
    var top_p: Int
)
