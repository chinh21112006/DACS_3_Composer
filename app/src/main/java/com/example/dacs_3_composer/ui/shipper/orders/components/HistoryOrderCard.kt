package com.example.dacs_3_composer.ui.shipper.orders.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs_3_composer.data.model.Order

@Composable
fun HistoryOrderCard(
    order: Order,
    onCardClick: () -> Unit
) {
    val isCancelled = order.status == "CANCELLED"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#ORD-${order.id.takeLast(4).uppercase()}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                // Badge phân biệt Đã giao thành công hay Đã hủy đơn
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isCancelled) Color(0xFFFFE4E6) else Color(0xFFDCFCE7))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isCancelled) "Đã hủy" else "Thành công",
                        color = if (isCancelled) Color(0xFFE11D48) else Color(0xFF16A34A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tên Quán và Giá Tiền kiếm được
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.restaurantName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF374151),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${String.format("%,.0f", order.shippingFee)}đ", // 🌟 ĐÃ SỬA: Đổi sang shippingFee để hiển thị thu nhập thực tế của shipper
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCancelled) Color.Gray else Color(0xFF111827)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Địa chỉ giao
            Text(
                text = "📍 Khách: ${order.customerAddress}",
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 1
            )

            if (order.time.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🕒 Thời gian: ${order.time}",
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
    }
}