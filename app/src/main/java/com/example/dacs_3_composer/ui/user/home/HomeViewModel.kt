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
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val dishList = snapshot.toObjects(Dish::class.java)
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
                    val restaurant = document.toObject(RestaurantDetail::class.java)
                    restaurant.id = document.id
                    list.add(restaurant)
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