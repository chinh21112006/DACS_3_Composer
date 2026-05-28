package com.example.dacs_3_composer.ui.admin.settings

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

class AdminProfileViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val menuRepository = MenuRepository()
    private val TAG = "AdminProfileVM"

    var name by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var avatarUrl by mutableStateOf("")
        private set
    var coverUrl by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    init {
        loadAdminProfileData()
    }

    private fun loadAdminProfileData() {
        if (currentUserId.isBlank()) return

        firestore.collection("users").document(currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Lỗi tải thông tin admin: ", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    name = snapshot.getString("name") ?: "Admin"
                    email = snapshot.getString("email") ?: ""
                    avatarUrl = snapshot.getString("avatarUrl") ?: ""
                    // Admin might not have a specific 'restaurants' entry for cover, 
                    // but we can use a default or store it in 'users' if needed.
                    // For now, let's see if there's a coverImage in 'users' or use default.
                    coverUrl = snapshot.getString("coverImage") ?: ""
                }
            }
    }

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

    fun uploadAndSaveCover(imageUri: Uri, uploadPreset: String = "ml_default") {
        if (currentUserId.isBlank()) return
        viewModelScope.launch {
            isLoading = true
            try {
                val uploadedUrl = menuRepository.uploadImage(imageUri, uploadPreset)
                if (uploadedUrl.isNotBlank()) {
                    firestore.collection("users").document(currentUserId).update("coverImage", uploadedUrl)
                }
                isLoading = false
            } catch (e: Exception) {
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

    fun logout() {
        auth.signOut()
    }
}
