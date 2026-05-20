package com.example.dacs_3_composer.ui.admin.complaint

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs_3_composer.ui.admin.complaint.components.*
import com.example.dacs_3_composer.ui.admin.overview.components.OverviewTopBar

@Composable
fun AdminComplaintScreen() {
    Scaffold(
        topBar = { OverviewTopBar(adminName = "Gourmet Admin") },
        floatingActionButton = {
            FloatingActionButton(onClick = {}, containerColor = Color(0xFF2159BC), contentColor = Color.White) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Dòng thẻ thống kê tổng quan (Doanh thu, Đơn mới, Khiếu nại)
            item { ComplaintStatsRow() }

            // 2. Khối cấu hình bộ lọc nâng cao tìm kiếm
            item { AdvancedFilterCard() }

            // Tiêu đề vùng danh sách đơn hàng
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Danh sách đơn hàng (24)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Mới nhất ▾", fontSize = 13.sp, color = Color.Gray)
                }
            }

            // 3. Danh sách đơn hàng tĩnh đổ theo mô hình
            item {
                AdminOrderCardItem(
                    orderId = "#ORD-88291",
                    restaurantName = "Gourmet Central - Q1",
                    details = "Khách hàng: Trần Văn Anh • 3 món • 1.250.000đ",
                    time = "14:20 - Hôm nay",
                    status = "PENDING"
                )
            }

            item {
                AdminOrderCardItem(
                    orderId = "#ORD-88285",
                    restaurantName = "Sakura Sushi - Q7",
                    details = "Khách hàng: Lê Minh Tâm • 5 món • 2.890.000đ",
                    time = "13:45 - Hôm nay",
                    status = "SHIPPING"
                )
            }

            item {
                AdminOrderCardItem(
                    orderId = "#ORD-88270",
                    restaurantName = "Le Petit Bistro - Q3",
                    details = "Khách hàng: Nguyễn Thị Lan • 2 món • 850.000đ",
                    time = "12:10 - Hôm nay",
                    status = "DELIVERED"
                )
            }

            // 4. Vùng trung tâm xử lý khiếu nại khẩn cấp màu đỏ hồng
            item { ActiveComplaintsSection() }

            // 5. Nhật ký hệ thống dưới chân trang
            item { SystemLogCard() }
        }
    }
}