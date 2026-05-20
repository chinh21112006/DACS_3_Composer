package com.example.dacs_3_composer.ui.user.cart

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.dacs_3_composer.data.model.DishItem
import com.example.dacs_3_composer.data.model.Order
import com.example.dacs_3_composer.data.model.OrderItem
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

    fun placeOrder(
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        restaurantId: String,
        restaurantName: String,
        shippingFee: Double,
        totalDishPrice: Double,
        finalTotal: Double,
        onSuccess: () -> Unit,
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

        // 🎯 ĐÃ SỬA: Map chuẩn biến theo đúng Model kết nối với Firebase của bạn
        val newOrder = Order(
            id = orderId,
            time = currentTime,
            status = "PENDING",
            totalDishPrice = totalDishPrice, // Gán giá gốc món ăn
            totalPrice = finalTotal,         // Gán tổng tiền thanh toán sau giảm giá

            // Khách hàng
            userId = uid,
            customerName = customerName,
            customerPhone = customerPhone,
            customerAddress = customerAddress,

            // Nhà hàng
            restaurantId = restaurantId,
            restaurantName = restaurantName,

            // Shipper
            shipperId = "",

            // Danh sách món ăn
            items = orderItemsList
        )

        db.collection("orders").document(orderId)
            .set(newOrder)
            .addOnSuccessListener {
                clearCart()
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e("CartViewModel", "Lỗi đặt đơn hàng: ${exception.message}")
                onFailure(exception.message ?: "Lỗi kết nối Firebase")
            }
    }

    fun addToCart(dish: DishItem) {
        val existingItem = cartItems.find { it.dish.id == dish.id }

        if (existingItem != null) {
            val index = cartItems.indexOf(existingItem)
            cartItems[index] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            cartItems.add(CartItem(dish = dish, quantity = 1))
        }
    }

    fun updateDeliveryInfo(newName: String, newPhone: String, newAddress: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val updates = mapOf(
            "name" to newName,
            "phone" to newPhone,
            "address" to newAddress
        )

        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                val addressMap = mapOf(
                    "name" to newName,
                    "phone" to newPhone,
                    "address" to newAddress
                )
                db.collection("users").document(uid)
                    .update("savedAddresses", FieldValue.arrayUnion(addressMap))
            }
    }

    fun minusFromCart(dish: DishItem) {
        val existingItem = cartItems.find { it.dish.id == dish.id } ?: return
        val index = cartItems.indexOf(existingItem)

        if (existingItem.quantity > 1) {
            cartItems[index] = existingItem.copy(quantity = existingItem.quantity - 1)
        } else {
            cartItems.removeAt(index)
        }
    }

    fun getTotalPrice(): Double {
        return cartItems.sumOf { it.dish.price * it.quantity }
    }

    fun clearCart() {
        cartItems.clear()
    }
}