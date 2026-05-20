package com.example.dacs_3_composer.ui.shipper.orders.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OutlinedFlag
import androidx.compose.material.icons.filled.Route
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
fun ActiveDeliveryCard(
    order: Order,
    onDetailClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Hàng 1: Mã đơn & Badge trạng thái màu xanh dương
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#ORD-${order.id.takeLast(4).uppercase()}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2563EB))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Đang giao",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hàng 2: Tên quán & Giá tiền ship
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.restaurantName.ifBlank { "Nhà hàng đối tác" },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${String.format("%,.0f", order.totalPrice)}đ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Điểm nhận hàng
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.LocationOn, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Điểm nhận", fontSize = 11.sp, color = Color.Gray)
                    Text(text = "123 Đường Lê Lợi, Quận 1, TP.HCM", fontSize = 13.sp, color = Color(0xFF374151)) // Thay bằng địa chỉ thực tế từ model nếu có
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Điểm giao hàng
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.OutlinedFlag, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Điểm giao", fontSize = 11.sp, color = Color.Gray)
                    Text(text = order.customerAddress.ifBlank { "Địa chỉ khách hàng" }, fontSize = 13.sp, color = Color(0xFF374151))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6))
            Spacer(modifier = Modifier.height(12.dp))

            // Hàng đáy: Số km & Nút bấm xem chi tiết
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Route, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "3.2 km", fontSize = 13.sp, color = Color.Gray)
                }

                Button(
                    onClick = onDetailClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(99.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(text = "Xem chi tiết", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}