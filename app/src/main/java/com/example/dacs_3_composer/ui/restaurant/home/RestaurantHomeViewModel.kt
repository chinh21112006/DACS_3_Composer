package com.example.dacs_3_composer.ui.restaurant.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.dacs_3_composer.data.model.Order
import com.example.dacs_3_composer.data.model.TopDish
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RestaurantHomeViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var totalRevenue by mutableStateOf("0")
        private set
    var totalOrdersCount by mutableStateOf("0")
        private set

    private val _topDishes = MutableStateFlow<List<TopDish>>(emptyList())
    val topDishes: StateFlow<List<TopDish>> = _topDishes

    // Mảng lưu tỷ lệ chiều cao biểu đồ cho 7 ngày trong tuần (T2 -> CN) từ 0.0f đến 1.0f
    var weeklyChartData by mutableStateOf(listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f))
        private set

    private val currentRestaurantId: String
        get() = auth.currentUser?.uid ?: "uid_quan_cua_ban"

    init {
        fetchDashboardData()
    }

    fun fetchDashboardData() {
        firestore.collection("orders")
            .whereEqualTo("restaurantId", currentRestaurantId)
            .whereEqualTo("status", "COMPLETED") // Chỉ tính doanh thu trên đơn thành công
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("RestaurantHomeVM", "Lỗi lấy dữ liệu doanh thu", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val completedOrders = snapshot.toObjects(Order::class.java)

                    // 1. Tính tổng doanh thu & tổng số đơn
                    val revenueSum = completedOrders.sumOf { it.totalPrice }
                    totalRevenue = String.format(Locale("vi", "VN"), "%,.0f", revenueSum)
                    totalOrdersCount = String.format(Locale("vi", "VN"), "%,d", completedOrders.size)

                    // 2. Tính toán biểu đồ doanh thu tuần (Phân tích theo thứ)
                    calculateWeeklyChart(completedOrders)

                    // 3. Phân tích thống kê Top 3 món ăn bán chạy nhất
                    calculateTopDishes(completedOrders)
                }
            }
    }

    private fun calculateWeeklyChart(orders: List<Order>) {
        val daysRevenue = mutableMapOf(
            Calendar.MONDAY to 0.0, Calendar.TUESDAY to 0.0, Calendar.WEDNESDAY to 0.0,
            Calendar.THURSDAY to 0.0, Calendar.FRIDAY to 0.0, Calendar.SATURDAY to 0.0, Calendar.SUNDAY to 0.0
        )

        // Cấu hình định dạng đọc chuỗi thời gian từ thuộc tính order.time (Ví dụ: "Hôm nay, 14:30" hoặc "2026-05-20 14:30")
        // Nếu dùng chuỗi tự do, bạn nên lưu thêm trường timestamp (Long) để phân tích chuẩn xác hơn.
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        orders.forEach { order ->
            try {
                val date = sdf.parse(order.time) ?: java.util.Date()
                val cal = Calendar.getInstance().apply { time = date }
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                if (daysRevenue.containsKey(dayOfWeek)) {
                    daysRevenue[dayOfWeek] = daysRevenue[dayOfWeek]!! + order.totalPrice
                }
            } catch (e: Exception) {
                // Dự phòng: Nếu chuỗi thời gian không khớp định dạng, tạm tính vào ngày hôm nay để tránh lỗi crash
                val todayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                daysRevenue[todayOfWeek] = daysRevenue[todayOfWeek]!! + order.totalPrice
            }
        }

        val maxRevenue = daysRevenue.values.maxOrNull() ?: 1.0
        val safeMax = if (maxRevenue == 0.0) 1.0 else maxRevenue

        // Sắp xếp mảng trả về đúng thứ tự giao diện Việt Nam: T2, T3, T4, T5, T6, T7, CN
        weeklyChartData = listOf(
            (daysRevenue[Calendar.MONDAY]!! / safeMax).toFloat(),
            (daysRevenue[Calendar.TUESDAY]!! / safeMax).toFloat(),
            (daysRevenue[Calendar.WEDNESDAY]!! / safeMax).toFloat(),
            (daysRevenue[Calendar.THURSDAY]!! / safeMax).toFloat(),
            (daysRevenue[Calendar.FRIDAY]!! / safeMax).toFloat(),
            (daysRevenue[Calendar.SATURDAY]!! / safeMax).toFloat(),
            (daysRevenue[Calendar.SUNDAY]!! / safeMax).toFloat()
        )
    }

    private fun calculateTopDishes(orders: List<Order>) {
        val dishCountMap = mutableMapOf<String, Int>() // [Tên món -> Số lượng]

        // Cộng dồn số lượng từng món ăn xuất hiện trong danh sách đơn hàng
        orders.flatMap { it.items }.forEach { item ->
            if (item.name.isNotBlank()) {
                dishCountMap[item.name] = (dishCountMap[item.name] ?: 0) + item.quantity
            }
        }

        // Sắp xếp giảm dần theo số lượng đặt, lấy tối đa 3 món đầu tiên
        val sortedDishes = dishCountMap.entries
            .sortedByDescending { it.value }
            .take(3)
            .mapIndexed { index, entry ->
                TopDish(
                    rank = index + 1,
                    dishName = entry.key,
                    ordersCount = entry.value,
                    imageUrl = "" // Bạn có thể tối ưu truy vấn thêm bảng dishes để lấy ảnh, tạm thời dùng ảnh mặc định trên UI
                )
            }

        _topDishes.value = sortedDishes
    }
}