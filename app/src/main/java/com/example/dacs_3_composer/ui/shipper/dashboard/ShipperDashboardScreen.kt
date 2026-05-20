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
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ShipperDashboardScreen(
    onOrderClick: (String) -> Unit,
    shipperViewModel: ShipperViewModel = viewModel()
) {
    val isReadyToWork by shipperViewModel.isReadyToWork.collectAsState()
    val availableOrders by shipperViewModel.availableOrders.collectAsState()
    val activeDeliveryOrder by shipperViewModel.activeDeliveryOrder.collectAsState()

    val firestore = FirebaseFirestore.getInstance()

    Scaffold(
        topBar = { ShipperDashboardTopBar() },
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

            // 2. Thẻ thu nhập
            item {
                IncomeCard(
                    todayIncome = "542.000đ",
                    growthText = "+12% so với hôm qua"
                )
            }

            // 3. Thống kê số đơn
            item { StatsMiniCardRow() }

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
                            // Cập nhật linh hoạt theo từng nấc trạng thái ngay tại trang chủ
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

                // ✅ ĐÃ SỬA: Gom cụm logic điều kiện chặt chẽ vào cấu trúc IF-ELSE tuần tự
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
                    // Khi đã bật App -> Kiểm tra xem kho đơn trống hay có đơn
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
                            AvailableOrderCard(
                                order = order,
                                onCardClick = { onOrderClick(order.id) },
                                onAcceptClick = { shipperViewModel.acceptOrder(order.id) }
                            )
                        }
                    }

                    // Chỉ hiển thị Banner bản đồ nhiệt khi tài xế đang bật App làm việc
                    item { HeatMapBanner() }
                }
                item { Spacer(modifier = Modifier.height(4.dp)) }
            }
        }
    }
}