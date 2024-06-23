package com.example.asistenvisual.ui.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.asistenvisual.data.response.Inputs
import com.example.asistenvisual.data.response.MyData
import com.example.asistenvisual.data.response.PredictResponse
import com.example.asistenvisual.data.response.ResultResponse
import com.example.asistenvisual.data.retrofit.ApiConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatViewModel : ViewModel() {

    private val _resultResponse = MutableLiveData<String>()
    val result: LiveData<String> = _resultResponse

    private val _starting = MutableLiveData<Boolean>()
    val starting: LiveData<Boolean> = _starting

    private val _processing = MutableLiveData<Boolean>()
    val processing: LiveData<Boolean> = _processing

    private val _failed = MutableLiveData<Boolean>()
    val failed: LiveData<Boolean> = _failed

    companion object {
        private const val TAG = "MainViewModel"
    }

    fun predict(imageUrl: String, prompt: String) {

        val inputs = Inputs(
            imageUrl, prompt, 1024, 0.2, 1
        )

        val myData = MyData(
            inputs, "01359160a4cff57c6b7d4dc625d0019d390c7c46f553714069f114b392f4a726"
        )

        val client = ApiConfig.getApiService().postTheImage(myData)
        client.enqueue(object : Callback<PredictResponse> {
            override fun onResponse(
                call: Call<PredictResponse>, response: Response<PredictResponse>
            ) {
                val responseBody = response.body()
                if (responseBody != null) {
                    getResult(responseBody.id)
                } else {
                    _failed.value = true
                }
            }

            override fun onFailure(call: Call<PredictResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
            }

        })
    }

    private fun getResult(id: String) {
        val client = ApiConfig.getApiService().getResult(id)
        client.enqueue(object : Callback<ResultResponse> {
            override fun onResponse(
                call: Call<ResultResponse>, response: Response<ResultResponse>
            ) {
                val responseBody = response.body()
                if (responseBody != null) {
                    if (responseBody.status == "starting") {
                        _starting.value = true

                        runBlocking {
                            launch {
                                delay(10000L) // Delay for 2 seconds
                                getResult(id)
                            }
                        }
                    }
                    if (responseBody.status == "processing") {
                        _processing.value = true
                        runBlocking {
                            launch {
                                delay(5000L) // Delay for 2 seconds
                                getResult(id)
                            }
                        }
                    }
                    if (responseBody.status == "succeeded") {
                        if (responseBody.output != null) {
                            _resultResponse.value = responseBody.output.joinToString(" ")
                        } else {
                            _failed.value = true
                        }
                    }
                    if (responseBody.status == "failed") {
                        _failed.value = true
                    }
                }
            }

            override fun onFailure(call: Call<ResultResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
            }
        })
    }
}