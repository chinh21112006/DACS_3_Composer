package com.example.dacs_3_composer.ui.user.order

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.example.dacs_3_composer.data.model.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OrderViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _ongoingOrders = MutableStateFlow<List<Order>>(emptyList())
    val ongoingOrders: StateFlow<List<Order>> = _ongoingOrders.asStateFlow()

    private val _historyOrders = MutableStateFlow<List<Order>>(emptyList())
    val historyOrders: StateFlow<List<Order>> = _historyOrders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 🌟 BỘ NHỚ ĐỆM: Lưu trữ [restaurantId -> Link ảnh mạng của quán]
    val restaurantImages = mutableStateMapOf<String, String>()

    // 🌟 THÊM MỚI: Biến lưu trữ thông tin đơn hàng đang được theo dõi chi tiết
    private val _currentTrackingOrder = MutableStateFlow<Order?>(null)
    val currentTrackingOrder: StateFlow<Order?> = _currentTrackingOrder.asStateFlow()

    init {
        observeUserOrders()
    }

    fun observeUserOrders() {
        val uid = auth.currentUser?.uid ?: return
        _isLoading.value = true

        firestore.collection("orders")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false
                if (error != null) {
                    Log.e("OrderViewModel", "Lỗi lắng nghe đơn hàng: ", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val allOrders = snapshot.toObjects(Order::class.java)

                    // 1. Nhóm chỉ chờ quán bấm xác nhận
                    val pending = allOrders.filter { it.status == "PENDING" }.sortedByDescending { it.time }

                    // 2. Nhóm đang làm món hoặc đang đi giao trên đường
                    val ongoing = allOrders.filter {
                        it.status == "PROCESSING" || it.status == "SHIPPING"
                    }.sortedByDescending { it.time }

                    // 3. Nhóm lịch sử hoàn tất
                    val history = allOrders.filter {
                        it.status == "COMPLETED" || it.status == "CANCELLED"
                    }.sortedByDescending { it.time }

                    // Cập nhật StateFlow tương ứng
                    _ongoingOrders.value = ongoing // Lưu trữ đơn "Đang đến"
                    _historyOrders.value = history

                    // Tạo thêm một StateFlow hoặc tận dụng State có sẵn (xem Bước 3)
                }
            }
    }

    // 🌟 THÊM MỚI: Hàm lắng nghe Realtime cho màn hình Theo dõi đơn hàng
    fun observeOrderDetails(orderId: String) {
        if (orderId.isBlank()) return

        firestore.collection("orders").document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("OrderViewModel", "Lỗi lắng nghe chi tiết đơn hàng $orderId: ", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val order = snapshot.toObject(Order::class.java)
                    _currentTrackingOrder.value = order
                }
            }
    }

    private fun fetchRestaurantImage(restaurantId: String) {
        firestore.collection("restaurants").document(restaurantId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val imageUrl = document.getString("coverImage") ?: ""
                    if (imageUrl.isNotBlank()) {
                        restaurantImages[restaurantId] = imageUrl
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("OrderViewModel", "Lỗi lấy ảnh quán $restaurantId", e)
            }
    }
}