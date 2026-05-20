package com.example.dacs_3_composer.ui.shipper.dashboard.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs_3_composer.data.model.OrderItem

@Composable
fun ItemsBillCard(
    orderId: String?,
    items: List<OrderItem>?, // Chuyển sang dạng Nullable để tránh lỗi dữ liệu từ Firebase trống
    totalPrice: Double?
) {
    // Xử lý dữ liệu an toàn phòng hờ Firebase trả về null
    val safeOrderId = orderId ?: ""
    val safeItems = items ?: emptyList()
    val safeTotalPrice = totalPrice ?: 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Tiêu đề Thẻ Hóa Đơn
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chi tiết thực đơn",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Text(
                    text = if (safeOrderId.isNotBlank()) "#${safeOrderId.takeLast(6).uppercase()}" else "",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Kiểm tra danh sách món ăn: Nếu trống thì hiển thị trạng thái thay vì crash UI
            if (safeItems.isEmpty()) {
                Text(
                    text = "Không có thông tin chi tiết món ăn hoặc đang tải...",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )
            } else {
                // Vòng lặp hiển thị danh sách món ăn động từ Firestore
                safeItems.forEach { item ->
                    FoodBillItem(
                        name = item.name.ifBlank { "Món ăn không tên" },
                        note = "Số lượng: x${item.quantity}",
                        price = "${String.format("%,.0f", item.price)}đ"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Tổng kết số tiền hóa đơn cuối cùng
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tổng thanh toán",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Text(
                    text = "${String.format("%,.0f", safeTotalPrice)}đ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB)
                )
            }
        }
    }
}

@Composable
private fun FoodBillItem(name: String, note: String, price: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Khối hình vuông giả lập ảnh món ăn (bạn có thể thay bằng AsyncImage coil sau này)
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE2E8F0))
        )
        Spacer(modifier = Modifier.width(12.dp))

        // Tên và số lượng món ăn
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = note, fontSize = 11.sp, color = Color(0xFF6B7280))
        }

        // Giá tiền của món ăn
        Text(text = price, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2563EB))
    }
}