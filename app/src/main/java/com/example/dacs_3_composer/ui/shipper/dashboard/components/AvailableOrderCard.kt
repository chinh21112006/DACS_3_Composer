package com.example.dacs_3_composer.ui.shipper.dashboard.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs_3_composer.data.model.Order

@Composable
fun AvailableOrderCard(
    order: com.example.dacs_3_composer.data.model.Order,
    onCardClick: () -> Unit, // 👈 Thêm dòng này để xử lý sự kiện click vào thân Card
    onAcceptClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }, // 🌟 Click vào bất cứ đâu trên Card để xem chi tiết
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "#ĐƠN: ${order.id.takeLast(6).uppercase()}", fontWeight = FontWeight.Bold, color = Color(0xFF2159BC))
                Text(text = "${String.format("%,.0f", order.totalPrice)}đ", fontWeight = FontWeight.Bold, color = Color(0xFF2159BC), fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "📍 Nhà hàng: ${order.restaurantName}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = "🏁 Giao đến: ${order.customerAddress}", fontSize = 13.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onAcceptClick, // Bấm nút này để kích hoạt tiến trình Firebase nhận đơn
                modifier = Modifier.fillMaxWidth().height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2159BC)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Nhận đơn hàng này", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}