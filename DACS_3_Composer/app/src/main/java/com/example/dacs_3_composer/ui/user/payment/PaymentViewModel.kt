package com.example.dacs_3_composer.ui.user.payment

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs_3_composer.data.model.OrderStatus
import com.example.dacs_3_composer.data.repository.PaymentRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class PaymentViewModel(private val repository: PaymentRepository) : ViewModel() {
    var checkoutUrl by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var isPaymentSuccess by mutableStateOf(false)

    private val db = FirebaseFirestore.getInstance()

    fun createPayment(orderId: String, amount: Double, description: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = repository.createPaymentLink(orderId, amount, description)
                checkoutUrl = response.checkoutUrl
                // Bắt đầu lắng nghe trạng thái đơn hàng ngay khi tạo link thành công
                observeOrderStatus(orderId)
            } catch (e: Exception) {
                errorMessage = "Lỗi kết nối thanh toán: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun observeOrderStatus(orderId: String) {
        db.collection("orders").document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    val status = snapshot.getString("status")
                    // Nếu status chuyển từ PENDING_PAYMENT sang WAITING_RESTAURANT nghĩa là Webhook đã xử lý xong
                    if (status == OrderStatus.WAITING_RESTAURANT.name) {
                        isPaymentSuccess = true
                    }
                }
            }
    }
    
    fun resetState() {
        checkoutUrl = null
        errorMessage = null
        isLoading = false
        isPaymentSuccess = false
    }
}
