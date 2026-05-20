package com.example.dacs_3_composer.ui.user.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Khuôn dữ liệu cho Voucher
data class VoucherItem(
    val code: String,
    val title: String,
    val description: String,
    val discountAmount: Double
)

// 🌟 Khuôn dữ liệu cho Địa chỉ đã lưu bao gồm cả Tên, SĐT, Địa chỉ
data class SavedAddress(
    val name: String = "",
    val phone: String = "",
    val address: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    onBackClick: () -> Unit
) {
    val cartItems = cartViewModel.cartItems
    val totalDishPrice = cartViewModel.getTotalPrice()

    // --- QUẢN LÝ TRẠNG THÁI (STATES) THÔNG TIN TỪ FIRESTORE ---
    // 🌟 Thêm State quản lý Tên người nhận song hành cùng SĐT và Địa chỉ
    var currentName by remember { mutableStateOf("Chưa cập nhật tên") }
    var currentPhone by remember { mutableStateOf("Chưa có số điện thoại") }
    var currentAddress by remember { mutableStateOf("Chưa có địa chỉ giao hàng. Vui lòng thêm!") }

    // Danh sách địa chỉ cũ lấy trực tiếp từ mảng "savedAddresses" trên Firestore
    var savedAddressesList by remember { mutableStateOf<List<SavedAddress>>(emptyList()) }

    // Gọi Firebase lấy dữ liệu thực tế (Name, Phone, Address) khi màn hình mở ra
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Đọc đồng thời cả 3 trường từ tài liệu user
                        val dbName = document.getString("name") ?: ""
                        val dbPhone = document.getString("phone") ?: ""
                        val dbAddress = document.getString("address") ?: ""

                        if (dbName.isNotBlank()) currentName = dbName
                        if (dbPhone.isNotBlank()) currentPhone = dbPhone
                        if (dbAddress.isNotBlank()) currentAddress = dbAddress

                        // Lấy mảng danh sách địa chỉ cũ lịch sử
                        val rawAddresses = document.get("savedAddresses") as? List<Map<String, String>>
                        if (rawAddresses != null) {
                            savedAddressesList = rawAddresses.map {
                                SavedAddress(
                                    name = it["name"] ?: "",
                                    phone = it["phone"] ?: "",
                                    address = it["address"] ?: ""
                                )
                            }
                        }
                    }
                }
        }
    }

    // Trạng thái hiển thị BottomSheet thay đổi địa chỉ
    var showAddressBottomSheet by remember { mutableStateOf(false) }
    val addressSheetState = rememberModalBottomSheetState()

    // 🌟 Biến tạm lưu dữ liệu khi nhập thông tin mới (Có thêm trường nhập Tên)
    var inputNewName by remember { mutableStateOf("") }
    var inputNewPhone by remember { mutableStateOf("") }
    var inputNewAddress by remember { mutableStateOf("") }

    // --- QUẢN LÝ TRẠNG THÁI SHIP & VOUCHER ---
    var shippingMethod by remember { mutableStateOf("Nhanh") }
    val shippingFee = if (shippingMethod == "Nhanh") 25000.0 else 15000.0

    var selectedVoucher by remember { mutableStateOf<VoucherItem?>(null) }
    var showVoucherBottomSheet by remember { mutableStateOf(false) }
    val voucherSheetState = rememberModalBottomSheetState()

    val availableVouchers = listOf(
        VoucherItem("APPNEW", "Giảm 20k cho bạn mới", "Áp dụng cho mọi đơn hàng", 20000.0),
        VoucherItem("TRASUA30", "Mã tiệc Trà Sữa 30k", "Áp dụng cho hóa đơn từ 0đ", 30000.0),
        VoucherItem("HELLOSUMMER", "Voucher hè rực rỡ 50k", "Giảm thẳng 50.000 đ vào hóa đơn", 50000.0)
    )

    val discount = selectedVoucher?.discountAmount ?: 0.0
    val finalTotal = (totalDishPrice + shippingFee - discount).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Xác nhận đặt hàng", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(modifier = Modifier.fillMaxWidth(), tonalElevation = 8.dp, shadowElevation = 8.dp, color = Color.White) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding()) {
                        Button(
                            onClick = {
                                // Kiểm tra điều kiện bắt buộc phải có địa chỉ mới cho bấm đặt
                                if (currentAddress.contains("Chưa có địa chỉ")) {
                                    // Bạn có thể dùng Toast để nhắc nhở người dùng nhập địa chỉ trước
                                    return@Button
                                }

                                // 🌟 TÌM VÀ THAY THẾ KHỐI LỆNH TRONG NÚT ĐẶT HÀNG:
                                // Tìm đến khối lệnh xử lý sự kiện onClick của nút bấm Button "XÁC NHẬN ĐẶT HÀNG" trong file CartScreen.kt và sửa lại:

                                cartViewModel.placeOrder(
                                    customerName = currentName,
                                    customerPhone = currentPhone,
                                    customerAddress = currentAddress,

                                    // 🚀 LẤY TỰ ĐỘNG KHÔNG CÒN HARDCODE NỮA:
                                    restaurantId = cartViewModel.currentRestaurantId.ifBlank { "RES_DEFAULT" },
                                    restaurantName = cartViewModel.currentRestaurantName.ifBlank { "Cửa hàng đối tác" },

                                    shippingFee = shippingFee,
                                    totalDishPrice = totalDishPrice,
                                    finalTotal = finalTotal,
                                    onSuccess = {
                                        onBackClick()
                                    },
                                    onFailure = { errorMess ->
                                        // Xử lý thông báo lỗi lên màn hình nếu cần
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E56A0)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("XÁC NHẬN ĐẶT HÀNG", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FA))
        ) {
            if (cartItems.isEmpty()) {
                // Màn hình giỏ hàng trống (Giữ nguyên)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // PHẦN 1 CẬP NHẬT: HIỂN THỊ ĐỦ BỘ 3 THÔNG TIN GIAO HÀNG
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Thông tin giao hàng", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

                                    Row(
                                        modifier = Modifier.clickable { showAddressBottomSheet = true },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF1E56A0), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Thay đổi", fontSize = 13.sp, color = Color(0xFF1E56A0), fontWeight = FontWeight.Medium)
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                // 🌟 Dòng hiển thị Tên người nhận
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1E56A0), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = currentName, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                // Dòng hiển thị Số điện thoại
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF1E56A0), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = currentPhone, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                // Dòng hiển thị Địa chỉ
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp).padding(top = 2.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = currentAddress, fontSize = 14.sp, color = Color.DarkGray)
                                }
                            }
                        }
                    }

                    // PHẦN 2: CHỌN PHƯƠNG THỨC SHIP (Giữ nguyên)
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Phương thức giao hàng", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (shippingMethod == "Nhanh") Color(0xFFE8F0FE) else Color(0xFFF1F3F4)).border(1.dp, if (shippingMethod == "Nhanh") Color(0xFF1E56A0) else Color.Transparent, RoundedCornerShape(8.dp)).clickable { shippingMethod = "Nhanh" }.padding(12.dp), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("⚡ Giao nhanh", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (shippingMethod == "Nhanh") Color(0xFF1E56A0) else Color.Black)
                                            Text("25.000 đ", fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }
                                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (shippingMethod == "TietKiem") Color(0xFFE8F0FE) else Color(0xFFF1F3F4)).border(1.dp, if (shippingMethod == "TietKiem") Color(0xFF1E56A0) else Color.Transparent, RoundedCornerShape(8.dp)).clickable { shippingMethod = "TietKiem" }.padding(12.dp), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("🐢 Tiết kiệm", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (shippingMethod == "TietKiem") Color(0xFF1E56A0) else Color.Black)
                                            Text("15.000 đ", fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // PHẦN 3: DANH SÁCH MÓN ĂN (Giữ nguyên)
                    item { Text(text = "Món ăn đã chọn", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(start = 4.dp)) }
                    items(cartItems) { item ->
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(model = item.dish.imageUrl, contentDescription = null, modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = item.dish.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(text = "${String.format("%,.0f", item.dish.price)} đ", fontSize = 13.sp, color = Color(0xFF1E56A0), modifier = Modifier.padding(top = 2.dp))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedIconButton(onClick = { cartViewModel.minusFromCart(item.dish) }, modifier = Modifier.size(28.dp), shape = RoundedCornerShape(6.dp)) {
                                        if (item.quantity == 1) Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Red)
                                        else Text("-", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                    Text(text = "${item.quantity}", modifier = Modifier.padding(horizontal = 6.dp), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    OutlinedIconButton(onClick = { cartViewModel.addToCart(item.dish) }, modifier = Modifier.size(28.dp), shape = RoundedCornerShape(6.dp)) { Text("+", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black) }
                                }
                            }
                        }
                    }

                    // PHẦN 4 & 5: VOUCHER & CHI TIẾT HÓA ĐƠN (Giữ nguyên)
                    item {
                        Card(modifier = Modifier.fillMaxWidth().clickable { showVoucherBottomSheet = true }, colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🎟️ ", fontSize = 18.sp)
                                    if (selectedVoucher == null) {
                                        Text(text = "Thêm Voucher", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                    } else {
                                        Text(text = "Đã áp dụng: ${selectedVoucher!!.title}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                                    }
                                }
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                            }
                        }
                    }

                    item {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(text = "Chi tiết hóa đơn", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Tổng giá món", color = Color.DarkGray, fontSize = 14.sp)
                                    Text("${String.format("%,.0f", totalDishPrice)} đ", fontSize = 14.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Phí giao hàng (${if (shippingMethod == "Nhanh") "Nhanh" else "Tiết kiệm"})", color = Color.DarkGray, fontSize = 14.sp)
                                    Text("+ ${String.format("%,.0f", shippingFee)} đ", fontSize = 14.sp)
                                }
                                if (selectedVoucher != null) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Mã khuyến mãi (${selectedVoucher!!.code})", color = Color.DarkGray, fontSize = 14.sp)
                                        Text("- ${String.format("%,.0f", discount)} đ", fontSize = 14.sp, color = Color.Red)
                                    }
                                }
                                Divider(color = Color(0xFFEEEEEE), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Tổng thanh toán", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(text = "${String.format("%,.0f", finalTotal)} đ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E56A0))
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }

            // ==========================================
            // 🌟 CẬP NHẬT: BOTTOM SHEET THAY ĐỔI ĐỊA CHỈ & HỖ TRỢ LƯU CẢ TÊN
            // ==========================================
            if (showAddressBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showAddressBottomSheet = false },
                    sheetState = addressSheetState,
                    containerColor = Color.White
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(text = "Thay đổi thông tin nhận hàng", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        // 🅰️ ĐỊA CHỈ CŨ: Đọc mảng cấu trúc {name, phone, address} từ Firestore
                        if (savedAddressesList.isNotEmpty()) {
                            item {
                                Text(text = "Chọn từ địa chỉ đã lưu:", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                            }

                            items(savedAddressesList) { info ->
                                val isSelected = currentAddress == info.address && currentPhone == info.phone && currentName == info.name
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, if (isSelected) Color(0xFF1E56A0) else Color(0xFFEEEEEE), RoundedCornerShape(10.dp))
                                        .clickable {
                                            currentName = info.name
                                            currentPhone = info.phone
                                            currentAddress = info.address

                                            // Gọi ViewModel cập nhật bộ ba dữ liệu mặc định lên Firestore
                                            cartViewModel.updateDeliveryInfo(info.name, info.phone, info.address)
                                            showAddressBottomSheet = false
                                        },
                                    colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFE8F0FE) else Color(0xFFFAFAFA))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = "👤 ${info.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "📞 ${info.phone}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "📍 ${info.address}", fontSize = 13.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }

                        // 🅱️ NHẬP THÔNG TIN MỚI HOÀN TOÀN
                        item {
                            Divider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(vertical = 4.dp))
                            Text(text = "Hoặc nhập thông tin mới hoàn toàn:", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                        }

                        // 🌟 THÊM: Ô nhập tên mới
                        item {
                            OutlinedTextField(
                                value = inputNewName,
                                onValueChange = { inputNewName = it },
                                label = { Text("Tên người nhận mới") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF1E56A0))
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = inputNewPhone,
                                onValueChange = { inputNewPhone = it },
                                label = { Text("Số điện thoại mới") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF1E56A0))
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = inputNewAddress,
                                onValueChange = { inputNewAddress = it },
                                label = { Text("Địa chỉ nhận hàng mới") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF1E56A0))
                            )
                        }

                        item {
                            Button(
                                onClick = {
                                    if (inputNewName.isNotBlank() && inputNewPhone.isNotBlank() && inputNewAddress.isNotBlank()) {
                                        currentName = inputNewName
                                        currentPhone = inputNewPhone
                                        currentAddress = inputNewAddress

                                        // 🚀 ĐỒNG BỘ: Đẩy bộ ba lên Firestore (Lưu đè thuộc tính gốc + Thêm vào Array)
                                        cartViewModel.updateDeliveryInfo(inputNewName, inputNewPhone, inputNewAddress)

                                        // Cập nhật nóng danh sách hiển thị trên giao diện tạm thời
                                        savedAddressesList = savedAddressesList + SavedAddress(inputNewName, inputNewPhone, inputNewAddress)

                                        inputNewName = ""
                                        inputNewPhone = ""
                                        inputNewAddress = ""
                                        showAddressBottomSheet = false
                                    }
                                },
                                enabled = inputNewName.isNotBlank() && inputNewPhone.isNotBlank() && inputNewAddress.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E56A0)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Sử dụng thông tin mới này", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }

            // BottomSheet chọn Voucher (Giữ nguyên)
            if (showVoucherBottomSheet) {
                // ==========================================
                // 🎟️ CẬP NHẬT: BOTTOM SHEET CHỌN VOUCHER HOÀN CHỈNH
                // ==========================================
                if (showVoucherBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showVoucherBottomSheet = false },
                        sheetState = voucherSheetState,
                        containerColor = Color.White
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Chọn Mã Khuyến Mãi", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    if (selectedVoucher != null) {
                                        TextButton(onClick = {
                                            selectedVoucher = null
                                            showVoucherBottomSheet = false
                                        }) {
                                            Text("Hủy chọn", color = Color.Red, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Duyệt qua danh sách mã giảm giá có sẵn
                            items(availableVouchers) { voucher ->
                                val isSelected = selectedVoucher?.code == voucher.code

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Color(0xFF4CAF50) else Color(0xFFEEEEEE),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            selectedVoucher = voucher
                                            showVoucherBottomSheet = false // Đóng sheet sau khi chọn thành công
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color(0xFFE8F5E9) else Color(0xFFFAFAFA)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = voucher.code,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1E56A0)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color(0xFFFFE082), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "Giảm ${String.format("%,.0f", voucher.discountAmount)}đ",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFE65100)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = voucher.title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                            Text(text = voucher.description, fontSize = 12.sp, color = Color.Gray)
                                        }

                                        // Hiển thị nút hoặc Icon tích chọn tương ứng trạng thái
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                selectedVoucher = voucher
                                                showVoucherBottomSheet = false
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF4CAF50))
                                        )
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(32.dp)) }
                        }
                    }
                }
            }
        }
    }
}