package com.example.dacs_3_composer.data.remote

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object ImageUploadService {

    suspend fun uploadImage(context: Context, imageUri: Uri): String = suspendCancellableCoroutine { continuation ->
        // Cần đảm bảo sử dụng .unsigned() và truyền đúng upload_preset đã tạo trên dashboard Cloudinary
        // Mặc định thường là 'ml_default' cho unsigned upload.
        try {
            MediaManager.get().upload(imageUri)
                .unsigned("ml_default") 
                .option("folder", "chat_images")
                .option("resource_type", "image")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                        val url = resultData["secure_url"] as? String
                        if (url != null) {
                            continuation.resume(url)
                        } else {
                            continuation.resumeWithException(Exception("Không tìm thấy URL trong phản hồi"))
                        }
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        continuation.resumeWithException(Exception(error?.description ?: "Lỗi Cloudinary không xác định"))
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                })
                .dispatch()
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
}
