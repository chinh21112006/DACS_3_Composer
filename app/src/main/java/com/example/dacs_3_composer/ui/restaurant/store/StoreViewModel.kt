package com.example.dacs_3_composer.ui.restaurant.store

import android.content.Context
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
        if (restaurantId.isEmpty()) {
            Log.e(TAG, "restaurantId is empty")
            return
        }

        isLoading.value = true
        Log.d(TAG, "Fetching data for ID: $restaurantId")

        // --- PHẦN 1: LẤY THÔNG TIN NHÀ HÀNG (GIỮ NGUYÊN) ---
        firestore.collection("restaurants").document(restaurantId)
            .get()
            .addOnSuccessListener { document ->
                try {
                    if (document.exists()) {
                        val data = document.toObject(RestaurantDetail::class.java)
                        restaurantDetail.value = data
                        Log.d(TAG, "Restaurant data loaded successfully")
                    } else {
                        Log.d(TAG, "No restaurant found, creating default...")
                        createDefaultRestaurant(restaurantId)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing RestaurantDetail: ${e.message}")
                    createDefaultRestaurant(restaurantId)
                }
                isLoading.value = false
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Firestore error: ${e.message}")
                isLoading.value = false
            }

        // --- PHẦN 2: 🌟 SỬA ĐOẠN NÀY - BÊ HÀM FLOW CỦA CODE CŨ SANG ---
        // Vì repository.getDishes là một hàm suspend/Flow nên bắt buộc phải bọc trong viewModelScope.launch
        // 🌟 SỬA TRONG STOREVIEWMODEL.KT
        viewModelScope.launch {
            try {
                repository.getDishes(restaurantId).collect { dishList ->
                    // 1. Lọc lấy các món đang bật (available == true)
                    val availableDishes = dishList.filter { dish -> dish.available == true }

                    // 2. ĐỔI TẠI ĐÂY: Dùng 'availableDishes' để map, thay vì 'dishList' như cũ
                    dishesList.value = availableDishes.map { dish -> // 🌟 Sửa chữ 'dishList' thành 'availableDishes'
                        DishItem(
                            id = dish.id,
                            name = dish.name,
                            price = dish.price,
                            description = dish.description,
                            imageUrl = dish.imageUrl,
                            category = dish.category
                        )
                    }
                    Log.d(TAG, "Đã cập nhật danh sách món ăn đang bán thành công!")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi lắng nghe danh sách món ăn Flow: ${e.message}")
            }
        }
    }

    private fun createDefaultRestaurant(restaurantId: String) {
        val defaultStore = RestaurantDetail(
            id = restaurantId,
            name = "Cửa hàng của tôi",
            rating = 5.0,
            address = "Chưa cập nhật địa chỉ",
            deliveryTime = "15-20 min",
            distance = "0.5 km",
            description = "Vui lòng cập nhật mô tả quán",
            coverImage = "https://res.cloudinary.com/dhscw17vq/image/upload/v1710000000/sample.jpg"
        )
        firestore.collection("restaurants").document(restaurantId).set(defaultStore)
            .addOnSuccessListener {
                restaurantDetail.value = defaultStore
                Log.d(TAG, "Default restaurant created")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to create default restaurant: ${e.message}")
            }
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

        firestore.collection("restaurants").document(restaurantId)
            .update(updates)
            .addOnSuccessListener {
                restaurantDetail.value = restaurantDetail.value?.copy(
                    name = newName,
                    description = newDescription,
                    address = newAddress
                )
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Update failed: ${e.message}")
            }
    }

    fun updateCoverImage(
        restaurantId: String,
        newCoverUrl: String,
        onSuccess: () -> Unit
    ) {
        if (newCoverUrl.isEmpty()) return

        firestore.collection("restaurants").document(restaurantId)
            .update("coverImage", newCoverUrl)
            .addOnSuccessListener {
                // Cập nhật giao diện tại chỗ lập tức
                restaurantDetail.value = restaurantDetail.value?.copy(coverImage = newCoverUrl)
                onSuccess()
                Log.d("StoreViewModel", "Cover image updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("StoreViewModel", "Failed to update cover image: ${e.message}")
            }
    }

    // 🌟 SỬA HOÀN TOÀN HÀM NÀY THÀNH COROUTINE GIỐNG BÊN THÊM MÓN
    fun uploadCoverToCloudinary(
        imageUri: Uri,
        restaurantId: String,
        uploadPreset: String = "ml_default", // Dùng chung preset "ml_default" đã thành công của bạn
        onSuccess: () -> Unit
    ) {
        if (restaurantId.isEmpty()) return

        // Chạy ngầm trong Coroutine
        viewModelScope.launch {
            isLoading.value = true
            try {
                Log.d(TAG, "Đang upload ảnh bìa lên Cloudinary...")

                // 1. Gọi Repository bốc ảnh quăng lên Cloudinary lấy link String về
                val uploadedUrl = repository.uploadImage(imageUri, uploadPreset)

                Log.d(TAG, "Upload Cloudinary thành công, link ảnh: $uploadedUrl")

                // 2. Tiến hành cập nhật cái link vừa lấy được vào Firestore của nhà hàng
                firestore.collection("restaurants").document(restaurantId)
                    .update("coverImage", uploadedUrl)
                    .addOnSuccessListener {
                        // Cập nhật giao diện tại chỗ lập tức
                        restaurantDetail.value = restaurantDetail.value?.copy(coverImage = uploadedUrl)
                        isLoading.value = false
                        onSuccess()
                        Log.d(TAG, "Đã lưu link ảnh bìa mới vào Firestore!")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Lỗi lưu Firestore: ${e.message}")
                        isLoading.value = false
                    }

            } catch (e: Exception) {
                Log.e(TAG, "Lỗi trong quá trình upload ảnh bìa: ${e.message}")
                isLoading.value = false
            }
        }
    }
}
