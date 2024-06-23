package com.example.asistenvisual.ui.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraViewModel : ViewModel() {

    private var storageReference: StorageReference? = null

    private val _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String> = _imageUrl

    private val _error = MutableLiveData<Boolean>()
    val error: LiveData<Boolean> = _error

    fun uploadImage(file: File) {
        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CANADA)
        val now = Date()
        val fileName = formatter.format(now)
        storageReference = FirebaseStorage.getInstance().getReference("images/$fileName")

        val compressedImageData = adjustImageOrientation(file)

        if (compressedImageData != null) {
            storageReference?.putBytes(compressedImageData)?.addOnSuccessListener {
                storageReference?.getDownloadUrl()?.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    _imageUrl.value = imageUrl
                }?.addOnFailureListener {
                    _error.value = true
                }
            }?.addOnFailureListener {
                _error.value = true
            }
        }
    }

    private fun adjustImageOrientation(file: File): ByteArray? {
        val exifInterface = ExifInterface(file.absolutePath)
        val orientation = exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
        )

        val rotationAngle = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        if (bitmap == null) {
            println("Failed to decode Bitmap from file")
            return null
        }

        val matrix = Matrix().apply { postRotate(rotationAngle.toFloat()) }
        val rotatedBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        val baos = ByteArrayOutputStream()
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        return baos.toByteArray()
    }
}