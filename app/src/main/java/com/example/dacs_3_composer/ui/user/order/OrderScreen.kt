package com.example.dacs_3_composer.ui.user.order

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dacs_3_composer.R
import com.example.dacs_3_composer.data.model.Restaurant
import com.example.dacs_3_composer.ui.user.order.components.EmptyOrderState
import com.example.dacs_3_composer.ui.user.order.components.HistoryOrderItem
import com.example.dacs_3_composer.ui.user.order.components.OngoingOrderItem
import com.example.dacs_3_composer.ui.user.order.components.OrderTabBar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    suggestedRestaurants: List<Restaurant>,
    chuyenHienThiTrangChiTiet: (String) -> Unit,
    orderViewModel: OrderViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    val ongoingOrders by orderViewModel.ongoingOrders.collectAsState()
    val historyOrders by orderViewModel.historyOrders.collectAsState()
    val isLoading by orderViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0.dp),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Đơn hàng",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF191C1D)
                        )
                        IconButton(onClick = { /* Tìm kiếm đơn hàng */ }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Tìm kiếm",
                                tint = Color(0xFF191C1D),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                OrderTabBar(
                    selectedTabIndex = selectedTabIndex,
                    onTabSelected = { selectedTabIndex = it }
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF2159BC))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (selectedTabIndex) {
                            0 -> { // 🎯 TAB 0: CHỜ XÁC NHẬN
                                val pendingOrders = ongoingOrders.filter { it.status == "PENDING" }

                                if (pendingOrders.isEmpty()) {
                                    item { EmptyOrderState() }
                                } else {
                                    item {
                                        Text(
                                            text = "Đơn hàng chờ xác nhận (${pendingOrders.size})",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF191C1D),
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }

                                    items(pendingOrders) { order ->
                                        val itemsSummaryText = order.items.joinToString(", ") { "${it.quantity}x ${it.name}" }
                                        val imgUrl = orderViewModel.restaurantImages[order.restaurantId] ?: ""

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                OngoingOrderItem(
                                                    restaurantName = order.restaurantName,
                                                    statusText = "Quán đang xem đơn hàng...",
                                                    estimatedTime = "Mã đơn: ..${order.id.takeLast(6)}",
                                                    itemsSummary = itemsSummaryText,
                                                    restaurantImageUrl = imgUrl,
                                                    chuyenTheoDoiDonHang = { chuyenHienThiTrangChiTiet(order.id) }
                                                )

                                                Spacer(modifier = Modifier.height(12.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            orderViewModel.cancelOrder(
                                                                orderId = order.id,
                                                                onSuccess = {
                                                                    Toast.makeText(context, "Đã hủy đơn hàng!", Toast.LENGTH_SHORT).show()
                                                                },
                                                                onFailure = { errorMessage ->
                                                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                                                }
                                                            )
                                                        },
                                                        shape = RoundedCornerShape(14.dp),
                                                        modifier = Modifier.weight(1f),
                                                        border = BorderStroke(1.dp, Color(0xFFDC3545)),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC3545))
                                                    ) {
                                                        Text(text = "Hủy đơn", fontWeight = FontWeight.SemiBold)
                                                    }

                                                    Button(
                                                        onClick = { chuyenHienThiTrangChiTiet(order.id) },
                                                        shape = RoundedCornerShape(14.dp),
                                                        modifier = Modifier.weight(1.2f),
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2159BC))
                                                    ) {
                                                        Text(text = "Theo dõi đơn", fontWeight = FontWeight.Bold, color = Color.White)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            1 -> { // 🎯 TAB 1: ĐANG ĐẾN (Đã sửa bộ lọc chứa PROCESSING, ACCEPTED, SHIPPING)
                                val activeDeliveryOrders = ongoingOrders.filter {
                                    it.status == "PROCESSING" || it.status == "ACCEPTED" || it.status == "SHIPPING"
                                }

                                if (activeDeliveryOrders.isEmpty()) {
                                    item {
                                        Box(modifier = Modifier.fillParentMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                            Text(text = "Không có đơn hàng nào đang đến.", color = Color.Gray)
                                        }
                                    }
                                } else {
                                    item {
                                        Text(
                                            text = "Đơn hàng đang đến (${activeDeliveryOrders.size})",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF191C1D),
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }

                                    items(activeDeliveryOrders) { order ->
                                        val itemsSummary = order.items.joinToString(", ") { "${it.quantity}x ${it.name}" }
                                        val imgUrl = orderViewModel.restaurantImages[order.restaurantId] ?: ""
                                        val dinhDangGia = String.format(Locale("vi", "VN"), "%,.0f đ", order.totalPrice)

                                        // Đổi văn bản trạng thái hiển thị linh hoạt dựa trên status Firebase
                                        val currentStatusText = when (order.status) {
                                            "PROCESSING" -> " - Đang nấu món"
                                            "ACCEPTED" -> " - Chờ shipper lấy hàng"
                                            "SHIPPING" -> " - Đang giao hàng"
                                            else -> ""
                                        }

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    coil.compose.AsyncImage(
                                                        model = imgUrl.ifBlank { R.drawable.banner1 },
                                                        contentDescription = null,
                                                        modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)),
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                    )

                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = order.restaurantName + currentStatusText,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 16.sp,
                                                            color = Color(0xFF191C1D)
                                                        )
                                                        Text(text = order.time, fontSize = 12.sp, color = Color(0xFF727785))
                                                        Text(text = itemsSummary, fontSize = 13.sp, color = Color(0xFF191C1D), modifier = Modifier.padding(top = 4.dp))
                                                    }
                                                    Text(text = dinhDangGia, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2159BC))
                                                }

                                                Spacer(modifier = Modifier.height(16.dp))

                                                Button(
                                                    onClick = { chuyenHienThiTrangChiTiet(order.id) },
                                                    shape = RoundedCornerShape(16.dp),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2159BC))
                                                ) {
                                                    Text(text = "Xem trạng thái đơn hàng", fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            2 -> { // 🎯 TAB 2: LỊCH SỬ ĐƠN HÀNG (Chứa COMPLETED và CANCELLED)
                                if (historyOrders.isEmpty()) {
                                    item {
                                        Box(modifier = Modifier.fillParentMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                            Text(text = "Bạn chưa có lịch sử đơn hàng nào.", color = Color.Gray)
                                        }
                                    }
                                } else {
                                    item {
                                        Text(
                                            text = "Đơn hàng gần đây (${historyOrders.size})",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF191C1D),
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }

                                    items(historyOrders) { order ->
                                        val historyItemsSummary = order.items.joinToString(", ") { "${it.quantity}x ${it.name}" }

                                        // Hiển thị nhãn phân biệt Đã hủy hoặc Hoàn thành sạch sẽ
                                        val displayStatus = when(order.status) {
                                            "CANCELLED" -> " - Đã hủy"
                                            "COMPLETED" -> " - Đã hoàn thành"
                                            else -> ""
                                        }
                                        val historyImgUrl = orderViewModel.restaurantImages[order.restaurantId] ?: ""
                                        val dinhDangGia = String.format(Locale("vi", "VN"), "%,.0f đ", order.totalPrice)

                                        HistoryOrderItem(
                                            restaurantName = order.restaurantName.ifBlank { "Cửa hàng đối tác" } + displayStatus,
                                            date = order.time,
                                            price = dinhDangGia,
                                            itemsSummary = historyItemsSummary,
                                            restaurantImageUrl = historyImgUrl,
                                            onReorderClick = { /* Xử lý mua lại nhanh nếu cần */ },
                                            onDetailClick = { chuyenHienThiTrangChiTiet(order.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}