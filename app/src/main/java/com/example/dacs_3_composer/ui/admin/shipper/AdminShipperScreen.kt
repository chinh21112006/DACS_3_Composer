package com.example.dacs_3_composer.ui.admin.shipper

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.dacs_3_composer.ui.admin.shipper.components.*

@Composable
fun AdminShipperScreen(
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { ShipperTopBar(onAddShipperClick = {}) },
        containerColor = Color(0xFFF8F9FA) // Nền xám dịu đồng nhất thiết kế mẫu
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Lưới thống kê số liệu
            item { ShipperStatsGrid() }

            // 2. Ô tìm kiếm & Bộ lọc Tab
            item { ShipperSearchAndFilter() }

            // 3. Danh sách các Shipper mẫu
            item {
                ShipperCardItem(
                    name = "Nguyễn Minh Tuấn",
                    location = "Quận 1, TP.HCM",
                    orderCount = "1,240",
                    rating = "4.9",
                    status = "ACTIVE"
                )
            }

            item {
                ShipperCardItem(
                    name = "Trần Thị Lan Anh",
                    location = "Quận 7, TP.HCM",
                    orderCount = "856",
                    rating = "4.7",
                    status = "DELIVERING"
                )
            }

            item {
                ShipperCardItem(
                    name = "Lê Hoàng Nam",
                    location = "Quận Bình Thạnh",
                    orderCount = "2,100",
                    rating = "3.2",
                    status = "LOCKED",
                    lockReason = "Vi phạm quy tắc"
                )
            }

            // 4. Thanh chuyển trang dưới cùng
            item { ShipperPagination() }
        }
    }
}