package com.example.dacs_3_composer.data.backend

import android.util.Log
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.cio.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import vn.payos.PayOS
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class CreatePaymentRequestBE(
    val orderId: String,
    val amount: Long,
    val description: String
)

object PayOSServer {
    private var server: ApplicationEngine? = null

    private val payOS = PayOS(
        "61c355bd-d376-4e4f-bf38-f36401f7ba3e",
        "ba80dd6f-6e65-4d56-a0f7-c4280547d00c",
        "84be0dc21b548239b6666de1471b8eb46ff0059018a36d2649e686e33fab5b12"
    )

    fun start() {
        try {
            server?.stop(0, 0)
            server = embeddedServer(CIO, port = 8888, host = "127.0.0.1") {
                install(ContentNegotiation) {
                    gson {
                        setPrettyPrinting()
                        serializeNulls()
                    }
                }
                routing {
                    get("/") {
                        call.respondText("PAYOS SERVER LIVE - V2.0.1", contentType = ContentType.Text.Plain)
                    }

                    post("/api/payment/create-link") {
                        try {
                            val request = call.receive<CreatePaymentRequestBE>()
                            Log.d("PayOS_V2", ">>> RECEIVE: ${request.orderId} - ${request.amount} VND")

                            val orderCode = System.currentTimeMillis() / 1000

                            val paymentData = CreatePaymentLinkRequest.builder()
                                .orderCode(orderCode)
                                .amount(request.amount)
                                .description("Thanh toan")
                                // ✅ Thêm status=PAID để App nhận diện khi quay lại
                                .returnUrl("dacs3://payment_callback?orderId=${request.orderId}&status=PAID")
                                .cancelUrl("dacs3://payment_callback?orderId=${request.orderId}&status=CANCELLED")
                                .build()

                            val response = withContext(Dispatchers.IO) {
                                payOS.paymentRequests().create(paymentData)
                            }

                            Log.d("PayOS_V2", "✅ Link thành công: ${response.checkoutUrl}")

                            call.respond(HttpStatusCode.OK, mapOf(
                                "checkoutUrl" to response.checkoutUrl,
                                "paymentLinkId" to response.paymentLinkId,
                                "qrCode" to response.qrCode
                            ))
                        } catch (e: Exception) {
                            Log.e("PayOS_V2", "❌ Lỗi: ${e.message}")
                            call.respond(HttpStatusCode.InternalServerError, mapOf(
                                "error" to (e.message ?: "SDK Error")
                            ))
                        }
                    }
                }
            }
            server?.start(wait = false)
            Log.d("PayOS_V2", "🚀 SERVER PAYOS V2 READY ON 8888")
        } catch (e: Exception) {
            Log.e("PayOS_V2", "❌ Không thể khởi động Server: ${e.message}")
        }
    }
}
