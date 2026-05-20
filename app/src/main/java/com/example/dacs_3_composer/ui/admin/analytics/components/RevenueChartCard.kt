package com.example.dacs_3_composer.ui.admin.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RevenueChartCard() {
    var isWeekSelected by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Biểu đồ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(text = "Biểu đồ doanh thu", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(text = "Thống kê theo tuần", fontSize = 12.sp, color = Color.Gray)
                }

                // Nút Toggle Tuần / Tháng
                Row(
                    modifier = Modifier
                        .background(Color(0xFFE8EFFF), RoundedCornerShape(20.dp))
                        .padding(2.dp)
                ) {
                    val activeButtonColors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052CC), contentColor = Color.White)
                    val inactiveButtonColors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(0xFF0052CC))

                    Button(
                        onClick = { isWeekSelected = true },
                        colors = if (isWeekSelected) activeButtonColors else inactiveButtonColors,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Tuần", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { isWeekSelected = false },
                        colors = if (!isWeekSelected) activeButtonColors else inactiveButtonColors,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Tháng", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Khu vực vẽ biểu đồ tĩnh (Giả lập các đường lưới và khoảng trống)
            Column(
                modifier = Modifier.fillMaxWidth().height(140.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(4) {
                    HorizontalDivider(color = Color(0xFFF1F3F4))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nhãn trục hoành (X-Axis)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val days = listOf("Th 2", "Th 3", "Th 4", "Th 5", "Th 6", "Th 7", "CN")
                days.forEach { day ->
                    Text(
                        text = day,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.width(32.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}