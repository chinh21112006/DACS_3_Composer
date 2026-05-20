package com.example.dacs_3_composer.ui.shipper.dashboard.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.dacs_3_composer.data.model.Order
import com.example.dacs_3_composer.data.model.OrderItem
import com.example.dacs_3_composer.data.model.OrderStatus
import com.example.dacs_3_composer.ui.shipper.dashboard.detail.components.*

@Composable
fun ShipperOrderDetailScreen(
    orderId: String,
    onBackClick: () -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    // Lấy UID của Shipper đang đăng nhập hiện tại
    val currentShipperId = remember { auth.currentUser?.uid ?: "" }

    var orderState by remember { mutableStateOf<Order?>(null) }
    var isUpdating by remember { mutableStateOf(false) }

    LaunchedEffect(orderId) {
        if (orderId.isBlank()) return@LaunchedEffect

        val docRef = firestore.collection("orders").document(orderId)
        docRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) {
                return@addSnapshotListener
            }

            try {
                val id = snapshot.id
                val time = snapshot.getString("time") ?: ""
                val status = snapshot.getString("status") ?: "PENDING"

                val totalDishPrice = snapshot.getDouble("totalDishPrice") ?: (snapshot.getLong("totalDishPrice")?.toDouble() ?: 0.0)
                val totalPrice = snapshot.getDouble("totalPrice") ?: (snapshot.getLong("totalPrice")?.toDouble() ?: 0.0)

                val userId = snapshot.getString("userId") ?: ""
                val customerName = snapshot.getString("customerName") ?: ""
                val customerPhone = snapshot.getString("customerPhone") ?: ""
                val customerAddress = snapshot.getString("customerAddress") ?: ""

                val restaurantId = snapshot.getString("restaurantId") ?: ""
                val restaurantName = snapshot.getString("restaurantName") ?: ""
                val shipperId = snapshot.getString("shipperId") ?: ""

                val itemsRaw = snapshot.get("items") as? List<Map<String, Any>>
                val parsedItems = itemsRaw?.map { map ->
                    OrderItem(
                        dishId = map["dishId"] as? String ?: "",
                        name = map["name"] as? String ?: "",
                        quantity = (map["quantity"] as? Long)?.toInt() ?: 0,
                        price = (map["price"] as? Double) ?: ((map["price"] as? Long)?.toDouble() ?: 0.0)
                    )
                } ?: emptyList()

                orderState = Order(
                    id = id, time = time, status = status,
                    totalDishPrice = totalDishPrice, totalPrice = totalPrice,
                    userId = userId, customerName = customerName,
                    customerPhone = customerPhone, customerAddress = customerAddress,
                    restaurantId = restaurantId, restaurantName = restaurantName,
                    shipperId = shipperId, items = parsedItems
                )

            } catch (e: Exception) {
                android.util.Log.e("FIRESTORE_MAPPING_ERROR", "Phân tích dữ liệu thất bại: ", e)
            }
        }
    }

    val order = orderState

    if (order == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF2563EB))
        }
    } else {
        Scaffold(
            topBar = { OrderDetailTopBar(onBackClick = onBackClick) },
            bottomBar = {
                // Chỉ xử lý hiển thị thanh điều khiển nếu đơn chưa hoàn thành hoặc chưa bị hủy
                if (order.status != OrderStatus.COMPLETED.name && order.status != OrderStatus.CANCELLED.name) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        // KỊCH BẢN 1: Đơn hàng chưa có ai nhận (shipperId trống) -> Hiện nút "NHẬN ĐƠN"
                        if (order.shipperId.isBlank()) {
                            Button(
                                onClick = {
                                    isUpdating = true
                                    firestore.collection("orders").document(orderId)
                                        .update("shipperId", currentShipperId) // Đóng dấu chủ quyền cho tài xế
                                        .addOnSuccessListener { isUpdating = false }
                                        .addOnFailureListener { isUpdating = false }
                                },
                                enabled = !isUpdating,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.AddCard, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Nhận giao đơn hàng này", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // KỊCH BẢN 2: Đơn hàng đã được nhận bởi chính Shipper này
                        else if (order.shipperId == currentShipperId) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Nút "Đã lấy hàng": Chỉ sáng lên khi trạng thái đơn là ACCEPTED (Quán làm xong, đang đợi shipper lấy)
                                Button(
                                    onClick = {
                                        isUpdating = true
                                        firestore.collection("orders").document(orderId)
                                            .update("status", OrderStatus.SHIPPING.name) // Chuyển nấc sang ĐANG GIAO
                                            .addOnSuccessListener { isUpdating = false }
                                            .addOnFailureListener { isUpdating = false }
                                    },
                                    enabled = !isUpdating && order.status == OrderStatus.ACCEPTED.name,
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFEFF6FF),
                                        contentColor = Color(0xFF2563EB),
                                        disabledContainerColor = Color(0xFFF1F5F9),
                                        disabledContentColor = Color.Gray
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (order.status != OrderStatus.ACCEPTED.name) "Đã lấy món" else "Đã lấy hàng",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Nút "Xác nhận giao xong": Chỉ sáng lên sau khi tài xế đã nhấn nút "Đã lấy hàng" (status sang SHIPPING)
                                Button(
                                    onClick = {
                                        isUpdating = true
                                        firestore.collection("orders").document(orderId)
                                            .update("status", OrderStatus.COMPLETED.name)
                                            .addOnSuccessListener {
                                                isUpdating = false
                                                onBackClick() // Hoàn thành xong tự động trả về màn hình danh sách đơn
                                            }
                                            .addOnFailureListener { isUpdating = false }
                                    },
                                    enabled = !isUpdating && order.status == OrderStatus.SHIPPING.name,
                                    modifier = Modifier.weight(1.2f).height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.LocalShipping, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isUpdating) "Đang xử lý..." else "Xác nhận giao xong",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // KỊCH BẢN 3: Đơn hàng đã bị một Shipper khác nhanh tay nhận trước
                        else {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "🔒 Đơn hàng đã có tài xế khác tiếp nhận.",
                                    color = Color(0xFFD32F2F),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(12.dp).align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            },
            containerColor = Color(0xFFF8F9FA)
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                item { MapRouteSection() }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CustomerInfoCard(
                            customerName = order.customerName,
                            customerPhone = order.customerPhone,
                            customerAddress = order.customerAddress,
                            restaurantName = order.restaurantName
                        )
                        ItemsBillCard(
                            orderId = order.id,
                            items = order.items,
                            totalPrice = order.totalPrice,
                        )
                        CustomerNoteBox(noteText = "Vui lòng gọi điện trước khi giao hoặc để lại quầy lễ tân nếu không liên lạc được.")
                    }
                }
            }
        }
    }
}