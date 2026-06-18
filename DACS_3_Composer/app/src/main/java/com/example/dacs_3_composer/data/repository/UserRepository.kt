package com.example.dacs_3_composer.data.repository

import com.example.dacs_3_composer.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getCurrentUser(): User {
        val uid = auth.currentUser?.uid ?: throw Exception("Chưa đăng nhập")
        return getUserById(uid)
    }

    suspend fun getUserById(uid: String): User {
        val snapshot = db.collection("users").document(uid).get().await()
        val user = snapshot.toObject(User::class.java) ?: throw Exception("Không tìm thấy người dùng")
        return user.copy(uid = uid)
    }

    suspend fun updateProfile(user: User) {
        val updateMap = mapOf(
            "name" to user.name,
            "phone" to user.phone,
            "address" to user.address,
            "vehicleName" to user.vehicleName,
            "avatarUrl" to user.avatarUrl
        )

        db.collection("users")
            .document(user.uid)
            .update(updateMap)
            .await()
    }

    suspend fun uploadAvatar(
        imageFile: File,
        uploadPreset: String = "ml_default"
    ): String = withContext(Dispatchers.IO) {
        val cloudName = "dhscw17vq"

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                imageFile.name,
                imageFile.asRequestBody("image/*".toMediaType())
            )
            .addFormDataPart("upload_preset", uploadPreset)
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
            .post(requestBody)
            .build()

        val client = OkHttpClient()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Cloudinary từ chối: Code ${response.code} - ${response.message}")
            }

            val body = response.body?.string() ?: throw Exception("Phản hồi từ Cloudinary rỗng")
            val json = JSONObject(body)

            return@withContext json.getString("secure_url")
        }
    }
}