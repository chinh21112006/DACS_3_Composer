package com.example.dacs_3_composer.data.model

enum class OrderStatus(val displayName: String) {
    PENDING("Chờ xác nhận"),
    PROCESSING("Đang nấu"),
    ACCEPTED("Chờ shipper"),
    SHIPPING("Đang giao"),
    COMPLETED("Đã hoàn thành"),
    CANCELLED("Đã hủy")
}

data class OrderItem(
    var dishId: String = "",
    var name: String = "",
    var quantity: Int = 0,
    var price: Double = 0.0
)

data class Order(
    // 1. Thông tin định danh Đơn hàng
    var id: String = "",
    var time: String = "",
    var status: String = "PENDING",

    // Cấu trúc tiền chuẩn giống hệt Firebase Console
    var totalDishPrice: Double = 0.0,    // Giá gốc tổng các món ăn
    var shippingFee: Double = 20000.0,   // 🌟 BỔ SUNG: Tiền ship cố định 20k cho mỗi đơn hàng
    var totalPrice: Double = 0.0,        // Số tiền cuối cùng Khách trả = totalDishPrice + shippingFee

    // 2. Thông tin của KHÁCH HÀNG (Người mua)
    var userId: String = "",
    var customerName: String = "",
    var customerPhone: String = "",
    var customerAddress: String = "",

    // 3. Thông tin của NHÀ HÀNG (Người bán)
    var restaurantId: String = "",
    var restaurantName: String = "",

    // 4. Thông tin của SHIPPER (Người giao)
    var shipperId: String = "",

    // 5. Danh sách các món ăn trong đơn
    var items: List<OrderItem> = emptyList()
) {
    // Constructor không tham số bắt buộc để Firebase Firestore ánh xạ (mapping) tự động
    constructor() : this(
        id = "", time = "", status = "PENDING",
        totalDishPrice = 0.0, shippingFee = 20000.0, totalPrice = 0.0, // 🌟 Cập nhật tại đây
        userId = "", customerName = "", customerPhone = "", customerAddress = "",
        restaurantId = "", restaurantName = "",
        shipperId = "", items = emptyList()
    )
}