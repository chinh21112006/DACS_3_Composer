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
        // 🌟 THÊM 2 THAM SỐ TOẠ ĐỘ VÀO HÀM:
        customerLat: Double,
        customerLng: Double,

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

        val finalRestaurantId = if (restaurantId.isNotBlank() && restaurantId != "RES_DEFAULT") {
            restaurantId
        } else if (this.currentRestaurantId.isNotBlank()) {
            this.currentRestaurantId
        } else {
            cartItems.firstOrNull()?.dish?.restaurantId ?: ""
        }

        if (finalRestaurantId.isBlank()) {
            Log.e("CartViewModel", "Đặt hàng thất bại: Không tìm thấy ID nhà hàng từ giỏ hàng!")
            onFailure("Lỗi hệ thống: Không xác định được ID nhà hàng!")
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

        // 🌟 KHỞI TẠO OBJECT ORDER THEO KHUÔN MỚI CỦA BẠN:
        val newOrder = Order(
            id = orderId,
            time = currentTime,
            status = "PENDING",
            totalDishPrice = totalDishPrice,
            totalPrice = finalTotal,
            shippingFee = shippingFee,

            userId = uid,
            customerName = customerName,
            customerPhone = customerPhone,
            customerAddress = customerAddress,

            // 🌟 GÁN GIÁ TRỊ TOẠ ĐỘ THỰC TẾ VÀO ĐÂY:
            customerLat = customerLat,
            customerLng = customerLng,

            restaurantId = finalRestaurantId,
            restaurantName = restaurantName.ifBlank { this.currentRestaurantName }.ifBlank { "Nhà hàng đối tác" },

            // Tạm thời để null vì chưa xử lý phần map nhà hàng, đúng như thiết kế file Model của bạn
            restaurantLat = null,
            restaurantLng = null,

            shipperId = "",
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

    // 🎯 ĐÃ SỬA: Tự động cập nhật thông tin nhà hàng dựa theo món ăn được thêm vào giỏ
    fun addToCart(dish: DishItem) {
        // 🎯 LUÔN LUÔN CẬP NHẬT: Đảm bảo lấy ID nhà hàng từ món ăn mới nhất vừa được thêm
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

        Log.d("CartViewModel", "Đã thêm món ${dish.name}. ID Nhà hàng hiện tại trong giỏ: ${this.currentRestaurantId}")
    }


    fun updateDeliveryInfo(
        newName: String,
        newPhone: String,
        newAddress: String,
        newAddressDetail: String,
        lat: Double,
        lng: Double
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        // Cập nhật các trường thông tin mặc định gốc của user
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
                // Thêm một object cấu trúc đầy đủ vào mảng lịch sử địa chỉ đã lưu
                val addressMap = mapOf(
                    "name" to newName,
                    "phone" to newPhone,
                    "address" to newAddress,
                    "addressDetail" to newAddressDetail,
                    "latitude" to lat,
                    "longitude" to lng
                )
                db.collection("users").document(uid)
                    .update("savedAddresses", FieldValue.arrayUnion(addressMap))
                    .addOnSuccessListener {
                        Log.d("CartViewModel", "Đã cập nhật mảng savedAddresses cấu trúc mới thành công")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("CartViewModel", "Lỗi cập nhật thông tin User: ${e.message}")
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

    fun getTotalPrice(): Double {
        return cartItems.sumOf { it.dish.price * it.quantity }
    }

    fun clearCart() {
        cartItems.clear()
        currentRestaurantId = ""
        currentRestaurantName = ""
    }
}