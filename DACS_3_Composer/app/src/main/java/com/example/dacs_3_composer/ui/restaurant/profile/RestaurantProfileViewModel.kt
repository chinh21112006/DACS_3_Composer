package com.example.dacs_3_composer.ui.restaurant.profile

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs_3_composer.data.repository.MenuRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class RestaurantProfileViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val menuRepository = MenuRepository()
    private val TAG = "RestaurantProfileVM"

    var name by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var avatarUrl by mutableStateOf("")
        private set

    // 🎯 THÊM BIẾN LƯU LINK ẢNH BÌA
    var coverUrl by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    init {
        loadUserProfileAndStoreData()
    }

    private fun loadUserProfileAndStoreData() {
        if (currentUserId.isBlank()) return

        // 1. Lắng nghe Realtime thông tin cơ bản từ bảng "users"
        firestore.collection("users").document(currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Lỗi tải thông tin user: ", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    name = snapshot.getString("name") ?: ""
                    email = snapshot.getString("email") ?: ""
                    avatarUrl = snapshot.getString("avatarUrl") ?: ""
                }
            }

        // 2. 🎯 LẮNG NGHE REALTIME THÔNG TIN ẢNH BÌA TỪ BẢNG "restaurants"
        firestore.collection("restaurants").document(currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Lỗi tải thông tin nhà hàng: ", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    coverUrl = snapshot.getString("coverImage") ?: ""
                    Log.d(TAG, "Tải thành công ảnh bìa từ Store: $coverUrl")
                }
            }
    }

    // Hàm upload Avatar (Giữ nguyên)
    fun uploadAndSaveAvatar(imageUri: Uri, uploadPreset: String = "ml_default") {
        if (currentUserId.isBlank()) return
        viewModelScope.launch {
            isLoading = true
            try {
                val uploadedUrl = menuRepository.uploadImage(imageUri, uploadPreset)
                if (uploadedUrl.isNotBlank()) {
                    firestore.collection("users").document(currentUserId).update("avatarUrl", uploadedUrl)
                }
                isLoading = false
            } catch (e: Exception) {
                error = "Không thể tải avatar: ${e.message}"
                isLoading = false
            }
        }
    }

    // 🎯 THÊM HÀM UPLOAD ẢNH BÌA LÊN CLOUDINARY VÀ LƯU VÀO COLLECTION RESTAURANTS
    fun uploadAndSaveCover(imageUri: Uri, uploadPreset: String = "ml_default") {
        if (currentUserId.isBlank()) return
        viewModelScope.launch {
            isLoading = true
            try {
                Log.d(TAG, "Đang tải ảnh bìa lên Cloudinary...")
                // 1. Đẩy file lên Cloudinary
                val uploadedUrl = menuRepository.uploadImage(imageUri, uploadPreset)
                if (uploadedUrl.isNotBlank()) {
                    Log.d(TAG, "Đẩy thành công! Tiến hành lưu Firestore bảng restaurants")
                    // 2. Cập nhật vào document nhà hàng tương ứng
                    firestore.collection("restaurants").document(currentUserId)
                        .update("coverImage", uploadedUrl)
                }
                isLoading = false
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi uploadAndSaveCover: ${e.message}")
                error = "Không thể cập nhật ảnh bìa: ${e.message}"
                isLoading = false
            }
        }
    }

    fun updateName(newName: String) {
        if (currentUserId.isBlank() || newName.isBlank()) return
        viewModelScope.launch {
            try {
                firestore.collection("users").document(currentUserId).update("name", newName)
            } catch (e: Exception) {
                error = "Không thể cập nhật tên: ${e.message}"
            }
        }
    }
}