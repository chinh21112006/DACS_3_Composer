package com.example.dacs_3_composer.ui.user.profile

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs_3_composer.data.repository.MenuRepository // 🌟 Dùng chung repository với nhà hàng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val repository = MenuRepository() // 🌟 Khai báo repository giống StoreViewModel

    var userName by mutableStateOf("Đang tải...")
        private set

    var userEmail by mutableStateOf("Đang tải...")
        private set

    // Trạng thái ảnh: Lưu Uri tệp điện thoại hoặc String link URL mạng
    var avatarState by mutableStateOf<Any?>(null)
        private set

    var isUploading by mutableStateOf(false)
        private set

    fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    userName = document.getString("name") ?: "Khách"
                    userEmail = document.getString("email") ?: auth.currentUser?.email ?: "Chưa cập nhật"
                    avatarState = document.getString("avatarUrl")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileViewModel", "Lỗi tải thông tin profile", e)
            }
    }

    // 🌟 Upload ảnh lên Cloudinary giống hệt bên StoreViewModel của nhà hàng
    fun uploadAvatar(imageUri: Uri, uploadPreset: String = "ml_default") {
        val uid = auth.currentUser?.uid ?: return

        // 🚀 HIỂN THỊ LẬP TỨC: Đổi sang tệp ảnh trong điện thoại ngay trên giao diện
        avatarState = imageUri
        isUploading = true

        viewModelScope.launch {
            try {
                // 1. Đẩy ảnh lên Cloudinary lấy link về
                val uploadedUrl = repository.uploadImage(imageUri, uploadPreset)

                // 2. Lưu link đó vào Firestore của User
                firestore.collection("users").document(uid)
                    .update("avatarUrl", uploadedUrl)
                    .addOnSuccessListener {
                        isUploading = false
                        avatarState = uploadedUrl // Đồng bộ lại bằng link online
                        Log.d("ProfileViewModel", "Cập nhật avatar lên Cloudinary thành công!")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ProfileViewModel", "Lỗi lưu link Firestore: ${e.message}")
                        isUploading = false
                    }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Lỗi upload ảnh đại diện: ${e.message}")
                isUploading = false
            }
        }
    }
}