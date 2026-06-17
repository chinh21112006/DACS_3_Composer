package com.example.dacs_3_composer.ui.user.cart

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.dacs_3_composer.data.model.DishItem
import com.example.dacs_3_composer.data.model.Order
import com.example.dacs_3_composer.data.model.OrderItem
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

    // Danh sách khuyến mãi thực tế lấy từ Firebase Firestore
    var availablePromotions = mutableStateListOf<Promotion>()
        private set

    /**
     * Tải danh sách Khuyến mãi (Promotions) từ Firestore về và lọc điều kiện thời gian/lượt dùng an toàn
     */
    fun fetchPromotionsFromFirebase() {
        val db = FirebaseFirestore.getInstance()
        db.collection("promotions")
            .whereEqualTo("status", "active") // Chỉ lấy các mã đang ở trạng thái active
            .get()
            .addOnSuccessListener { documents ->
                Log.d("CartViewModel", "Firebase trả về số lượng tài liệu gốc: ${documents.size()}")
                availablePromotions.clear()
                val ngayGioHienTai = Timestamp.now()

                for (document in documents) {
                    try {
                        // Ép kiểu thủ công từng trường để tránh lỗi đúc Object (toObject) từ Firebase
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

                        // Khởi tạo Object Promotion
                        val promo = Promotion(
                            id = id, code = code, type = type, value = value,
                            maxDiscount = maxDiscount, minOrderValue = minOrderValue,
                            usageCount = usageCount, usageLimit = usageLimit,
                            startDate = startDate, endDate = endDate,
                            description = description, title = title, status = status
                        )

                        // Kiểm tra điều kiện thời gian & lượt dùng
                        val đãBắtĐầu = promo.startDate?.let { ngayGioHienTai >= it } ?: true
                        val chưaHếtHạn = promo.endDate?.let { ngayGioHienTai <= it } ?: true
                        val cònLượtDùng = if (promo.usageLimit > 0) promo.usageCount < promo.usageLimit else true

                        // 🔴 SỬA LẠI DÒNG NÀY TRONG CARTVIEWMODEL.KT:
                        Log.d("CartViewModel", "Mã: ${promo.code} -> Bắt đầu: $đãBắtĐầu, Chưa hết hạn: $chưaHếtHạn, Còn lượt: $cònLượtDùng")

                        if (đãBắtĐầu && chưaHếtHạn && cònLượtDùng) {
                            availablePromotions.add(promo)
                        }
                    } catch (e: Exception) {
                        Log.e("CartViewModel", "Lỗi parse tài liệu ${document.id}: ${e.message}")
                    }
                }
                Log.d("CartViewModel", "Tổng số mã hợp lệ hiển thị lên UI: ${availablePromotions.size}")
            }
            .addOnFailureListener { e ->
                Log.e("CartViewModel", "Lỗi tải dữ liệu Khuyến mãi: ${e.message}")
            }
    }

    /**
     * Xử lý đặt đơn hàng và tự động trừ lượt sử dụng mã khuyến mãi trên hệ thống
     */
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
        appliedPromotionTitle: String, // Nhận thêm Tên Voucher
        promotionDiscount: Double,    // Nhận thêm Số tiền giảm giá
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

        val newOrder = Order(
            id = orderId,
            time = currentTime,
            status = "PENDING",
            totalDishPrice = totalDishPrice,
            totalPrice = finalTotal,
            shippingFee = shippingFee,

            // LƯU ĐẦY ĐỦ BỘ BA THÔNG TIN KHUYẾN MÃI VÀO ĐƠN HÀNG
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
            restaurantLat = null,
            restaurantLng = null,
            shipperId = "",
            items = orderItemsList
        )

        db.collection("orders").document(orderId)
            .set(newOrder)
            .addOnSuccessListener {
                // Tăng số lần sử dụng mã (usageCount) lên +1 ngay trên Database Firestore
                if (!appliedPromotionId.isNullOrBlank()) {
                    db.collection("promotions").document(appliedPromotionId)
                        .update("usageCount", FieldValue.increment(1))
                        .addOnSuccessListener {
                            Log.d("CartViewModel", "Đã cập nhật số lần sử dụng của mã: $appliedPromotionId")
                        }
                        .addOnFailureListener { e ->
                            Log.e("CartViewModel", "Lỗi cập nhật số lượng mã: ${e.message}")
                        }
                }
                clearCart()
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e("CartViewModel", "Lỗi đặt đơn hàng: ${exception.message}")
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