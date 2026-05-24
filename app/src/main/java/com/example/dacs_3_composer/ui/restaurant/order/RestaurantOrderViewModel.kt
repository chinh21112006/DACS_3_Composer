package com.example.dacs_3_composer.ui.restaurant.order

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.dacs_3_composer.data.model.Order
import com.example.dacs_3_composer.data.model.OrderStatus
import com.example.dacs_3_composer.data.repository.ActivityLogRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RestaurantOrderViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val activityLogRepository = ActivityLogRepository()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    var isLoading by mutableStateOf(false)
        private set

    private val currentRestaurantId: String
        get() = auth.currentUser?.uid ?: ""

    init {
        listenToRestaurantOrders()
    }

    private fun listenToRestaurantOrders() {
        if (currentRestaurantId.isBlank()) return
        isLoading = true
        firestore.collection("orders")
            .whereEqualTo("restaurantId", currentRestaurantId)
            .addSnapshotListener { snapshot, error ->
                isLoading = false
                if (error != null) {
                    Log.e("RestaurantOrderVM", "Lỗi lắng nghe đơn hàng", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val orderList = snapshot.toObjects(Order::class.java)
                    _orders.value = orderList.sortedByDescending { it.id }
                }
            }
    }

    fun updateOrderStatus(orderId: String, nextStatus: OrderStatus) {
        if (orderId.isBlank()) return

        firestore.collection("orders").document(orderId)
            .update("status", nextStatus.name)
            .addOnSuccessListener {
                Log.d("RestaurantOrderVM", "Đơn hàng $orderId đã chuyển sang: ${nextStatus.name}")
                
                // 🎯 LOG ACTIVITY
                val statusMsg = when(nextStatus) {
                    OrderStatus.PROCESSING -> "Đã xác nhận đơn hàng"
                    OrderStatus.SHIPPING -> "Đã bàn giao cho Shipper"
                    OrderStatus.COMPLETED -> "Đã hoàn tất đơn hàng"
                    OrderStatus.CANCELLED -> "Đã hủy đơn hàng"
                    else -> "Cập nhật trạng thái đơn hàng"
                }
                
                activityLogRepository.logActivity(
                    restaurantId = currentRestaurantId,
                    type = "order",
                    title = statusMsg,
                    description = "Đơn hàng #$orderId",
                    extraInfo = mapOf("orderId" to orderId)
                )
            }
            .addOnFailureListener { e ->
                Log.e("RestaurantOrderVM", "Lỗi cập nhật đơn $orderId", e)
            }
    }
}
