package com.example.dacs_3_composer.ui.restaurant.profile.settings

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Looper
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dacs_3_composer.R
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
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

@SuppressLint("ClickableViewAccessibility", "MissingPermission", "RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantInfoScreen(
    onBackClick: () -> Unit,
    viewModel: RestaurantInfoViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val detail = viewModel.restaurantDetail

    // --- State cho form ---
    var name by remember(detail) { mutableStateOf(detail?.name ?: "") }
    var email by remember(detail) { mutableStateOf(detail?.email ?: "") }
    var phone by remember(detail) { mutableStateOf(detail?.phone ?: "") }
    var address by remember(detail) { mutableStateOf(detail?.address ?: "Đang tải vị trí...") }
    var openTime by remember(detail) { mutableStateOf(detail?.openTime ?: "") }
    var closeTime by remember(detail) { mutableStateOf(detail?.closeTime ?: "") }
    var description by remember(detail) { mutableStateOf(detail?.description ?: "") }

    // --- State Toạ độ Quán ---
    var restaurantLat by remember(detail) { mutableStateOf(detail?.latitude ?: 16.0748) }
    var restaurantLng by remember(detail) { mutableStateOf(detail?.longitude ?: 108.2240) }

    var isSearchingAddress by remember { mutableStateOf(false) }
    var debounceJob by remember { mutableStateOf<Job?>(null) }

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var selectedAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var selectedCoverUri by remember { mutableStateOf<Uri?>(null) }

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { selectedAvatarUri = it }
    val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { selectedCoverUri = it }

    // Cấu hình Request yêu cầu độ chính xác cao
    val locationRequest = remember {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMaxUpdates(1) // Chỉ cần cập nhật 1 lần duy nhất để lấy vị trí hiện tại
            .build()
    }

    // 🌟 SỬA ĐỔI QUAN TRỌNG: Hàm lấy vị trí kết hợp cả Quét thời gian thực (Fresh Update)
    val getDeviceLocation = {
        isSearchingAddress = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // Nếu có cache, lấy luôn cho nhanh
                restaurantLat = location.latitude
                restaurantLng = location.longitude
                mapViewRef?.controller?.animateTo(GeoPoint(restaurantLat, restaurantLng))
                isSearchingAddress = false
            } else {
                // 🌟 BẬT CHẾ ĐỘ QUÉT ÉP BUỘC: Nếu cache trống, ép hệ thống dò GPS mới ngay lập tức
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            val lastLoc = locationResult.lastLocation
                            if (lastLoc != null) {
                                restaurantLat = lastLoc.latitude
                                restaurantLng = lastLoc.longitude
                                mapViewRef?.controller?.animateTo(GeoPoint(restaurantLat, restaurantLng))
                            } else {
                                Toast.makeText(context, "Môi trường sóng yếu, hãy thử di chuyển ra khu vực thoáng hơn hoặc tự kéo bản đồ!", Toast.LENGTH_LONG).show()
                            }
                            isSearchingAddress = false
                            fusedLocationClient.removeLocationUpdates(this) // Đọc xong gỡ lắng nghe để đỡ tốn pin
                        }
                    },
                    Looper.getMainLooper()
                )
            }
        }.addOnFailureListener {
            isSearchingAddress = false
            Toast.makeText(context, "Không thể đọc cảm biến vị trí!", Toast.LENGTH_SHORT).show()
        }
    }

    // Trình khởi chạy yêu cầu kích hoạt GPS hệ thống
    val gpsSettingLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            getDeviceLocation()
        } else {
            Toast.makeText(context, "BẮT BUỘC phải bật vị trí thiết bị để định vị chính xác nhà hàng!", Toast.LENGTH_LONG).show()
        }
    }

    // Kiểm tra cài đặt phần cứng
    val checkAndEnableGPS = {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            getDeviceLocation()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution.intentSender).build()
                    gpsSettingLauncher.launch(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    sendEx.printStackTrace()
                }
            }
        }
    }

    // Launcher cấp quyền vị trí của ứng dụng
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            checkAndEnableGPS()
        } else {
            Toast.makeText(context, "Ứng dụng cần quyền vị trí để xác định tọa độ cửa hàng!", Toast.LENGTH_LONG).show()
        }
    }

    // Tự động quét vị trí khi khởi chạy màn hình nếu quán chưa cấu hình định vị
    LaunchedEffect(detail) {
        if (detail != null && detail.latitude == null) {
            val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (hasFineLocation) {
                checkAndEnableGPS()
            } else {
                locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
        }
    }

    remember {
        Configuration.getInstance().load(context, context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FF))
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. PHẦN HÌNH NỀN (COVER IMAGE) ---
        Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(selectedCoverUri ?: detail?.coverImage ?: R.drawable.banner1)
                    .crossfade(true).build(),
                contentDescription = "Cover Image",
                modifier = Modifier.fillMaxSize().clickable { coverPicker.launch("image/*") },
                contentScale = ContentScale.Crop
            )

            IconButton(
                onClick = onBackClick,
                modifier = Modifier.padding(16.dp).background(Color.White.copy(alpha = 0.7f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            Box(
                modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape).clickable { coverPicker.launch("image/*") }.padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Text("Sửa nền", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- 2. PHẦN ẢNH ĐẠI DIỆN (AVATAR) ---
        Box(modifier = Modifier.offset(y = (-60).dp)) {
            Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(selectedAvatarUri ?: detail?.avatarUrl ?: R.drawable.banner1)
                        .crossfade(true).build(),
                    contentDescription = "Avatar",
                    modifier = Modifier.size(120.dp).clip(CircleShape).background(Color.White).padding(4.dp).clip(CircleShape).clickable { avatarPicker.launch("image/*") },
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier.size(36.dp).background(Color(0xFF1A73E8), CircleShape).clickable { avatarPicker.launch("image/*") }.padding(8.dp).offset(x = (-2).dp, y = (-2).dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Sửa đại diện", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }

        // --- 3. FORM THÔNG TIN CHI TIẾT ---
        Column(modifier = Modifier.padding(horizontal = 20.dp).offset(y = (-40).dp)) {

            // KHỐI BẢN ĐỒ ĐỊNH VỊ
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text(
                    text = "Vị trí của quán trên bản đồ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF414754),
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.LightGray)
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            MapView(ctx).apply {
                                setMultiTouchControls(true)
                                setBuiltInZoomControls(false)
                                controller.setZoom(17.5)

                                val startPoint = GeoPoint(restaurantLat, restaurantLng)
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
                                    title = "Vị trí chọn"
                                }
                                overlays.add(centerMarker)

                                addMapListener(object : MapListener {
                                    override fun onScroll(event: ScrollEvent?): Boolean {
                                        val center = mapCenter as GeoPoint
                                        centerMarker.position = center
                                        restaurantLat = center.latitude
                                        restaurantLng = center.longitude

                                        debounceJob?.cancel()
                                        debounceJob = coroutineScope.launch {
                                            delay(800)
                                            isSearchingAddress = true
                                            val textAddress = withContext(Dispatchers.IO) {
                                                reverseGeocode(center.latitude, center.longitude)
                                            }
                                            address = textAddress ?: "Vị trí quán ăn tùy chọn"
                                            isSearchingAddress = false
                                        }
                                        return true
                                    }
                                    override fun onZoom(event: ZoomEvent?): Boolean = false
                                })
                                mapViewRef = this
                            }
                        },
                        update = { map ->
                            val pt = GeoPoint(restaurantLat, restaurantLng)
                            if (map.mapCenter.latitude != pt.latitude || map.mapCenter.longitude != pt.longitude) {
                                map.controller.animateTo(pt) // Dùng animateTo để tạo hiệu ứng bay mượt
                            }
                        }
                    )

                    Box(modifier = Modifier.size(12.dp).background(Color.Red, CircleShape).align(Alignment.Center))

                    // NÚT ĐỊNH VỊ GPS HIỆN TẠI ĐÃ ĐƯỢC FIX LỖI CACHE
                    FloatingActionButton(
                        onClick = {
                            val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            if (hasFineLocation) {
                                checkAndEnableGPS()
                            } else {
                                locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .size(40.dp),
                        containerColor = Color.White,
                        contentColor = Color(0xFF1A73E8),
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(2.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Vị trí hiện tại", modifier = Modifier.size(20.dp))
                    }
                }
            }

            if (isSearchingAddress) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), color = Color(0xFF2159BC))
            }

            InfoTextField(label = "Địa chỉ chi tiết của quán", value = address, onValueChange = { address = it }, icon = Icons.Default.LocationOn)
            InfoTextField(label = "Tên nhà hàng", value = name, onValueChange = { name = it }, icon = Icons.Default.Storefront)
            InfoTextField(label = "Email liên hệ", value = email, onValueChange = { email = it }, icon = Icons.Default.Mail)
            InfoTextField(label = "Số điện thoại", value = phone, onValueChange = { phone = it }, icon = Icons.Default.Call)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    InfoTextField(label = "Giờ mở cửa", value = openTime, onValueChange = { openTime = it }, icon = Icons.Default.Schedule, placeholder = "08:00")
                }
                Box(modifier = Modifier.weight(1f)) {
                    InfoTextField(label = "Giờ đóng cửa", value = closeTime, onValueChange = { closeTime = it }, icon = Icons.Default.HistoryToggleOff, placeholder = "22:00")
                }
            }

            InfoTextField(label = "Mô tả quán", value = description, onValueChange = { description = it }, icon = Icons.Default.Description, singleLine = false, minLines = 3)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (restaurantLat == 16.0748 && restaurantLng == 108.2240 && detail?.latitude == null) {
                        Toast.makeText(context, "Vui lòng bấm nút định vị hoặc kéo bản đồ để chọn chính xác địa chỉ quán!", Toast.LENGTH_LONG).show()
                    } else {
                        viewModel.updateRestaurantInfo(
                            name, email, phone, address, restaurantLat, restaurantLng, openTime, closeTime, description, selectedAvatarUri, selectedCoverUri
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.saveSuccess) Color(0xFF008939) else Color(0xFF1A73E8)
                ),
                enabled = !viewModel.isSaving
            ) {
                if (viewModel.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        if (viewModel.saveSuccess) "Đã lưu thành công" else "Lưu thay đổi",
                        fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp
                    )
                }
            }

            LaunchedEffect(viewModel.saveSuccess) {
                if (viewModel.saveSuccess) {
                    delay(2500)
                    viewModel.resetSaveSuccess()
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun InfoTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF414754),
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        OutlinedTextField(
            value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(icon, contentDescription = null, tint = Color(0xFF2159BC)) },
            placeholder = { Text(placeholder, color = Color(0xFF727785)) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFC1C6D6), focusedBorderColor = Color(0xFF005BBF),
                unfocusedContainerColor = Color.White, focusedContainerColor = Color.White
            ),
            singleLine = singleLine, minLines = minLines
        )
    }
}

private fun reverseGeocode(lat: Double, lng: Double): String? {
    return try {
        val url = URL("https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lng&format=json&addressdetails=1")
        val urlConnection = url.openConnection() as HttpURLConnection
        urlConnection.setRequestProperty("User-Agent", "DACS_3_Composer_Restaurant_App")
        val response = urlConnection.inputStream.bufferedReader().use { it.readText() }
        val jsonObject = org.json.JSONObject(response)
        jsonObject.optString("display_name")
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}