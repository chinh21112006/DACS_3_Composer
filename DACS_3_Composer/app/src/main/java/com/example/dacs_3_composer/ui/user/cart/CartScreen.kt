    package com.example.dacs_3_composer.ui.user.cart

    import android.Manifest
    import android.annotation.SuppressLint
    import android.content.Context
    import android.view.MotionEvent
    import android.widget.Toast
    import androidx.activity.compose.rememberLauncherForActivityResult
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.compose.foundation.background
    import androidx.compose.foundation.border
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.items
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.Add
    import androidx.compose.material.icons.filled.ArrowBack
    import androidx.compose.material.icons.filled.Delete
    import androidx.compose.material.icons.filled.DeleteOutline
    import androidx.compose.material.icons.filled.Edit
    import androidx.compose.material.icons.filled.KeyboardArrowRight
    import androidx.compose.material.icons.filled.LocationOn
    import androidx.compose.material.icons.filled.Person
    import androidx.compose.material.icons.filled.Phone
    import androidx.compose.material.icons.filled.Payment
    import androidx.compose.material.icons.filled.Money
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.style.TextOverflow
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.compose.ui.viewinterop.AndroidView
    import coil.compose.AsyncImage
    import com.example.dacs_3_composer.data.model.Promotion
    import com.google.android.gms.location.LocationServices
    import com.google.android.gms.location.Priority
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.firestore.FieldValue
    import com.google.firebase.firestore.FirebaseFirestore
    import com.google.firebase.firestore.IgnoreExtraProperties
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.Job
    import kotlinx.coroutines.delay
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.withContext
    import org.osmdroid.config.Configuration
    import org.osmdroid.events.MapListener
    import org.osmdroid.events.ScrollEvent
    import org.osmdroid.events.ZoomEvent
    import org.osmdroid.util.GeoPoint
    import org.osmdroid.views.MapView
    import org.osmdroid.views.overlay.Marker
    import java.net.HttpURLConnection
    import java.net.URL

    @IgnoreExtraProperties
    data class SavedAddress(
        val name: String = "",
        val phone: String = "",
        val address: String = "",
        val addressDetail: String = "",
        val latitude: Double = 0.0,
        val longitude: Double = 0.0
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CartScreen(
        cartViewModel: CartViewModel,
        onBackClick: () -> Unit,
        onNavigateToPayment: (String, Double) -> Unit
    ) {
        val context = LocalContext.current
        val cartItems = cartViewModel.cartItems
        val totalDishPrice = cartViewModel.getTotalPrice()

        var currentName by remember { mutableStateOf("Chưa cập nhật tên") }
        var currentPhone by remember { mutableStateOf("Chưa có số điện thoại") }
        var currentAddress by remember { mutableStateOf("Chưa có địa chỉ giao hàng. Vui lòng thêm!") }
        var currentAddressDetail by remember { mutableStateOf("") }
        var currentLat by remember { mutableStateOf(16.0748) }
        var currentLng by remember { mutableStateOf(108.2240) }

        var savedAddressesList by remember { mutableStateOf<List<SavedAddress>>(emptyList()) }
        var showMapDialog by remember { mutableStateOf(false) }
        var editingAddressIndex by remember { mutableStateOf<Int?>(null) }
        var addressToEditData by remember { mutableStateOf<SavedAddress?>(null) }

        var deviceLat by remember { mutableStateOf(16.0748) }
        var deviceLng by remember { mutableStateOf(108.2240) }

        val locationLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                try {
                    LocationServices.getFusedLocationProviderClient(context)
                        .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { loc ->
                            if (loc != null) {
                                deviceLat = loc.latitude
                                deviceLng = loc.longitude
                            }
                        }
                } catch (e: SecurityException) { e.printStackTrace() }
            }
        }

        LaunchedEffect(Unit) {
            locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            cartViewModel.fetchPromotionsFromFirebase()

            // Tải danh sách Khuyến mãi thực tế từ Firebase Firestore
            cartViewModel.fetchPromotionsFromFirebase()

            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                FirebaseFirestore.getInstance().collection("users").document(uid).get()
                    .addOnSuccessListener { doc ->
                        if (doc != null && doc.exists()) {
                            currentName = doc.getString("name") ?: "Chưa cập nhật tên"
                            currentPhone = doc.getString("phone") ?: "Chưa có số điện thoại"
                            currentAddress = doc.getString("address") ?: "Chưa có địa chỉ giao hàng. Vui lòng thêm!"
                            currentAddressDetail = doc.getString("addressDetail") ?: ""
                            currentLat = doc.getDouble("latitude") ?: 16.0748
                            currentLng = doc.getDouble("longitude") ?: 108.2240

                            val rawAddresses = doc.get("savedAddresses") as? List<Map<String, Any>>
                            if (rawAddresses != null) {
                                savedAddressesList = rawAddresses.map {
                                    SavedAddress(
                                        name = it["name"] as? String ?: "",
                                        phone = it["phone"] as? String ?: "",
                                        address = it["address"] as? String ?: "",
                                        addressDetail = it["addressDetail"] as? String ?: "",
                                        latitude = (it["latitude"] as? Number)?.toDouble() ?: 0.0,
                                        longitude = (it["longitude"] as? Number)?.toDouble() ?: 0.0
                                    )
                                }
                            }
                        }
                    }
            }
        }

        var showAddressBottomSheet by remember { mutableStateOf(false) }
        val addressSheetState = rememberModalBottomSheetState()

        var shippingMethod by remember { mutableStateOf("Nhanh") }
        val shippingFee = if (shippingMethod == "Nhanh") 25000.0 else 15000.0

        // ✅ PHƯƠNG THỨC THANH TOÁN
        var paymentMethod by remember { mutableStateOf("CASH") } // "CASH" hoặc "ONLINE"

        // Danh sách Khuyến mãi và trạng thái chọn mã từ Firebase
        val listPromotionsFromFirebase = cartViewModel.availablePromotions
        var selectedPromotion by remember { mutableStateOf<Promotion?>(null) }
        var showVoucherBottomSheet by remember { mutableStateOf(false) }
        val voucherSheetState = rememberModalBottomSheetState()

        // Logic tính toán số tiền khuyến mãi dựa trên cấu trúc bảng dữ liệu mới
        val discount = remember(selectedPromotion, totalDishPrice) {
            if (selectedPromotion != null) {
                if (totalDishPrice < selectedPromotion!!.minOrderValue) {
                    0.0 // Tổng tiền không đạt mức chi tiêu tối thiểu của mã
                } else {
                    when (selectedPromotion!!.type) {
                        "percentage" -> {
                            val tinhGiam = (totalDishPrice * selectedPromotion!!.value) / 100
                            if (selectedPromotion!!.maxDiscount > 0) tinhGiam.coerceAtMost(selectedPromotion!!.maxDiscount) else tinhGiam
                        }
                        "fixed" -> selectedPromotion!!.value
                        "free_shipping" -> shippingFee
                        else -> 0.0
                    }
                }
            } else {
                0.0
            }
        }

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
                                    if (currentAddress.contains("Chưa có địa chỉ")) {
                                        Toast.makeText(context, "Vui lòng chọn địa chỉ giao hàng trước!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (selectedPromotion != null && totalDishPrice < selectedPromotion!!.minOrderValue) {
                                        Toast.makeText(context, "Đơn hàng của bạn không còn đủ điều kiện dùng mã này!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    cartViewModel.placeOrder(
                                        customerName = currentName,
                                        customerPhone = currentPhone,
                                        customerAddress = if (currentAddressDetail.isNotBlank()) "$currentAddressDetail, $currentAddress" else currentAddress,
                                        customerLat = currentLat,
                                        customerLng = currentLng,
                                        restaurantId = cartViewModel.currentRestaurantId.ifBlank { "RES_DEFAULT" },
                                        restaurantName = cartViewModel.currentRestaurantName.ifBlank { "Cửa hàng đối tác" },
                                        shippingFee = shippingFee,
                                        totalDishPrice = totalDishPrice,
                                        finalTotal = finalTotal,
                                        appliedPromotionId = selectedPromotion?.id,
                                        appliedPromotionTitle = selectedPromotion?.title ?: "",
                                        promotionDiscount = discount,
                                        paymentMethod = paymentMethod, // ✅ Truyền phương thức thanh toán
                                        onSuccess = { orderId ->
                                            if (paymentMethod == "ONLINE") {
                                                onNavigateToPayment(orderId, finalTotal)
                                            } else {
                                                Toast.makeText(context, "Đặt hàng thành công!", Toast.LENGTH_LONG).show()
                                                cartViewModel.clearCart()
                                                onBackClick()
                                            }
                                        },
                                        onFailure = { error -> Toast.makeText(context, error, Toast.LENGTH_SHORT).show() }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E56A0)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                val buttonText = if (paymentMethod == "ONLINE") "THANH TOÁN TRỰC TUYẾN" else "XÁC NHẬN ĐẶT HÀNG (COD)"
                                Text(buttonText, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color(0xFFF8F9FA))) {
                if (cartItems.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Thông tin giao hàng
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "Thông tin giao hàng", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                        Row(modifier = Modifier.clickable { showAddressBottomSheet = true }, verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF1E56A0), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "Thay đổi / Quản lý", fontSize = 13.sp, color = Color(0xFF1E56A0), fontWeight = FontWeight.Medium)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1E56A0), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = currentName, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF1E56A0), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = currentPhone, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp).padding(top = 2.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            if (currentAddressDetail.isNotBlank()) {
                                                Text(text = "Chi tiết: $currentAddressDetail", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E56A0))
                                                Spacer(modifier = Modifier.height(2.dp))
                                            }
                                            Text(text = currentAddress, fontSize = 14.sp, color = Color.DarkGray)
                                        }
                                    }
                                }
                            }
                        }

                        // ✅ KHU VỰC CHỌN PHƯƠNG THỨC THANH TOÁN
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = "Phương thức thanh toán", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (paymentMethod == "CASH") Color(0xFFE8F0FE) else Color(0xFFF1F3F4))
                                                .border(1.dp, if (paymentMethod == "CASH") Color(0xFF1E56A0) else Color.Transparent, RoundedCornerShape(8.dp))
                                                .clickable { paymentMethod = "CASH" }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Money, contentDescription = null, tint = if (paymentMethod == "CASH") Color(0xFF1E56A0) else Color.Gray)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text("Thanh toán khi nhận hàng (COD)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (paymentMethod == "CASH") Color(0xFF1E56A0) else Color.Black)
                                                Text("Thanh toán bằng tiền mặt khi shipper giao hàng", fontSize = 12.sp, color = Color.Gray)
                                            }
                                            Spacer(modifier = Modifier.weight(1f))
                                            RadioButton(selected = paymentMethod == "CASH", onClick = { paymentMethod = "CASH" })
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (paymentMethod == "ONLINE") Color(0xFFE8F0FE) else Color(0xFFF1F3F4))
                                                .border(1.dp, if (paymentMethod == "ONLINE") Color(0xFF1E56A0) else Color.Transparent, RoundedCornerShape(8.dp))
                                                .clickable { paymentMethod = "ONLINE" }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Payment, contentDescription = null, tint = if (paymentMethod == "ONLINE") Color(0xFF1E56A0) else Color.Gray)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text("Chuyển khoản / Ví điện tử", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (paymentMethod == "ONLINE") Color(0xFF1E56A0) else Color.Black)
                                                Text("Thanh toán an toàn qua PayOS", fontSize = 12.sp, color = Color.Gray)
                                            }
                                            Spacer(modifier = Modifier.weight(1f))
                                            RadioButton(selected = paymentMethod == "ONLINE", onClick = { paymentMethod = "ONLINE" })
                                        }
                                    }
                                }
                            }
                        }

                        // Món ăn đã chọn
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

                        // KHU VỰC CHỌN MÃ KHUYẾN MÃI (PROMOTION)
                        item {
                            Card(modifier = Modifier.fillMaxWidth().clickable { showVoucherBottomSheet = true }, colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "🎟️ ", fontSize = 18.sp)
                                        if (selectedPromotion == null) Text(text = "Thêm mã khuyến mãi", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                        else Text(text = "Áp dụng: ${selectedPromotion!!.code}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                                    }
                                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                                }
                            }
                        }

                        // Chi tiết hóa đơn
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(text = "Chi tiết hóa đơn", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Tổng giá món", color = Color.DarkGray, fontSize = 14.sp)
                                        Text("${String.format("%,.0f", totalDishPrice)} đ", fontSize = 14.sp)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Phí giao hàng", color = Color.DarkGray, fontSize = 14.sp)
                                        Text("+ ${String.format("%,.0f", shippingFee)} đ", fontSize = 14.sp)
                                    }
                                    if (selectedPromotion != null && discount > 0) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Khuyến mãi (${selectedPromotion!!.code})", color = Color.DarkGray, fontSize = 14.sp)
                                            Text("- ${String.format("%,.0f", discount)} đ", fontSize = 14.sp, color = Color.Red)
                                        }
                                    }
                                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("Tổng thanh toán", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text(text = "${String.format("%,.0f", finalTotal)} đ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E56A0))
                                    }
                                }
                            }
                        }
                    }
                }

                // BottomSheets và Dialogs (Giữ nguyên logic cũ)
                if (showAddressBottomSheet) {
                    ModalBottomSheet(onDismissRequest = { showAddressBottomSheet = false }, sheetState = addressSheetState, containerColor = Color.White) {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Thay đổi thông tin nhận hàng", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                TextButton(onClick = { editingAddressIndex = null; addressToEditData = null; showMapDialog = true }) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Thêm mới", fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            if (savedAddressesList.isEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) { Text("Bạn chưa lưu địa chỉ nào.", color = Color.Gray) }
                            } else {
                                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(savedAddressesList.size) { index ->
                                        val info = savedAddressesList[index]
                                        val isSelected = currentAddress == info.address && currentPhone == info.phone && currentName == info.name
                                        Card(
                                            modifier = Modifier.fillMaxWidth().border(1.dp, if (isSelected) Color(0xFF1E56A0) else Color(0xFFEEEEEE), RoundedCornerShape(12.dp)).clickable {
                                                currentName = info.name; currentPhone = info.phone; currentAddress = info.address; currentAddressDetail = info.addressDetail; currentLat = info.latitude; currentLng = info.longitude
                                                cartViewModel.updateDeliveryInfo(info.name, info.phone, info.address, info.addressDetail, info.latitude, info.longitude)
                                                showAddressBottomSheet = false
                                            },
                                            colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFE8F0FE) else Color(0xFFFAFAFA))
                                        ) {
                                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = "👤 ${info.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                    Text(text = "📞 ${info.phone}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                                                    if (info.addressDetail.isNotBlank()) Text(text = "🏠 Chi tiết: ${info.addressDetail}", fontSize = 13.sp, color = Color(0xFF1E56A0), fontWeight = FontWeight.Medium)
                                                    Text(text = "📍 Định vị: ${info.address}", fontSize = 13.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                }
                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    IconButton(onClick = { editingAddressIndex = index; addressToEditData = info; showMapDialog = true }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = Color(0xFF1E56A0), modifier = Modifier.size(18.dp)) }
                                                    IconButton(onClick = {
                                                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@IconButton
                                                        FirebaseFirestore.getInstance().collection("users").document(uid).update("savedAddresses", FieldValue.arrayRemove(info)).addOnSuccessListener {
                                                            savedAddressesList = savedAddressesList.filterIndexed { i, _ -> i != index }
                                                            Toast.makeText(context, "Đã xóa địa chỉ!", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.DeleteOutline, contentDescription = "Xóa", tint = Color.Red, modifier = Modifier.size(18.dp)) }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }

                if (showMapDialog) {
                    CartAddressFormDialog(
                        addressToEdit = addressToEditData, deviceLat = deviceLat, deviceLng = deviceLng,
                        onDismiss = { showMapDialog = false },
                        onSave = { updatedAddress ->
                            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@CartAddressFormDialog
                            val db = FirebaseFirestore.getInstance()
                            if (editingAddressIndex == null) {
                                db.collection("users").document(uid).update("savedAddresses", FieldValue.arrayUnion(updatedAddress)).addOnSuccessListener {
                                    savedAddressesList = savedAddressesList + updatedAddress
                                    showMapDialog = false
                                }
                            } else {
                                val oldAddress = savedAddressesList[editingAddressIndex!!]
                                db.collection("users").document(uid).update("savedAddresses", FieldValue.arrayRemove(oldAddress)).addOnSuccessListener {
                                    db.collection("users").document(uid).update("savedAddresses", FieldValue.arrayUnion(updatedAddress)).addOnSuccessListener {
                                        savedAddressesList = savedAddressesList.mapIndexed { i, item -> if (i == editingAddressIndex) updatedAddress else item }
                                        showMapDialog = false
                                    }
                                }
                            }
                        }
                    )
                }

                if (showVoucherBottomSheet) {
                    ModalBottomSheet(onDismissRequest = { showVoucherBottomSheet = false }, sheetState = voucherSheetState, containerColor = Color.White) {
                        LazyColumn(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            item {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "Chọn Mã Khuyến Mãi", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    if (selectedPromotion != null) {
                                        TextButton(onClick = { selectedPromotion = null; showVoucherBottomSheet = false }) { Text("Hủy chọn", color = Color.Red, fontWeight = FontWeight.Bold) }
                                    }
                                }
                            }

                            items(listPromotionsFromFirebase) { promo ->
                                val isSelected = selectedPromotion?.id == promo.id
                                val duDieuKienNhanma = totalDishPrice >= promo.minOrderValue

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(width = 1.dp, color = if (isSelected) Color(0xFF4CAF50) else Color(0xFFEEEEEE), shape = RoundedCornerShape(10.dp))
                                        .clickable(enabled = duDieuKienNhanma) {
                                            selectedPromotion = promo
                                            showVoucherBottomSheet = false
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color(0xFFE8F5E9) else if (duDieuKienNhanma) Color(0xFFFAFAFA) else Color(0xFFF2F2F2)
                                    )
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = promo.code, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (duDieuKienNhanma) Color(0xFF1E56A0) else Color.Gray)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(modifier = Modifier.background(if (duDieuKienNhanma) Color(0xFFFFE082) else Color(0xFFE0E0E0), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                                    val textGiam = if (promo.type == "percentage") "Giảm ${promo.value}%" else "Giảm ${String.format("%,.0f", promo.value)}đ"
                                                    Text(text = textGiam, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (duDieuKienNhanma) Color(0xFFE65100) else Color.DarkGray)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = promo.title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                            if (promo.description.isNotBlank()) {
                                                Text(text = promo.description, fontSize = 12.sp, color = Color.Gray)
                                            }

                                            if (!duDieuKienNhanma) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Đơn hàng tối thiểu phải đạt từ ${String.format("%,.0f", promo.minOrderValue)}đ",
                                                    fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                        RadioButton(
                                            selected = isSelected,
                                            enabled = duDieuKienNhanma,
                                            onClick = { selectedPromotion = promo; showVoucherBottomSheet = false },
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

    @SuppressLint("MissingPermission", "ClickableViewAccessibility", "RememberReturnType")
    @Composable
    fun CartAddressFormDialog(
        addressToEdit: SavedAddress?, deviceLat: Double, deviceLng: Double,
        onDismiss: () -> Unit, onSave: (SavedAddress) -> Unit
    ) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        var name by remember { mutableStateOf(addressToEdit?.name ?: "") }
        var phone by remember { mutableStateOf(addressToEdit?.phone ?: "") }
        var addressStr by remember { mutableStateOf(addressToEdit?.address ?: "Đang xác định vị trí...") }
        var addressDetail by remember { mutableStateOf(addressToEdit?.addressDetail ?: "") }

        var currentLat by remember { mutableStateOf(addressToEdit?.latitude ?: deviceLat) }
        var currentLng by remember { mutableStateOf(addressToEdit?.longitude ?: deviceLng) }

        var isSearchingAddress by remember { mutableStateOf(false) }
        var debounceJob by remember { mutableStateOf<Job?>(null) }

        remember { Configuration.getInstance().load(context, context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE)) }

        LaunchedEffect(currentLat, currentLng) {
            isSearchingAddress = true
            val textAddress = withContext(Dispatchers.IO) { reverseGeocode(currentLat, currentLng) }
            addressStr = textAddress ?: "Vị trí tùy chọn trên bản đồ"
            isSearchingAddress = false
        }

        AlertDialog(
            onDismissRequest = onDismiss, shape = RoundedCornerShape(24.dp), containerColor = Color.White, modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            title = { Text(if (addressToEdit == null) "Thêm địa chỉ giao hàng mới" else "Sửa địa chỉ đã chọn", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(Color.LightGray, RoundedCornerShape(16.dp))) {
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { ctx ->
                                    MapView(ctx).apply {
                                        setMultiTouchControls(true)
                                        setBuiltInZoomControls(false)
                                        controller.setZoom(17.0)
                                        val startPoint = GeoPoint(currentLat, currentLng)
                                        controller.setCenter(startPoint)
                                        setOnTouchListener { view, event ->
                                            if (event.action == MotionEvent.ACTION_DOWN) view.parent.requestDisallowInterceptTouchEvent(true)
                                            false
                                        }
                                        val centerMarker = Marker(this).apply { position = startPoint; setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM) }
                                        overlays.add(centerMarker)
                                        addMapListener(object : MapListener {
                                            override fun onScroll(event: ScrollEvent?): Boolean {
                                                val center = mapCenter as GeoPoint
                                                centerMarker.position = center
                                                currentLat = center.latitude
                                                currentLng = center.longitude
                                                debounceJob?.cancel()
                                                debounceJob = coroutineScope.launch {
                                                    delay(800)
                                                    isSearchingAddress = true
                                                    val textAddress = withContext(Dispatchers.IO) { reverseGeocode(center.latitude, center.longitude) }
                                                    addressStr = textAddress ?: "Vị trí tùy chọn trên bản đồ"
                                                    isSearchingAddress = false
                                                }
                                                return true
                                            }
                                            override fun onZoom(event: ZoomEvent?): Boolean = false
                                        })
                                    }
                                }
                            )
                            Box(modifier = Modifier.size(12.dp).background(Color.Red, RoundedCornerShape(100.dp)).align(Alignment.Center))
                        }
                    }
                    item {
                        if (isSearchingAddress) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFF2159BC))
                        else Text(text = "📍 Định vị: $addressStr", fontSize = 13.sp, color = Color(0xFF2159BC), fontWeight = FontWeight.Medium)
                    }
                    item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên gợi nhớ (Ví dụ: Nhà riêng, Công ty)") }, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Số điện thoại nhận hàng") }, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = addressDetail, onValueChange = { addressDetail = it }, label = { Text("Số nhà, Tên tòa nhà, Số tầng/Phòng") }, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (phone.isNotBlank() && addressStr.isNotBlank() && name.isNotBlank()) {
                            onSave(SavedAddress(name = name, phone = phone, address = addressStr, addressDetail = addressDetail, latitude = currentLat, longitude = currentLng))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E56A0))
                ) { Text("Lưu lại") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy bỏ", color = Color.Gray) } }
        )
    }

    private fun reverseGeocode(lat: Double, lng: Double): String? {
        return try {
            val url = URL("https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lng&format=json&addressdetails=1")
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.setRequestProperty("User-Agent", "DACS_3_Composer_Customer_App")
            val response = urlConnection.inputStream.bufferedReader().use { it.readText() }
            val jsonObject = org.json.JSONObject(response)
            jsonObject.optString("display_name")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
