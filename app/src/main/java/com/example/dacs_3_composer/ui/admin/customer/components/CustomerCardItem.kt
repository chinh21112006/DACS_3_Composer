package com.example.dacs_3_composer.ui.admin.customer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomerCardItem(
    name: String,
    phone: String,
    orderCount: String,
    totalSpent: String,
    status: String // "ACTIVE" hoặc "LOCKED"
) {
    val isLocked = status == "LOCKED"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Phần thông tin chính (Avatar, Tên, SĐT, Trạng thái)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.LightGray))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF191C1D))
                    Text(text = phone, fontSize = 13.sp, color = Color(0xFF727785))
                }

                // Badge trạng thái
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isLocked) Color(0xFFFFEBEE) else Color(0xFFE8F8F0),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isLocked) "ĐÃ KHÓA" else "ĐANG HOẠT ĐỘNG",
                        fontSize = 10.sp,
                        color = if (isLocked) Color(0xFFE57373) else Color(0xFF2ECC71),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Phần chỉ số đơn hàng & chi tiêu
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Số đơn hàng", fontSize = 12.sp, color = Color(0xFF727785))
                    Text(text = orderCount, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1D))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Tổng chi tiêu", fontSize = 12.sp, color = Color(0xFF727785))
                    Text(text = totalSpent, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0052CC))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hàng nút hành động dưới cùng
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Nút xem lịch sử đặt hàng
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLocked) Color(0xFFEEF2FF) else Color(0xFF3B82F6),
                        contentColor = if (isLocked) Color(0xFF3B82F6) else Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(text = "Lịch sử đặt hàng", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                // Nút chức năng Khóa / Mở khóa tài khoản
                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .background(
                            color = if (isLocked) Color(0xFFFFEAEA) else Color(0xFFF5F6F8),
                            shape = CircleShape
                        )
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Outlined.LockOpen else Icons.Outlined.Block,
                        contentDescription = null,
                        tint = if (isLocked) Color(0xFFE74C3C) else Color(0xFF727785),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}