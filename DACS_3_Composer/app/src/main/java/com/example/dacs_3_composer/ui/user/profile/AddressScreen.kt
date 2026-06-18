package com.example.dacs_3_composer.ui.user.profile

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dacs_3_composer.data.model.UserAddress
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddressViewModel = viewModel()
) {
    val context = LocalContext.current
    val addresses by viewModel.addressList.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingAddress by remember { mutableStateOf<UserAddress?>(null) }

    // Tọa độ GPS thực tế của thiết bị khách hàng (Mặc định ban đầu là trung tâm Đà Nẵng)
    var deviceLat by remember { mutableStateOf(16.0748) }
    var deviceLng by remember { mutableStateOf(108.2240) }

    // 🚀 LỰA CHỌN KHỞI TẠO XIN QUYỀN VỊ TRÍ TỰ ĐỘNG
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            // Nếu được cấp quyền thành công, tiến hành lấy GPS ngay lập tức
            try {
                val client = LocationServices.getFusedLocationProviderClient(context)
                client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            deviceLat = location.latitude
                            deviceLng = location.longitude
                        }
                    }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(context, "Ứng dụng cần quyền vị trí để định vị nơi giao hàng chính xác!", Toast.LENGTH_LONG).show()
        }
    }

    // Tự động kích hoạt xin quyền ngay khi màn hình này vừa được mở lên
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                title = { Text("Địa chỉ đã lưu", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingAddress = null
                    showDialog = true
                },
                containerColor = Color(0xFF2159BC),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Thêm mới", fontWeight = FontWeight.Medium)
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(paddingValues)
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF2159BC))
            } else if (addresses.isEmpty()) {
                Text(text = "Bạn chưa lưu địa chỉ nào.", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(addresses) { address ->
                        AddressItemCard(
                            address = address,
                            onEditClick = {
                                editingAddress = address
                                showDialog = true
                            },
                            onDeleteClick = { viewModel.deleteAddress(address) }
                        )
                    }
                }
            }
        }

        if (showDialog) {
            AddressFormDialog(
                addressToEdit = editingAddress,
                // Truyền tọa độ thiết bị đã quét được xuống để bản đồ lấy làm tâm điểm
                deviceLat = deviceLat,
                deviceLng = deviceLng,
                onDismiss = { showDialog = false },
                onSave = { updatedAddress ->
                    if (editingAddress == null) {
                        viewModel.addAddress(updatedAddress) { showDialog = false }
                    } else {
                        viewModel.updateAddress(editingAddress!!, updatedAddress) { showDialog = false }
                    }
                }
            )
        }
    }
}

@Composable
fun AddressItemCard(
    address: UserAddress,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFE8F0FE), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF2159BC))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (address.name.isNotBlank()) address.name else "Địa chỉ cá nhân",
                    fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (address.phone.isNotBlank()) {
                    Text(text = "SĐT: ${address.phone}", fontSize = 14.sp, color = Color(0xFF555555))
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(text = address.address, fontSize = 13.sp, color = Color.Gray, lineHeight = 18.sp)
                if (address.addressDetail.isNotBlank()) {
                    Text(
                        text = "Chi tiết: ${address.addressDetail}",
                        fontSize = 12.sp,
                        color = Color(0xFF2159BC),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF2159BC),
                    modifier = Modifier.size(20.dp).clickable { onEditClick() }
                )
                Icon(
                    imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFC0392B),
                    modifier = Modifier.size(20.dp).clickable { onDeleteClick() }
                )
            }
        }
    }
}

@SuppressLint("MissingPermission", "ClickableViewAccessibility", "RememberReturnType")
@Composable
fun AddressFormDialog(
    addressToEdit: UserAddress?,
    deviceLat: Double,
    deviceLng: Double,
    onDismiss: () -> Unit,
    onSave: (UserAddress) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf(addressToEdit?.name ?: "") }
    var phone by remember { mutableStateOf(addressToEdit?.phone ?: "") }
    var addressStr by remember { mutableStateOf(addressToEdit?.address ?: "Đang xác định vị trí...") }
    var addressDetail by remember { mutableStateOf(addressToEdit?.addressDetail ?: "") }

    // 🌟 QUYẾT ĐỊNH ĐIỂM KHỞI ĐẦU: Nếu là sửa đơn thì lấy tọa độ cũ, nếu thêm mới thì dùng định vị GPS máy khách vừa lấy được
    var currentLat by remember { mutableStateOf(addressToEdit?.latitude ?: deviceLat) }
    var currentLng by remember { mutableStateOf(addressToEdit?.longitude ?: deviceLng) }

    var isSearchingAddress by remember { mutableStateOf(false) }
    var debounceJob by remember { mutableStateOf<Job?>(null) }

    remember {
        Configuration.getInstance().load(context, context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE))
    }

    // Tự động chạy dịch chữ cho tọa độ ban đầu của bản đồ
    LaunchedEffect(currentLat, currentLng) {
        isSearchingAddress = true
        val textAddress = withContext(Dispatchers.IO) {
            reverseGeocode(currentLat, currentLng)
        }
        addressStr = textAddress ?: "Vị trí tùy chọn trên bản đồ"
        isSearchingAddress = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        title = { Text(if (addressToEdit == null) "Thêm địa chỉ giao hàng" else "Sửa địa chỉ", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color.LightGray, RoundedCornerShape(16.dp))
                    ) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                MapView(ctx).apply {
                                    setMultiTouchControls(true)
                                    setBuiltInZoomControls(false)
                                    controller.setZoom(17.0) // Tăng độ phóng to lên một chút nhìn cho rõ nhà cửa

                                    val startPoint = GeoPoint(currentLat, currentLng)
                                    controller.setCenter(startPoint)

                                    setOnTouchListener { view, event ->
                                        when (event.action) {
                                            MotionEvent.ACTION_DOWN -> view.parent.requestDisallowInterceptTouchEvent(true)
                                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.parent.requestDisallowInterceptTouchEvent(false)
                                        }
                                        false
                                    }

                                    val centerMarker = Marker(this).apply {
                                        position = startPoint
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                        title = "Vị trí giao đồ ăn"
                                    }
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
                                                val textAddress = withContext(Dispatchers.IO) {
                                                    reverseGeocode(center.latitude, center.longitude)
                                                }
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
                        Box(
                            modifier = Modifier.size(12.dp).background(Color.Red, RoundedCornerShape(100.dp)).align(Alignment.Center)
                        )
                    }
                }

                item {
                    if (isSearchingAddress) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFF2159BC))
                    } else {
                        Text(text = "📍 Địa chỉ định vị: $addressStr", fontSize = 13.sp, color = Color(0xFF2159BC), fontWeight = FontWeight.Medium)
                    }
                }

                item {
                    OutlinedTextField(
                        value = addressDetail, onValueChange = { addressDetail = it },
                        label = { Text("Số nhà, Tên cổng, Số tầng/Phòng (Tùy chọn)") },
                        shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Tên người nhận (Ví dụ: Nhà riêng, Công ty)") },
                        shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = phone, onValueChange = { phone = it },
                        label = { Text("Số điện thoại nhận hàng") },
                        shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (phone.isNotBlank() && addressStr.isNotBlank()) {
                        onSave(
                            UserAddress(
                                name = name,
                                phone = phone,
                                address = addressStr,
                                addressDetail = addressDetail,
                                latitude = currentLat,
                                longitude = currentLng
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2159BC))
            ) {
                Text("Lưu địa chỉ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy bỏ", color = Color.Gray) }
        }
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