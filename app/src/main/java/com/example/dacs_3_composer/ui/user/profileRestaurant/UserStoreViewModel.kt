package com.example.dacs_3_composer.ui.user.profileRestaurant

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs_3_composer.data.model.DishItem
import com.example.dacs_3_composer.data.model.RestaurantDetail
import com.example.dacs_3_composer.data.repository.MenuRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class UserStoreViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val repository = MenuRepository()
    private val TAG = "UserStoreViewModel"

    // 1. Tạo 2 cái túi thần kỳ (State) ban đầu trống rỗng để chứa dữ liệu thật
    var restaurantDetail = mutableStateOf<RestaurantDetail?>(null)
    var dishesList = mutableStateOf<List<DishItem>>(emptyList())
    var isLoading = mutableStateOf(false)

    fun fetchStoreDataForUser(restaurantId: String) {
        if (restaurantId.isEmpty()) return

        isLoading.value = true

        // NHÁNH 1: Lên Firestore lấy thông tin tên quán, địa chỉ, ảnh bìa
        firestore.collection("restaurants").document(restaurantId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    restaurantDetail.value = document.toObject(RestaurantDetail::class.java)
                }
                isLoading.value = false
            }
            .addOnFailureListener { isLoading.value = false }

        // NHÁNH 2: Lắng nghe danh sách món ăn Real-time + LỌC MÓN ĐANG BẬT
        viewModelScope.launch {
            try {
                repository.getDishes(restaurantId).collect { dishList ->
                    // 🌟 Lọc: Chỉ lấy những món có available == true
                    val availableDishes = dishList.filter { dish -> dish.available == true }

                    // Đổ dữ liệu đã lọc sạch vào túi chứa dishesList để UI lấy ra vẽ
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
                Log.e(TAG, "Lỗi lấy món phía user: ${e.message}")
            }
        }
    }
}