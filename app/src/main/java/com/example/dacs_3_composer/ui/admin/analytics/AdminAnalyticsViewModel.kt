package com.example.dacs_3_composer.ui.admin.analytics

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.dacs_3_composer.data.model.Order
import com.example.dacs_3_composer.data.model.User
import com.example.dacs_3_composer.data.model.OrderStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Data class chứa toàn bộ chỉ số thực tế cần hiển thị trên trang chủ
data class AnalyticsState(
    val totalUsers: Int = 0,
    val totalRestaurants: Int = 0,
    val totalShippers: Int = 0,
    val totalOrders: Int = 0,
    val shippingOrders: Int = 0,
    val completedOrders: Int = 0,
    val weeklyRevenue: List<Double> = listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) // Index 0->6 ứng với Thứ 2 -> Chủ Nhật
)

class AdminAnalyticsViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _analyticsState = MutableStateFlow(AnalyticsState())
    val analyticsState: StateFlow<AnalyticsState> = _analyticsState

    init {
        observeSystemData()
    }

    private fun observeSystemData() {
        // 1. LẮNG NGHE REALTIME COLLECTION "users"
        firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AdminAnalyticsVM", "Lỗi lắng nghe dữ liệu người dùng: ", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    var usersCount = 0
                    var restaurantsCount = 0
                    var shippersCount = 0

                    for (doc in snapshot.documents) {
                        try {
                            val user = doc.toObject(User::class.java)
                            if (user != null) {
                                when (user.role.lowercase()) {
                                    "user" -> usersCount++
                                    "restaurant" -> restaurantsCount++
                                    "shipper" -> shippersCount++
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("AdminAnalyticsVM", "Lỗi map object User: ${e.message}")
                        }
                    }

                    _analyticsState.value = _analyticsState.value.copy(
                        totalUsers = usersCount,
                        totalRestaurants = restaurantsCount,
                        totalShippers = shippersCount
                    )
                }
            }

        // 2. LẮNG NGHE REALTIME COLLECTION "orders"
        firestore.collection("orders")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AdminAnalyticsVM", "Lỗi lắng nghe dữ liệu đơn hàng: ", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    var totalCount = snapshot.size()
                    var shippingCount = 0
                    var completedCount = 0

                    // Mảng chứa doanh thu thực tế từ Thứ 2 đến Chủ Nhật của tuần hiện tại
                    val revenueArray = mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val currentCalendar = Calendar.getInstance()

                    // Lấy thông tin tuần hiện tại trong năm để đối chiếu
                    val currentWeek = currentCalendar.get(Calendar.WEEK_OF_YEAR)
                    val currentYear = currentCalendar.get(Calendar.YEAR)

                    for (doc in snapshot.documents) {
                        try {
                            val order = doc.toObject(Order::class.java)
                            if (order != null) {
                                // Đồng bộ đếm trạng thái đơn hàng theo cấu trúc chuẩn của bạn
                                when (order.status.uppercase()) {
                                    "SHIPPING" -> shippingCount++
                                    "COMPLETED" -> completedCount++
                                }

                                // Chỉ tính doanh thu cho các đơn hàng đã thành công (COMPLETED)
                                if (order.status.uppercase() == "COMPLETED" && order.time.isNotBlank()) {
                                    // Parse chuỗi ngày từ trường order.time (ví dụ: "21/05/2026 14:30")
                                    val dateStr = order.time.split(" ").firstOrNull() ?: ""
                                    val date = sdf.parse(dateStr)

                                    if (date != null) {
                                        val orderCal = Calendar.getInstance()
                                        orderCal.time = date

                                        // Kiểm tra nếu đơn hàng thuộc tuần này và năm này
                                        if (orderCal.get(Calendar.WEEK_OF_YEAR) == currentWeek &&
                                            orderCal.get(Calendar.YEAR) == currentYear) {

                                            val dayOfWeek = orderCal.get(Calendar.DAY_OF_WEEK)
                                            // Chuyển đổi định dạng: Calendar quy định SUNDAY = 1, MONDAY = 2...
                                            val targetIndex = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2

                                            if (targetIndex in 0..6) {
                                                revenueArray[targetIndex] += order.totalPrice
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("AdminAnalyticsVM", "Lỗi xử lý đơn hàng tính doanh thu: ${e.message}")
                        }
                    }

                    _analyticsState.value = _analyticsState.value.copy(
                        totalOrders = totalCount,
                        shippingOrders = shippingCount,
                        completedOrders = completedCount,
                        weeklyRevenue = revenueArray
                    )
                }
            }
    }
}