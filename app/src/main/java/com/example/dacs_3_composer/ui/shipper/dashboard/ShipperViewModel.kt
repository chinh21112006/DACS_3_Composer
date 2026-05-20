package com.example.dacs_3_composer.ui.shipper.dashboard

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs_3_composer.data.model.Order
import com.example.dacs_3_composer.data.model.OrderStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShipperViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _isReadyToWork = MutableStateFlow(false)
    val isReadyToWork: StateFlow<Boolean> = _isReadyToWork.asStateFlow()

    // Đơn hàng đang chờ điều phối (Trạng thái ACCEPTED nhưng chưa có shipperId)
    private val _availableOrders = MutableStateFlow<List<Order>>(emptyList())
    val availableOrders: StateFlow<List<Order>> = _availableOrders.asStateFlow()

    // Đơn hàng mà chính Shipper này đã nhấn nhận (Bao gồm cả lúc đang đến quán lấy và đang đi giao)
    private val _activeDeliveryOrder = MutableStateFlow<Order?>(null)
    val activeDeliveryOrder: StateFlow<Order?> = _activeDeliveryOrder.asStateFlow()

    private var availableOrdersListener: ListenerRegistration? = null
    private var activeOrderListener: ListenerRegistration? = null

    var isLoading by mutableStateOf(false)
        private set

    private val currentShipperId: String
        get() = auth.currentUser?.uid ?: ""

    init {
        observeShipperStatus()
        listenToActiveDelivery()
    }

    private fun observeShipperStatus() {
        val uid = currentShipperId.ifBlank { return }
        firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null && snapshot.exists()) {
                    val isAvailable = snapshot.getBoolean("isAvailable") ?: false
                    _isReadyToWork.value = isAvailable

                    if (isAvailable) {
                        startListeningAvailableOrders()
                    } else {
                        stopListeningAvailableOrders()
                    }
                }
            }
    }

    fun toggleWorkStatus(isReady: Boolean) {
        val uid = currentShipperId.ifBlank { return }
        viewModelScope.launch {
            firestore.collection("users").document(uid).update("isAvailable", isReady)
        }
    }

    // 2. LẮNG NGHE ĐƠN HÀNG CHỜ LẤY (Quán nấu xong chuyển sang ACCEPTED)
    private fun startListeningAvailableOrders() {
        if (availableOrdersListener != null) return

        availableOrdersListener = firestore.collection("orders")
            .whereEqualTo("status", OrderStatus.ACCEPTED.name) // ✅ ĐÃ SỬA: Lọc trạng thái ACCEPTED
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    val list = mutableListOf<Order>()
                    for (document in snapshot.documents) {
                        val order = document.toObject(Order::class.java)
                        if (order != null && order.shipperId.isBlank()) {
                            order.id = document.id
                            list.add(order)
                        }
                    }
                    _availableOrders.value = list
                }
            }
    }

    // 3. LẮNG NGHE ĐƠN HÀNG MÀ TÀI XẾ NÀY ĐANG NHẬN XỬ LÝ
    private fun listenToActiveDelivery() {
        val uid = currentShipperId.ifBlank { return }
        activeOrderListener = firestore.collection("orders")
            .whereEqualTo("shipperId", uid)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    // ✅ ĐÃ SỬA: Lấy đơn thuộc tài xế này đang ở trạng thái chuẩn bị giao (ACCEPTED) hoặc đang giao (SHIPPING)
                    val activeOrder = snapshot.documents
                        .mapNotNull { doc -> doc.toObject(Order::class.java)?.apply { id = doc.id } }
                        .firstOrNull { it.status == "ACCEPTED" || it.status == "SHIPPING" }

                    _activeDeliveryOrder.value = activeOrder
                }
            }
    }

    // ⚡ SHIPPER BẤM NHẬN ĐƠN
    fun acceptOrder(orderId: String) {
        val uid = currentShipperId.ifBlank { return }
        firestore.collection("orders").document(orderId)
            .update("shipperId", uid)
            .addOnSuccessListener {
                Log.d("ShipperVM", "Nhận đơn $orderId thành công!")
            }
    }

    // ⚡ SHIPPER XÁC NHẬN ĐÃ GIAO XONG
    fun completeOrder(orderId: String) {
        firestore.collection("orders").document(orderId)
            .update("status", OrderStatus.COMPLETED.name)
            .addOnSuccessListener {
                Log.d("ShipperVM", "Hoàn thành đơn hàng $orderId")
                _activeDeliveryOrder.value = null
            }
    }

    private fun stopListeningAvailableOrders() {
        availableOrdersListener?.remove()
        availableOrdersListener = null
        _availableOrders.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        stopListeningAvailableOrders()
        activeOrderListener?.remove()
    }
}