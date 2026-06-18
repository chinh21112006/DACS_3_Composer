package com.example.dacs_3_composer.data.repository

import com.example.dacs_3_composer.data.remote.CreatePaymentRequest
import com.example.dacs_3_composer.data.remote.PaymentApiService
import com.example.dacs_3_composer.data.remote.PayOSResponse

class PaymentRepository(private val apiService: PaymentApiService) {
    suspend fun createPaymentLink(orderId: String, amount: Double, description: String): PayOSResponse {
        // Chuyển đổi amount từ Double sang Long để khớp với CreatePaymentRequest
        return apiService.createPaymentLink(CreatePaymentRequest(orderId, amount.toLong(), description))
    }

    suspend fun getPaymentStatus(orderId: String): String {
        val response = apiService.getPaymentStatus(orderId)
        return response["status"] ?: "PENDING"
    }
}
