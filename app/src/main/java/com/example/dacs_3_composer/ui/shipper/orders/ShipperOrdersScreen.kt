package com.example.dacs_3_composer.ui.shipper.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs_3_composer.ui.shipper.dashboard.ShipperViewModel
import com.example.dacs_3_composer.ui.shipper.orders.components.ActiveDeliveryCard
import com.example.dacs_3_composer.ui.shipper.orders.components.AvailableOrderCard

@Composable
fun ShipperOrdersScreen(
    viewModel: ShipperViewModel,
    onNavigateToDetail: (String) -> Unit
) {
    val availableOrders by viewModel.availableOrders.collectAsState()
    val activeDeliveryOrder by viewModel.activeDeliveryOrder.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Đang giao", "Lịch sử")

    Scaffold(
        topBar = {
            // Thanh Topbar hiển thị tên Shipper và chuông thông báo độc lập
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2E8F0))
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_my_calendar),
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        tint = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Name Ship",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB),
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { /* Xử lý thông báo */ }) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_my_calendar), // Thay bằng icon chuông của bạn
                        contentDescription = "Notification",
                        tint = Color(0xFF2563EB)
                    )
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tiêu đề chức năng đơn hàng
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Đơn hàng của tôi",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Text(
                    text = "Quản lý các chuyến giao hàng hiện tại và lịch sử.",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Thanh chuyển đổi Tab "Đang giao" và "Lịch sử" trùng khớp bản vẽ
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF2563EB),
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 16.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nội dung bên trong danh sách
            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Render đơn hàng chủ chốt đang đi giao
                    activeDeliveryOrder?.let { activeOrder ->
                        item {
                            ActiveDeliveryCard(
                                order = activeOrder,
                                onDetailClick = { onNavigateToDetail(activeOrder.id) }
                            )
                        }
                    }

                    // 2. Render danh sách các đơn hàng vệ tinh hoặc đơn hàng chờ khác
                    val poolOrders = availableOrders.filter { it.id != activeDeliveryOrder?.id }
                    if (poolOrders.isEmpty() && activeDeliveryOrder == null) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "Không có đơn hàng nào trong tiến trình.", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    } else {
                        items(poolOrders) { order ->
                            AvailableOrderCard(
                                order = order,
                                onUpdateClick = { onNavigateToDetail(order.id) }
                            )
                        }
                    }
                }
            } else {
                // Tab lịch sử
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Lịch sử đơn hàng trống", color = Color.Gray)
                }
            }
        }
    }
}