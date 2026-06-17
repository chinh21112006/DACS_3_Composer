package com.example.dacs_3_composer.ui.user.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs_3_composer.data.model.Order
import com.example.dacs_3_composer.data.model.OrderStatus
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    orderViewModel: OrderViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Kích hoạt lắng nghe dữ liệu thời gian thực của đơn hàng này từ Firebase
    LaunchedEffect(orderId) {
        orderViewModel.observeOrderDetails(orderId)
    }

    val orderState by orderViewModel.currentTrackingOrder.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết hóa đơn", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(paddingValues)
        ) {
            val order = orderState
            if (order == null) {
                // Hiển thị trạng thái đang tải nếu chưa nhận được dữ liệu từ Firebase
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2159BC))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Khối Trạng thái đơn hàng & Thời gian đặt
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Mã đơn hàng: #${order.id.takeLast(8).uppercase(Locale.getDefault())}",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Lấy tên hiển thị tiếng Việt của trạng thái
                                val statusEnum = try { OrderStatus.valueOf(order.status) } catch (e: Exception) { null }
                                val statusText = statusEnum?.displayName ?: order.status
                                val statusColor = when (order.status) {
                                    "PENDING" -> Color(0xFFFF9800)     // Cam: Chờ xác nhận
                                    "PROCESSING" -> Color(0xFF00B0FF)  // Xanh dương nhạt: Đang nấu
                                    "ACCEPTED" -> Color(0xFF9C27B0)    // Tím: Chờ shipper
                                    "SHIPPING" -> Color(0xFF2159BC)    // Xanh dương: Đang giao
                                    "COMPLETED" -> Color(0xFF4CAF50)   // Xanh lá: Hoàn thành
                                    "CANCELLED" -> Color(0xFFDC3545)   // Đỏ: Đã hủy
                                    else -> Color.Gray
                                }

                                Surface(
                                    color = statusColor.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = statusText,
                                        color = statusColor,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Thời gian đặt: ${order.time}",
                                    fontSize = 13.sp,
                                    color = Color(0xFF727785)
                                )
                            }
                        }
                    }

                    // 2. Khối Địa chỉ & Thông tin người nhận
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Thông tin giao hàng", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFEEEEEE))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF2159BC), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = order.customerName, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF2159BC), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = order.customerPhone, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp).padding(top = 2.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = order.customerAddress, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp)
                                }
                            }
                        }
                    }

                    // 3. Tiêu đề danh sách món đặt
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Chi tiết món ăn đã đặt", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                    }

                    // 4. Danh sách các món ăn trong đơn hàng
                    items(order.items) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = item.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Đơn giá: ${String.format(Locale("vi", "VN"), "%,.0fđ", item.price)}",
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )
                                }
                                Text(
                                    text = "x${item.quantity}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Text(
                                    text = String.format(Locale("vi", "VN"), "%,.0fđ", item.price * item.quantity),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    // 5. Khối Đối soát Tài chính chi tiết (Giá gốc, Tiền giảm, Phí Ship, Voucher, Tổng tiền)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(text = "Tóm tắt thanh toán", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 1.dp)

                                // Tổng tiền hàng gốc trước khi giảm
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Tổng giá gốc món ăn", color = Color.DarkGray, fontSize = 14.sp)
                                    Text(String.format(Locale("vi", "VN"), "%,.0f đ", order.totalDishPrice), fontSize = 14.sp)
                                }

                                // Phí vận chuyển
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Phí giao hàng", color = Color.DarkGray, fontSize = 14.sp)
                                    Text("+ ${String.format(Locale("vi", "VN"), "%,.0f đ", order.shippingFee)}", fontSize = 14.sp)
                                }

                                // Hiển thị chi tiết Voucher giảm giá (Nếu đơn hàng có áp dụng mã)
                                if (!order.appliedPromotionId.isNullOrBlank() || order.promotionDiscount > 0) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text("Khuyến mãi áp dụng", color = Color.DarkGray, fontSize = 14.sp)
                                            val tenVoucher = order.appliedPromotionTitle.ifBlank { "Mã giảm giá" }
                                            Text(text = "($tenVoucher)", fontSize = 12.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Medium)
                                        }
                                        Text(
                                            text = "- ${String.format(Locale("vi", "VN"), "%,.0f đ", order.promotionDiscount)}",
                                            fontSize = 14.sp,
                                            color = Color.Red,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                                // Số tiền cuối cùng thực tế khách phải trả
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Tổng thanh toán thực tế", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(
                                        text = String.format(Locale("vi", "VN"), "%,.0f đ", order.totalPrice),
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2159BC)
                                    )
                                }
                            }
                        }
                    }

                    // Khoảng đệm an toàn dưới cùng danh sách
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}