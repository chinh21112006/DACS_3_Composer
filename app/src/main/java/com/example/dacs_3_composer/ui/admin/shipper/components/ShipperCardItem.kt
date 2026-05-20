package com.example.dacs_3_composer.ui.admin.shipper.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
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
fun ShipperCardItem(
    name: String,
    location: String,
    orderCount: String,
    rating: String,
    status: String, // "ACTIVE", "DELIVERING", "LOCKED"
    lockReason: String? = null
) {
    // Cấu hình màu sắc, text tương ứng với Badge trạng thái
    val (statusText, statusBg, statusColor, dotColor) = when (status) {
        "ACTIVE" -> Quadruple("ĐANG HOẠT ĐỘNG", Color(0xFFE8F8F0), Color(0xFF2ECC71), Color(0xFF2ECC71))
        "DELIVERING" -> Quadruple("ĐANG GIAO HÀNG", Color(0xFFFFF3E0), Color(0xFFE67E22), Color(0xFFE67E22))
        else -> Quadruple("ĐÃ KHÓA TÀI KHOẢN", Color(0xFFFFEAEA), Color(0xFFE74C3C), Color(0xFFE74C3C))
    }

    val isLocked = status == "LOCKED"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = if (isLocked) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD6D6)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                // Khối hình tròn giả lập Avatar kèm chấm trạng thái nhỏ ở góc
                Box(modifier = Modifier.size(54.dp)) {
                    Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.LightGray))
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.BottomEnd)
                            .background(dotColor, CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Cột thông tin chi tiết
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        // Badge trạng thái
                        Box(
                            modifier = Modifier.background(statusBg, RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = statusText, fontSize = 10.sp, color = statusColor, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "📍 $location", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "📦 $orderCount đơn", fontSize = 13.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F3F4))
            Spacer(modifier = Modifier.height(12.dp))

            // Phần chân dưới của thẻ: Thay đổi tùy thuộc vào tài khoản có bị khóa hay không
            if (isLocked) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Lý do", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(text = lockReason ?: "Vi phạm quy tắc", fontSize = 13.sp, color = Color(0xFFE74C3C), fontWeight = FontWeight.Medium)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("Mở khóa", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = {},
                            modifier = Modifier.background(Color(0xFFF8F9FA), RoundedCornerShape(8.dp)).size(36.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFE74C3C))
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Đánh giá ", fontSize = 13.sp, color = Color.Gray)
                        Text(text = "$rating ⭐", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {},
                            modifier = Modifier.background(Color(0xFFE8EFFF), RoundedCornerShape(8.dp)).size(36.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF0052CC), modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = {},
                            modifier = Modifier.background(Color(0xFFFFEAEA), RoundedCornerShape(8.dp)).size(36.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFE74C3C), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

// Data class bổ trợ chứa bộ 4 dữ liệu trạng thái gọn gàng
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)