package com.example.dacs_3_composer.ui.user.cart

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.dacs_3_composer.data.model.DishItem
import com.example.dacs_3_composer.data.model.Order
import com.example.dacs_3_composer.data.model.OrderItem
import com.example.dacs_3_composer.data.model.OrderStatus
import com.example.dacs_3_composer.data.model.Promotion
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CartItem(
    val dish: DishItem,
    val quantity: Int
)

class CartViewModel : ViewModel() {

    var cartItems = mutableStateListOf<CartItem>()
        private set

    var currentRestaurantId: String = ""
    var currentRestaurantName: String = ""

    var availablePromotions = mutableStateListOf<Promotion>()
        private set

    fun fetchPromotionsFromFirebase() {
        val db = FirebaseFirestore.getInstance()
        db.collection("promotions")
            .whereEqualTo("status", "active")
            .get()
            .addOnSuccessListener { documents ->
                availablePromotions.clear()
                val ngayGioHienTai = Timestamp.now()

                for (document in documents) {
                    try {
                        val id = document.id
                        val code = document.getString("code") ?: ""
                        val type = document.getString("type") ?: "percentage"
                        val description = document.getString("description") ?: ""
                        val title = document.getString("title") ?: ""
                        val status = document.getString("status") ?: "active"

                        val value = document.getDouble("value") ?: 0.0
                        val maxDiscount = document.getDouble("maxDiscount") ?: 0.0
                        val minOrderValue = document.getDouble("minOrderValue") ?: 0.0

                        val usageCount = document.getLong("usageCount") ?: 0L
                        val usageLimit = document.getLong("usageLimit") ?: 0L

                        val startDate = document.getTimestamp("startDate")
                        val endDate = document.getTimestamp("endDate")

                        val promo = Promotion(
                            id = id, code = code, type = type, value = value,
                            maxDiscount = maxDiscount, minOrderValue = minOrderValue,
                            usageCount = usageCount, usageLimit = usageLimit,
                            startDate = startDate, endDate = endDate,
                            description = description, title = title, status = status
                        )

                        val đãBắtĐầu = promo.startDate?.let { ngayGioHienTai >= it } ?: true
                        val chưaHếtHạn = promo.endDate?.let { ngayGioHienTai <= it } ?: true
                        val cònLượtDùng = if (promo.usageLimit > 0) promo.usageCount < promo.usageLimit else true

                        if (đãBắtĐầu && chưaHếtHạn && cònLượtDùng) {
                            availablePromotions.add(promo)
                        }
                    } catch (e: Exception) {
                        Log.e("CartViewModel", "Lỗi parse tài liệu ${document.id}: ${e.message}")
                    }
                }
            }
    }

    fun placeOrder(
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        customerLat: Double,
        customerLng: Double,
        restaurantId: String,
        restaurantName: String,
        shippingFee: Double,
        totalDishPrice: Double,
        finalTotal: Double,
        appliedPromotionId: String?,
        appliedPromotionTitle: String,
        promotionDiscount: Double,
        paymentMethod: String, // ✅ Nhận thêm phương thức thanh toán
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            onFailure("Người dùng chưa đăng nhập!")
            return
        }

        if (cartItems.isEmpty()) {
            onFailure("Giỏ hàng đang trống!")
            return
        }

        val finalRestaurantId = if (restaurantId.isNotBlank() && restaurantId != "RES_DEFAULT") {
            restaurantId
        } else if (this.currentRestaurantId.isNotBlank()) {
            this.currentRestaurantId
        } else {
            cartItems.firstOrNull()?.dish?.restaurantId ?: ""
        }

        val db = FirebaseFirestore.getInstance()
        val orderId = db.collection("orders").document().id

        val orderItemsList = cartItems.map { item ->
            OrderItem(
                dishId = item.dish.id,
                name = item.dish.name,
                quantity = item.quantity,
                price = item.dish.price
            )
        }

        val currentTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        // Xác định trạng thái ban đầu dựa trên phương thức thanh toán
        val initialStatus = if (paymentMethod == "ONLINE") {
            OrderStatus.PENDING_PAYMENT.name
        } else {
            OrderStatus.WAITING_RESTAURANT.name // COD thì vào luôn hàng chờ nhà hàng
        }

        val newOrder = Order(
            id = orderId,
            time = currentTime,
            status = initialStatus,
            paymentMethod = paymentMethod, // ✅ Lưu phương thức thanh toán
            totalDishPrice = totalDishPrice,
            totalPrice = finalTotal,
            shippingFee = shippingFee,
            appliedPromotionId = appliedPromotionId,
            appliedPromotionTitle = appliedPromotionTitle,
            promotionDiscount = promotionDiscount,
            userId = uid,
            customerName = customerName,
            customerPhone = customerPhone,
            customerAddress = customerAddress,
            customerLat = customerLat,
            customerLng = customerLng,
            restaurantId = finalRestaurantId,
            restaurantName = restaurantName.ifBlank { this.currentRestaurantName }.ifBlank { "Nhà hàng đối tác" },
            items = orderItemsList
        )

        db.collection("orders").document(orderId)
            .set(newOrder)
            .addOnSuccessListener {
                if (!appliedPromotionId.isNullOrBlank()) {
                    db.collection("promotions").document(appliedPromotionId)
                        .update("usageCount", FieldValue.increment(1))
                }
                onSuccess(orderId)
            }
            .addOnFailureListener { exception ->
                onFailure(exception.message ?: "Lỗi kết nối Firebase")
            }
    }

    fun addToCart(dish: DishItem) {
        if (dish.restaurantId.isNotBlank()) {
            this.currentRestaurantId = dish.restaurantId
        }
        val existingItem = cartItems.find { it.dish.id == dish.id }
        if (existingItem != null) {
            val index = cartItems.indexOf(existingItem)
            cartItems[index] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            cartItems.add(CartItem(dish = dish, quantity = 1))
        }
    }

    fun updateDeliveryInfo(newName: String, newPhone: String, newAddress: String, newAddressDetail: String, lat: Double, lng: Double) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val updates = mapOf(
            "name" to newName,
            "phone" to newPhone,
            "address" to newAddress,
            "addressDetail" to newAddressDetail,
            "latitude" to lat,
            "longitude" to lng
        )

        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                db.collection("users").document(uid)
                    .update("savedAddresses", FieldValue.arrayUnion(updates))
            }
    }

    fun minusFromCart(dish: DishItem) {
        val existingItem = cartItems.find { it.dish.id == dish.id } ?: return
        val index = cartItems.indexOf(existingItem)

        if (existingItem.quantity > 1) {
            cartItems[index] = existingItem.copy(quantity = existingItem.quantity - 1)
        } else {
            cartItems.removeAt(index)
            if (cartItems.isEmpty()) {
                currentRestaurantId = ""
                currentRestaurantName = ""
            }
        }
    }

    fun getTotalPrice(): Double = cartItems.sumOf { it.dish.price * it.quantity }

    fun clearCart() {
        cartItems.clear()
        currentRestaurantId = ""
        currentRestaurantName = ""
    }
}
