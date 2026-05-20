package com.example.dacs_3_composer.data.repository

import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.dacs_3_composer.data.model.Dish
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MenuRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val dishCollection = firestore.collection("dishes")
    private val TAG = "MenuRepository"

//    Gọi viewmodel để lấy dữ liệu (callbackFlow lắng nghe nếu tác động thảy đổi để load lại)
fun getDishes(restaurantId: String): Flow<List<Dish>> = callbackFlow {
    val subscription = dishCollection
        .whereEqualTo("restaurantId", restaurantId)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Lỗi lắng nghe dữ liệu: ${error.message}")
                close(error)
                return@addSnapshotListener
            }

            // 🌟 SỬA ĐOẠN NÀY ĐỂ LẤY ĐƯỢC ID DOCUMENT THẬT
            val dishes = snapshot?.documents?.mapNotNull { document ->
                val dish = document.toObject(Dish::class.java)
                // Bốc cái ID Document thật của Firebase gán vào trường id của Model Dish
                dish?.copy(id = document.id)
            } ?: emptyList()

            trySend(dishes)
        }
    awaitClose { subscription.remove() }
}
//  Xử lý tải ảnh lên
    suspend fun uploadImage(imageUri: Uri, uploadPreset: String): String = suspendCoroutine { continuation ->
        Log.d(TAG, "Bắt đầu upload ảnh: $imageUri với preset: $uploadPreset")
        try {
            MediaManager.get().upload(imageUri)
                .unsigned(uploadPreset)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                        val url = resultData?.get("secure_url") as? String ?: ""
                        Log.d(TAG, "Upload ảnh thành công: $url")
                        continuation.resume(url)
                    }
                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        val errorMsg = error?.description ?: "Unknown Cloudinary error"
                        Log.e(TAG, "Upload ảnh thất bại: $errorMsg")
                        continuation.resumeWithException(Exception(errorMsg))
                    }
                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                }).dispatch()
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi gọi MediaManager: ${e.message}")
            continuation.resumeWithException(e)
        }
    }

    suspend fun addDish(dish: Dish) {
        try {
            Log.d(TAG, "Đang thêm món vào Firestore: ${dish.name}")
            dishCollection.add(dish).await()
            Log.d(TAG, "Thêm món thành công!")
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi Firestore addDish: ${e.message}")
            throw e
        }
    }

    suspend fun updateDish(dish: Dish) {
        if (dish.id.isNotEmpty()) {
            dishCollection.document(dish.id).set(dish).await()
        }
    }

    suspend fun deleteDish(dishId: String) {
        dishCollection.document(dishId).delete().await()
    }
}
