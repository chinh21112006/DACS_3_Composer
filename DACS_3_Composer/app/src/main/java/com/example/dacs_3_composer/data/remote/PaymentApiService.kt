package com.example.dacs_3_composer.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path

data class CreatePaymentRequest(
    val orderId: String,
    val amount: Long, // ✅ Đổi sang Long để đồng bộ với cổng thanh toán
    val description: String
)

data class PayOSResponse(
    val checkoutUrl: String,
    val paymentLinkId: String,
    val qrCode: String
)

interface PaymentApiService {
    @POST("api/payment/create-link")
    suspend fun createPaymentLink(@Body request: CreatePaymentRequest): PayOSResponse

    @GET("api/payment/status/{orderId}")
    suspend fun getPaymentStatus(@Path("orderId") orderId: String): Map<String, String>
}
