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
import com.example.dacs_3_composer.ui.shipper.profile.ShipperProfileViewModel
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ShipperDashboardScreen(
    onOrderClick: (String) -> Unit,
    onMessageClick: () -> Unit, // 🎯 THÊM THAM SỐ NÀY
    shipperViewModel: ShipperViewModel = viewModel(),
    profileViewModel: ShipperProfileViewModel = viewModel()
) {
    val isReadyToWork by shipperViewModel.isReadyToWork.collectAsState()
    val availableOrders by shipperViewModel.availableOrders.collectAsState()
    val activeDeliveryOrder by shipperViewModel.activeDeliveryOrder.collectAsState()
    val todayIncome by shipperViewModel.todayIncomeStr.collectAsState()
    val completedCount by shipperViewModel.completedOrdersCountStr.collectAsState()
    val user by profileViewModel.user.collectAsState()

    val firestore = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }

    Scaffold(
        topBar = {
            ShipperDashboardTopBar(
                user = user,
                onMessageClick = onMessageClick // 🎯 KÍCH HOẠT NÚT CHAT
            )
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

            item {
                WorkStatusToggleCard(
                    isReady = isReadyToWork,
                    onStatusChange = { shipperViewModel.toggleWorkStatus(it) }
                )
            }

            item {
                IncomeCard(
                    todayIncome = todayIncome,
                    growthText = "Thu nhập dựa trên đơn hoàn thành"
                )
            }

            item {
                StatsMiniCardRow(
                    completedCount = completedCount,
                    activeCount = if (activeDeliveryOrder != null) "1" else "0"
                )
            }

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
            } else {
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