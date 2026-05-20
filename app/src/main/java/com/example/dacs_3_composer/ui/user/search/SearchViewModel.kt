package com.example.dacs_3_composer.ui.user.search

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.dacs_3_composer.data.model.RestaurantDetail
import com.example.dacs_3_composer.data.model.Dish
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

// Class bọc món ăn kết hợp tên nhà hàng để hiển thị lên UI
data class SearchDishWrapper(
    val dish: Dish,
    val restaurantName: String
)

class SearchViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _restaurantResults = MutableStateFlow<List<RestaurantDetail>>(emptyList())
    val restaurantResults: StateFlow<List<RestaurantDetail>> = _restaurantResults.asStateFlow()

    private val _dishResults = MutableStateFlow<List<SearchDishWrapper>>(emptyList())
    val dishResults: StateFlow<List<SearchDishWrapper>> = _dishResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun searchRestaurantsAndDishes(query: String) {
        if (query.isBlank()) {
            _restaurantResults.value = emptyList()
            _dishResults.value = emptyList()
            return
        }

        _isLoading.value = true
        val lowercaseQuery = query.lowercase(Locale.getDefault()).trim()

        // Lấy dữ liệu từ Collection nhà hàng
        firestore.collection("restaurants").get()
            .addOnSuccessListener { restaurantSnapshot ->
                if (restaurantSnapshot != null) {
                    val allRestaurants = restaurantSnapshot.toObjects(RestaurantDetail::class.java)

                    // 1. Lọc danh sách nhà hàng phù hợp
                    val filteredRestaurants = allRestaurants.filter { restaurant ->
                        restaurant.name.lowercase(Locale.getDefault()).contains(lowercaseQuery) ||
                                restaurant.description.lowercase(Locale.getDefault()).contains(lowercaseQuery)
                    }
                    _restaurantResults.value = filteredRestaurants

                    // 2. TÌM KIẾM MÓN ĂN
                    // LỰA CHỌN A: Nếu cấu trúc DB của bạn lưu danh sách `dishes` nằm TRONG Document của nhà hàng
                    val dishesFromRestaurants = mutableListOf<SearchDishWrapper>()
                    allRestaurants.forEach { restaurant ->
                        // Giả sử model RestaurantDetail của bạn có trường: val dishes: List<Dish> = emptyList()
                        // restaurant.dishes.forEach { dish -> ... }
                    }

                    // LỰA CHỌN B: Nếu cấu trúc DB của bạn lưu bộ món ăn ở Collection "dishes" riêng biệt (Khuyên dùng)
                    firestore.collection("dishes").get()
                        .addOnSuccessListener { dishSnapshot ->
                            _isLoading.value = false
                            if (dishSnapshot != null) {
                                val allDishes = dishSnapshot.toObjects(Dish::class.java)
                                val filteredDishes = mutableListOf<SearchDishWrapper>()

                                allDishes.forEach { dish ->
                                    if (dish.name.lowercase(Locale.getDefault()).contains(lowercaseQuery) ||
                                        dish.description.lowercase(Locale.getDefault()).contains(lowercaseQuery)) {

                                        // Tìm tên nhà hàng sở hữu món ăn này dựa vào restaurantId tương ứng
                                        val parentRestaurant = allRestaurants.find { it.id == dish.restaurantId }
                                        val rName = parentRestaurant?.name ?: "Cửa hàng đối tác"

                                        filteredDishes.add(SearchDishWrapper(dish = dish, restaurantName = rName))
                                    }
                                }
                                _dishResults.value = filteredDishes
                            }
                        }
                        .addOnFailureListener { e ->
                            _isLoading.value = false
                            Log.e("SearchViewModel", "Lỗi tải danh sách món ăn", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                Log.e("SearchViewModel", "Lỗi truy vấn tìm kiếm tổng hợp", e)
            }
    }
}