package com.example.dacs_3_composer.ui.restaurant.profile.settings

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs_3_composer.data.model.RestaurantDetail
import com.example.dacs_3_composer.data.repository.MenuRepository
import com.example.dacs_3_composer.data.repository.ActivityLogRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RestaurantInfoViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val repository = MenuRepository()
    private val activityLogRepository = ActivityLogRepository()
    private val TAG = "RestaurantInfoVM"

    private val restaurantId: String
        get() = auth.currentUser?.uid ?: ""

    var restaurantDetail by mutableStateOf<RestaurantDetail?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var isSaving by mutableStateOf(false)
        private set
    var saveSuccess by mutableStateOf(false)
        private set

    init {
        fetchRestaurantData()
    }

    fun fetchRestaurantData() {
        if (restaurantId.isBlank()) return
        firestore.collection("restaurants").document(restaurantId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to restaurant data", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    restaurantDetail = snapshot.toObject(RestaurantDetail::class.java)
                } else if (snapshot != null && !snapshot.exists()) {
                    createDefaultData()
                }
            }
    }

    private fun createDefaultData() {
        viewModelScope.launch {
            try {
                val userDoc = firestore.collection("users").document(restaurantId).get().await()
                val default = RestaurantDetail(
                    id = restaurantId,
                    name = userDoc.getString("name") ?: "Nhà hàng mới",
                    email = userDoc.getString("email") ?: "",
                    avatarUrl = userDoc.getString("avatarUrl") ?: "",
                    openTime = "",
                    closeTime = ""
                )
                firestore.collection("restaurants").document(restaurantId).set(default).await()
            } catch (e: Exception) {
                Log.e(TAG, "Error creating default data", e)
            }
        }
    }

    fun updateRestaurantInfo(
        name: String,
        email: String,
        phone: String,
        address: String,
        openTime: String,
        closeTime: String,
        description: String,
        avatarUri: Uri? = null,
        coverUri: Uri? = null
    ) {
        if (restaurantId.isBlank()) return
        viewModelScope.launch {
            isSaving = true
            saveSuccess = false
            try {
                var finalAvatarUrl = restaurantDetail?.avatarUrl ?: ""
                var finalCoverUrl = restaurantDetail?.coverImage ?: ""
                
                if (avatarUri != null) {
                    val uploadedAvatar = repository.uploadImage(avatarUri, "ml_default")
                    if (uploadedAvatar.isNotBlank()) {
                        finalAvatarUrl = uploadedAvatar
                        firestore.collection("users").document(restaurantId).update("avatarUrl", finalAvatarUrl)
                    }
                }

                if (coverUri != null) {
                    val uploadedCover = repository.uploadImage(coverUri, "ml_default")
                    if (uploadedCover.isNotBlank()) {
                        finalCoverUrl = uploadedCover
                    }
                }

                val updates = mutableMapOf<String, Any>(
                    "name" to name,
                    "email" to email,
                    "phone" to phone,
                    "address" to address,
                    "openTime" to openTime,
                    "closeTime" to closeTime,
                    "description" to description,
                    "avatarUrl" to finalAvatarUrl,
                    "coverImage" to finalCoverUrl
                )

                firestore.collection("restaurants").document(restaurantId).update(updates).await()
                firestore.collection("users").document(restaurantId).update("name", name).await()

                // 🎯 LOG ACTIVITY
                activityLogRepository.logActivity(
                    restaurantId = restaurantId,
                    type = "profile",
                    title = "Chỉnh sửa hồ sơ",
                    description = "Cập nhật thông tin chi tiết nhà hàng và hình ảnh"
                )

                saveSuccess = true
            } catch (e: Exception) {
                Log.e(TAG, "Error updating restaurant info: ${e.message}")
            } finally {
                isSaving = false
            }
        }
    }

    fun resetSaveSuccess() {
        saveSuccess = false
    }
}
