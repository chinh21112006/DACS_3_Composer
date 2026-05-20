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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs_3_composer.data.model.OrderItem

@Composable
fun ItemsBillCard(
    orderId: String,
    items: List<OrderItem>,
    totalPrice: Double,
    shippingFee: Double, // 🌟 ĐÃ BỔ SUNG: Tham số nhận tiền ship động từ màn hình cha
    modifier: Modifier = Modifier
) {
    // Tính toán tiền món ăn thực tế = Tổng hóa đơn - Tiền ship
    val totalDishPrice = (totalPrice - shippingFee).coerceAtLeast(0.0)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Chi tiết đơn hàng",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Danh sách các món ăn trong hóa đơn
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            fontSize = 14.sp,
                            color = Color(0xFF374151),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Số lượng: ${item.quantity}",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                    Text(
                        text = "${String.format("%,.0f", item.price * item.quantity)}đ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6))
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Dòng hiển thị tổng tiền các món ăn
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Tổng tiền món", fontSize = 13.sp, color = Color(0xFF6B7280))
                Text(text = "${String.format("%,.0f", totalDishPrice)}đ", fontSize = 13.sp, color = Color(0xFF374151))
            }

            // 2. Dòng hiển thị tiền ship lấy động từ Database (Thay vì viết cứng 20k)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Phí vận chuyển (Shipper nhận)", fontSize = 13.sp, color = Color(0xFF6B7280))
                Text(
                    text = "${String.format("%,.0f", shippingFee)}đ", // 🌟 Hiển thị chuẩn xác số tiền từ DB
                    fontSize = 13.sp,
                    color = Color(0xFF2563EB), // Đổi màu xanh làm điểm nhấn doanh thu
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFE5E7EB))
            Spacer(modifier = Modifier.height(8.dp))

            // 3. Dòng Thành tiền cuối cùng khách phải trả
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Khách thanh toán",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Text(
                    text = "${String.format("%,.0f", totalPrice)}đ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFEF4444) // Màu đỏ nổi bật tổng tiền cần thu hộ/thanh toán
                )
            }
        }
    }
}