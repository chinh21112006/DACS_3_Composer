package com.example.dacs_3_composer.ui.user.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs_3_composer.data.model.Dish
import com.example.dacs_3_composer.data.model.RestaurantDetail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _restaurantsList = MutableStateFlow<List<RestaurantDetail>>(emptyList())
    val restaurantsList: StateFlow<List<RestaurantDetail>> = _restaurantsList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _deliciousDishes = MutableStateFlow<List<Dish>>(emptyList())
    val deliciousDishes: StateFlow<List<Dish>> = _deliciousDishes.asStateFlow()

    init {
        loadUserData() // 🌟 Cho chạy ngay từ đầu để thông tin cá nhân nạp nhanh hơn
        fetchDeliciousDishes()
        fetchAllRestaurants()
    }

    // --- QUẢN LÝ THÔNG TIN NGƯỜI DÙNG ---
    var userName by mutableStateOf("Khách")
        private set

    var userAddress by mutableStateOf("")
        private set

    // 🌟 THÊM BIẾN NÀY ĐỂ LƯU LINK AVATAR NGƯỜI DÙNG
    var userImageUrl by mutableStateOf("")
        private set

    fun loadUserData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    userName = document.getString("name") ?: "Khách"
                    userAddress = document.getString("address") ?: ""

                    // 🌟 SỬA TẠI ĐÂY: Thay "imageUrl" bằng "avatarUrl" để khớp dữ liệu lưu trữ
                    userImageUrl = document.getString("avatarUrl") ?: ""
                }
            }
            .addOnFailureListener { exception ->
                Log.e("HomeViewModel", "Lỗi lấy dữ liệu user: ", exception)
            }
    }

    private fun fetchDeliciousDishes() {
        viewModelScope.launch {
            firestore.collection("dishes")
                .whereEqualTo("available", true)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("HomeViewModel", "Lỗi addSnapshotListener: ", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val dishList = mutableListOf<Dish>()
                        for (document in snapshot) {
                            try {
                                // Lấy dữ liệu thủ công cực kỳ an toàn
                                val dish = Dish(
                                    id = document.id,
                                    name = document.getString("name") ?: "",
                                    // Chấp nhận cả trường hợp trên Firebase là số nguyên hay số thực
                                    price = document.getDouble("price") ?: document.getLong("price")?.toDouble() ?: 0.0,
                                    imageUrl = document.getString("imageUrl") ?: "",
                                    category = document.getString("category") ?: "Tất cả",
                                    description = document.getString("description") ?: "",
                                    available = document.getBoolean("available") ?: true,
                                    restaurantId = document.getString("restaurantId") ?: ""
                                )
                                dishList.add(dish)
                            } catch (e: Exception) {
                                Log.e("HomeViewModel", "Lỗi convert món ăn ID ${document.id}: ", e)
                            }
                        }
                        _deliciousDishes.value = dishList
                    }
                }
        }
    }

    private fun fetchAllRestaurants() {
        _isLoading.value = true

        firestore.collection("restaurants")
            .get()
            .addOnSuccessListener { result ->
                val list = mutableListOf<RestaurantDetail>()
                for (document in result) {
                    try {
                        // Lấy dữ liệu thủ công để tránh xung đột kiểu dữ liệu Double/Long
                        val restaurant = RestaurantDetail(
                            id = document.id,
                            name = document.getString("name") ?: "",
                            rating = document.getDouble("rating") ?: document.getLong("rating")?.toDouble() ?: 0.0,
                            address = document.getString("address") ?: "",
                            deliveryTime = document.getString("deliveryTime") ?: "0 min",
                            distance = document.getString("distance") ?: "0 km",
                            description = document.getString("description") ?: "",
                            coverImage = document.getString("coverImage") ?: ""
                        )
                        list.add(restaurant)
                    } catch (e: Exception) {
                        Log.e("HomeViewModel", "Lỗi convert nhà hàng ID ${document.id}: ", e)
                    }
                }
                _restaurantsList.value = list
                _isLoading.value = false
            }
            .addOnFailureListener { exception ->
                Log.e("HomeViewModel", "Lỗi lấy danh sách quán: ", exception)
                _isLoading.value = false
            }
    }
}