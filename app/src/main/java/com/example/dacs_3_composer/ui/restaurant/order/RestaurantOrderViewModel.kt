package com.example.dacs_3_composer.ui.restaurant.order

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.dacs_3_composer.data.model.Order
import com.example.dacs_3_composer.data.model.OrderStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RestaurantOrderViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    var isLoading by mutableStateOf(false)
        private set

    // Lấy ID của nhà hàng đang đăng nhập hệ thống hiện tại
    private val currentRestaurantId: String
        get() = auth.currentUser?.uid ?: "uid_quan_cua_ban" // Dự phòng ID mẫu nếu chưa đăng nhập

    init {
        listenToRestaurantOrders()
    }

    // 🔄 LẮNG NGHE ĐƠN HÀNG THỜI GIAN THỰC TỪ FIREBASE TỪNG QUÁN
    private fun listenToRestaurantOrders() {
        isLoading = true
        firestore.collection("orders")
            .whereEqualTo("restaurantId", currentRestaurantId)
            .addSnapshotListener { snapshot, error ->
                isLoading = false
                if (error != null) {
                    Log.e("RestaurantOrderVM", "Lỗi lắng nghe đơn hàng của nhà hàng", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val orderList = snapshot.toObjects(Order::class.java)                    // Sắp xếp đơn hàng mới nhất lên trên đầu dựa theo ID hoặc trường thời gian
                    _orders.value = orderList.sortedByDescending { it.id }
                }
            }
    }

    // ⚡ CẬP NHẬT TRẠNG THÁI ĐƠN HÀNG (Xác nhận đơn / Hoàn tất món)
    fun updateOrderStatus(orderId: String, nextStatus: OrderStatus) {
        if (orderId.isBlank()) return

        firestore.collection("orders").document(orderId)
            .update("status", nextStatus.name)
            .addOnSuccessListener {
                Log.d("RestaurantOrderVM", "Đơn hàng $orderId đã chuyển sang: ${nextStatus.name}")
            }
            .addOnFailureListener { e ->
                Log.e("RestaurantOrderVM", "Lỗi cập nhật trạng thái đơn $orderId", e)
            }
    }
}