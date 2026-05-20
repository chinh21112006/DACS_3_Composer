package com.example.dacs_3_composer.ui.shipper.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dacs_3_composer.ui.shipper.dashboard.components.*
import com.example.dacs_3_composer.ui.shipper.profile.ShipperProfileViewModel // 🎯 IMPORT VIEWMODEL PROFILE
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ShipperDashboardScreen(
    onOrderClick: (String) -> Unit,
    shipperViewModel: ShipperViewModel = viewModel(),
    profileViewModel: ShipperProfileViewModel = viewModel() // 🎯 INJECT THÊM VIEWMODEL PROFILE
) {
    val isReadyToWork by shipperViewModel.isReadyToWork.collectAsState()
    val availableOrders by shipperViewModel.availableOrders.collectAsState()
    val activeDeliveryOrder by shipperViewModel.activeDeliveryOrder.collectAsState()

    // 🌟 LẤY DỮ LIỆU ĐỘNG TỰ ĐỘNG TÍNH TOÁN TỪ VIEWMODEL GỐC
    val todayIncome by shipperViewModel.todayIncomeStr.collectAsState()
    val completedCount by shipperViewModel.completedOrdersCountStr.collectAsState()

    // 🎯 LẤY STATE USER THỰC TẾ ĐỂ HIỂN THỊ TÊN & AVATAR
    val user by profileViewModel.user.collectAsState()

    val firestore = FirebaseFirestore.getInstance()

    // 🎯 TỰ ĐỘNG REFRESH THÔNG TIN TÀI XẾ MỖI KHI VÀO LẠI TAB TRANG CHỦ
    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }

    Scaffold(
        topBar = {
            // 🎯 TRUYỀN USER THẬT VÀO TOPBAR
            ShipperDashboardTopBar(user = user)
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

            // 1. Thẻ bật/tắt nhận đơn tự động
            item {
                WorkStatusToggleCard(
                    isReady = isReadyToWork,
                    onStatusChange = { shipperViewModel.toggleWorkStatus(it) }
                )
            }

            // 2. Thẻ thu nhập thực tế
            item {
                IncomeCard(
                    todayIncome = todayIncome, // 🌟 Đã truyền dữ liệu thật động ở đây
                    growthText = "Thu nhập dựa trên đơn hoàn thành"
                )
            }

            // 3. Thống kê số đơn thực tế
            item {
                StatsMiniCardRow(
                    completedCount = completedCount, // 🌟 Truyền dữ liệu đếm đơn xuống row con
                    activeCount = if (activeDeliveryOrder != null) "1" else "0"
                )
            }

            // THẾ TRẬN 1: TÀI XẾ ĐANG CÓ ĐƠN HÀNG ĐANG GIAO / ĐANG ĐẾN LẤY
            if (activeDeliveryOrder != null) {
                item {
                    Text(
                        text = "Đơn hàng đang xử lý",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }

                item {
                    ActiveDeliveryDetailCard(
                        order = activeDeliveryOrder!!,
                        onUpdateStatusClick = { targetStatus ->
                            firestore.collection("orders")
                                .document(activeDeliveryOrder!!.id)
                                .update("status", targetStatus)
                        },
                        onCardClick = { onOrderClick(activeDeliveryOrder!!.id) }
                    )
                }
            }
            // THẾ TRẬN 2: CHƯA NHẬN ĐƠN -> KIỂM TRA SWITCH BẬT/TẮT APP
            else {
                item {
                    Text(
                        text = "Đơn hàng phù hợp hiện có",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }

                if (!isReadyToWork) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Hãy bật trạng thái sẵn sàng để nhận đơn.", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                } else {
                    if (availableOrders.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "Đang quét đơn hàng mới từ các nhà hàng...", color = Color(0xFF2159BC), fontSize = 14.sp)
                            }
                        }
                    } else {
                        items(availableOrders, key = { it.id }) { order ->
                            DashboardAvailableOrderCard(
                                order = order,
                                onCardClick = { onOrderClick(order.id) },
                                onAcceptClick = { shipperViewModel.acceptOrder(order.id) }
                            )
                        }
                    }

                    item { HeatMapBanner() }
                }
                item { Spacer(modifier = Modifier.height(4.dp)) }
            }
        }
    }
}