package com.example.dacs_3_composer.ui.restaurant.menu

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs_3_composer.data.model.Dish
import com.example.dacs_3_composer.data.repository.MenuRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RestaurantViewModel : ViewModel() {
    private val repository = MenuRepository()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "RestaurantViewModel"

    private val _dishes = MutableStateFlow<List<Dish>>(emptyList())
//    Quản lý trạng thái
    val dishes: StateFlow<List<Dish>> = _dishes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

//    Xác định danh tính nhà hàng
    private val restaurantId: String
        get() = auth.currentUser?.uid ?: ""

    init {
        fetchDishes()
    }

//    Lấy danh sách món ăn
    fun fetchDishes() {
        val uid = restaurantId
        Log.d(TAG, "Fetching dishes for restaurantId: $uid")
        if (uid.isEmpty()) {
            _error.value = "Bạn cần đăng nhập để quản lý thực đơn"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getDishes(uid).collect { dishList ->
                    _dishes.value = dishList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi fetchDishes: ${e.message}")
                _error.value = "Lỗi khi tải thực đơn: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun addDish(
        name: String,
        price: Double,
        category: String,
        description: String,
        imageUri: Uri?,
//        Đây là nơi để biết đến Cloudinary
        uploadPreset: String = "ml_default" 
    ) {
//        Phân biệt id nhà hàng
        val uid = restaurantId
        Log.d(TAG, "Bắt đầu addDish. restaurantId: $uid, name: $name")
        
        if (uid.isEmpty()) {
            _error.value = "Lỗi: Không tìm thấy ID nhà hàng. Vui lòng đăng nhập lại."
            Log.e(TAG, "restaurantId is empty!")
            return
        }
//      Chạy ngầm kiểu như task java
        viewModelScope.launch {
//            Bật load dữ liệu
            _isLoading.value = true
            try {
//                Khai báo Url bởi vì Uri là link ảnh
                var imageUrl = ""
                if (imageUri != null) {
                    Log.d(TAG, "Đang upload ảnh...")
//                    Lấy link ảnh khi đã thêm ảnh ở Cloud dinary
                    imageUrl = repository.uploadImage(imageUri, uploadPreset)
                }
//          Sau khi upload ảnh thì gán từng thành phần vào model có Url khi mình lấy có giá trị trả về ở trên
                val newDish = Dish(
                    name = name,
                    price = price,
                    category = category,
                    description = description,
                    imageUrl = imageUrl,
                    restaurantId = uid,
                    available = true
                )
                
                Log.d(TAG, "Đang lưu món vào Firestore...")
//                Lúc này là thêm vào fire base
                repository.addDish(newDish)
                Log.d(TAG, "Lưu Firestore thành công!")
//                Khi lưu xong thì sẽ tắt vòng xoay dữ liệu
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi trong addDish: ${e.message}")
                _error.value = "Lỗi khi thêm món: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun updateDish(dish: Dish, newImageUri: Uri?, uploadPreset: String = "ml_default") {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                var imageUrl = dish.imageUrl
                if (newImageUri != null) {
                    imageUrl = repository.uploadImage(newImageUri, uploadPreset)
                }

                val updatedDish = dish.copy(imageUrl = imageUrl)
                repository.updateDish(updatedDish)
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi updateDish: ${e.message}")
                _error.value = "Lỗi khi cập nhật: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun deleteDish(dishId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteDish(dishId)
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Lỗi khi xóa món: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // Thêm hàm này vào cuối file (trên clearError) để cập nhật trạng thái bật tắt món
    fun toggleDishAvailability(dish: Dish) {
        viewModelScope.launch {
            // Không cần bật _isLoading.value = true để tránh hiện vòng quay CircularProgressIndicator làm khựng Switch của chủ quán
            try {
                // Đảo ngược trạng thái hiện tại của món ăn (true -> false hoặc false -> true)
                val updatedDish = dish.copy(available = !dish.available)

                Log.d(TAG, "Đang đổi trạng thái món ${dish.name} sang: ${updatedDish.available}")

                // Gọi repository cập nhật thẳng lên Firestore
                repository.updateDish(updatedDish)
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi toggleDishAvailability: ${e.message}")
                _error.value = "Không thể cập nhật trạng thái món: ${e.message}"
            }
        }
    }
    fun clearError() {
        _error.value = null
    }
}
