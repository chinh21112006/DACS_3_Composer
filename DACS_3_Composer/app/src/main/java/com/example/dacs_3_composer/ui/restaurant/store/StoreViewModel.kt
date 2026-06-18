package com.example.dacs_3_composer.ui.restaurant.store

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs_3_composer.data.model.DishItem
import com.example.dacs_3_composer.data.model.RestaurantDetail
import com.example.dacs_3_composer.data.repository.MenuRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class StoreViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val repository = MenuRepository()
    private val TAG = "StoreViewModel"

    var restaurantDetail = mutableStateOf<RestaurantDetail?>(null)
    var dishesList = mutableStateOf<List<DishItem>>(emptyList())
    var isLoading = mutableStateOf(false)

    fun fetchStoreData(restaurantId: String) {
        if (restaurantId.isEmpty()) return

        isLoading.value = true
        
        // 🎯 Lắng nghe Realtime thông tin nhà hàng
        firestore.collection("restaurants").document(restaurantId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    restaurantDetail.value = snapshot.toObject(RestaurantDetail::class.java)
                } else {
                    createDefaultRestaurant(restaurantId)
                }
                isLoading.value = false
            }

        // Lấy danh sách món ăn
        viewModelScope.launch {
            try {
                repository.getDishes(restaurantId).collect { dishList ->
                    val availableDishes = dishList.filter { it.available == true }
                    dishesList.value = availableDishes.map { dish ->
                        DishItem(
                            id = dish.id,
                            name = dish.name,
                            price = dish.price,
                            description = dish.description,
                            imageUrl = dish.imageUrl,
                            category = dish.category
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching dishes: ${e.message}")
            }
        }
    }

    private fun createDefaultRestaurant(restaurantId: String) {
        val defaultStore = RestaurantDetail(
            id = restaurantId,
            name = "Cửa hàng của tôi",
            openTime = "08:00",
            closeTime = "22:00"
        )
        firestore.collection("restaurants").document(restaurantId).set(defaultStore)
    }

    fun updateRestaurantInfo(
        restaurantId: String,
        newName: String,
        newDescription: String,
        newAddress: String,
        onSuccess: () -> Unit
    ) {
        val updates = mapOf(
            "name" to newName,
            "description" to newDescription,
            "address" to newAddress
        )
        firestore.collection("restaurants").document(restaurantId).update(updates)
            .addOnSuccessListener { onSuccess() }
    }

    // Fix lỗi Unresolved reference 'updateCoverImage'
    fun updateCoverImage(
        restaurantId: String,
        newCoverUrl: String,
        onSuccess: () -> Unit
    ) {
        if (newCoverUrl.isEmpty()) return
        firestore.collection("restaurants").document(restaurantId)
            .update("coverImage", newCoverUrl)
            .addOnSuccessListener {
                onSuccess()
                Log.d(TAG, "Cover image updated successfully")
            }
    }

    fun uploadCoverToCloudinary(
        imageUri: Uri,
        restaurantId: String,
        uploadPreset: String = "ml_default",
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val uploadedUrl = repository.uploadImage(imageUri, uploadPreset)
                if (uploadedUrl.isNotBlank()) {
                    firestore.collection("restaurants").document(restaurantId).update("coverImage", uploadedUrl)
                        .addOnSuccessListener { onSuccess() }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Upload failed: ${e.message}")
            }
        }
    }
}
