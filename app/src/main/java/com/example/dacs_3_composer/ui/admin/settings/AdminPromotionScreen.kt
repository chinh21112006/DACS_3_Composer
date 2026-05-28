package com.example.dacs_3_composer.ui.admin.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.dacs_3_composer.data.model.Promotion
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPromotionScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminPromotionViewModel = viewModel()
) {
    val context = LocalContext.current
    val promotions by viewModel.promotions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedFilter by remember { mutableStateOf("Tất cả") }
    var showAddDialog by remember { mutableStateOf(false) }

    // Lắng nghe thông báo từ ViewModel
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val filteredPromotions = when (selectedFilter) {
        "Đang chạy" -> promotions.filter { it.status == "active" }
        "Hết hạn" -> promotions.filter { it.status == "expired" }
        else -> promotions
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Quản lý Khuyến mãi", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Chiến dịch ưu đãi", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("Thiết lập và theo dõi các chương trình khuyến mãi", fontSize = 13.sp, color = Color.Gray)
                        }
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2159BC)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AddCircle, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Tạo mới", fontSize = 14.sp)
                        }
                    }
                }

                // 1. Flash Sale Section
                item {
                    FeaturedPromotionCard()
                }

                // 2. Banner Section
                item {
                    PromotionBannerCard()
                }

                // 3. Filter Tabs
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Tất cả", "Đang chạy", "Hết hạn").forEach { filter ->
                            FilterChip(
                                label = filter,
                                isSelected = selectedFilter == filter,
                                onClick = { selectedFilter = filter }
                            )
                        }
                    }
                }

                // 4. Danh sách Voucher
                if (isLoading) {
                    item { Box(Modifier.fillMaxWidth(), Alignment.Center) { CircularProgressIndicator(color = Color(0xFF2159BC)) } }
                } else if (filteredPromotions.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                            Text("Không có mã giảm giá nào", color = Color.Gray)
                        }
                    }
                } else {
                    items(filteredPromotions, key = { it.id }) { promotion ->
                        VoucherItemCard(
                            promotion = promotion,
                            onDelete = { viewModel.deletePromotion(promotion.id) },
                            onToggle = { viewModel.toggleStatus(promotion.id, promotion.status) }
                        )
                    }
                }
            }

            // Dialog Form tạo mới mã khuyến mãi
            if (showAddDialog) {
                AddPromotionDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { newPromotion ->
                        viewModel.addPromotion(newPromotion)
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPromotionDialog(
    onDismiss: () -> Unit,
    onConfirm: (Promotion) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("percentage") } // percentage, fixed, free_shipping
    var value by remember { mutableStateOf("") }
    var maxDiscount by remember { mutableStateOf("") }
    var minOrderValue by remember { mutableStateOf("") }
    var usageLimit by remember { mutableStateOf("") }
    var daysCount by remember { mutableStateOf("7") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tạo mã ưu đãi mới", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2159BC)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Mã Voucher (Ví dụ: GIAM30K)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tiêu đề ưu đãi") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Text("Hình thức giảm giá:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val types = listOf("percentage" to "%", "fixed" to "VNĐ", "free_shipping" to "Ship")
                    types.forEach { (t, label) ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { type = t },
                            color = if (type == t) Color(0xFF2159BC) else Color(0xFFF1F3F4),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = if (type == t) Color.White else Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (type != "free_shipping") {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) value = it },
                        label = { Text(if (type == "percentage") "Giá trị giảm (%)" else "Số tiền giảm (VNĐ)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                if (type == "percentage") {
                    OutlinedTextField(
                        value = maxDiscount,
                        onValueChange = { if (it.all { char -> char.isDigit() }) maxDiscount = it },
                        label = { Text("Giảm tối đa (VNĐ)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = minOrderValue,
                    onValueChange = { if (it.all { char -> char.isDigit() }) minOrderValue = it },
                    label = { Text("Đơn tối thiểu (VNĐ)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = usageLimit,
                    onValueChange = { if (it.all { char -> char.isDigit() }) usageLimit = it },
                    label = { Text("Giới hạn lượt dùng (0 = không giới hạn)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = daysCount,
                    onValueChange = { if (it.all { char -> char.isDigit() }) daysCount = it },
                    label = { Text("Số ngày có hiệu lực") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (code.isNotBlank()) {
                        val calendar = Calendar.getInstance()
                        val startDate = Timestamp(calendar.time)
                        calendar.add(Calendar.DAY_OF_YEAR, daysCount.toIntOrNull() ?: 7)
                        val endDate = Timestamp(calendar.time)

                        val newPromotion = Promotion(
                            code = code.uppercase().trim(),
                            title = title.trim(),
                            type = type,
                            value = value.toDoubleOrNull() ?: 0.0,
                            maxDiscount = maxDiscount.toDoubleOrNull() ?: 0.0,
                            minOrderValue = minOrderValue.toDoubleOrNull() ?: 0.0,
                            usageCount = 0L,
                            usageLimit = usageLimit.toLongOrNull() ?: 0L,
                            startDate = startDate,
                            endDate = endDate,
                            status = "active",
                            description = title.trim()
                        )
                        onConfirm(newPromotion)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2159BC)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Xác nhận")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = Color.Gray)
            }
        }
    )
}

@Composable
fun FeaturedPromotionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2159BC).copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(color = Color(0xFF2159BC), shape = CircleShape) {
                    Text("ĐANG DIỄN RA", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Text(" 14:00 - 16:00 Hôm nay", fontSize = 12.sp, color = Color.Gray)
                }
            }
            
            Spacer(Modifier.height(12.dp))
            Text("Flash Sale: Giờ vàng giá sốc", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2159BC))
            Text("Áp dụng cho 12 món ăn bán chạy nhất trên toàn hệ thống", fontSize = 13.sp, color = Color.Gray)
            
            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatSmallCard("Lượt đặt", "428", Color(0xFF40E56C), Modifier.weight(1f))
                StatSmallCard("Doanh thu", "12.5M", Color(0xFFFFB950), Modifier.weight(1f))
                StatSmallCard("Tỉ lệ chốt", "84%", Color(0xFF2159BC), Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun PromotionBannerCard() {
    Card(
        modifier = Modifier.height(140.dp).fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1504674900247-0877df9cc836?q=80&w=1000",
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.7f)))))
            Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                Text("BANNER QUẢNG CÁO", color = Color(0xFFFFB950), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Mừng đại lễ - Giảm trọn thực đơn 20% cho mọi đơn hàng", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) Color(0xFF2159BC) else Color(0xFFE8EAED),
        shape = CircleShape
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Color(0xFF727785),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun StatSmallCard(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Color.White, shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 10.sp, color = Color.Gray)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
fun VoucherItemCard(promotion: Promotion, onDelete: () -> Unit, onToggle: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val expiryDate = promotion.endDate?.toDate()?.let { dateFormat.format(it) } ?: "N/A"
    val progressValue = if (promotion.usageLimit > 0) promotion.usageCount.toFloat() / promotion.usageLimit else 0f
    val usageLimitDisplay = if (promotion.usageLimit == 0L) "∞" else promotion.usageLimit.toString()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).background(Color(0xFFE8F0FE), RoundedCornerShape(12.dp)), Alignment.Center) {
                Icon(Icons.Default.ConfirmationNumber, null, tint = Color(0xFF2159BC), modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(promotion.code, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2159BC))
                Text(
                    text = when(promotion.type) {
                        "percentage" -> "Giảm ${promotion.value.toInt()}% (Tối đa ${promotion.maxDiscount.toInt()} VNĐ)"
                        "fixed" -> "Giảm ${promotion.value.toInt()} VNĐ cho đơn từ ${promotion.minOrderValue.toInt()} VNĐ"
                        "free_shipping" -> "Miễn phí vận chuyển (Đơn từ ${promotion.minOrderValue.toInt()} VNĐ)"
                        else -> promotion.title.ifEmpty { "Ưu đãi đặc biệt" }
                    },
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                
                Spacer(Modifier.height(8.dp))
                // Progress Bar
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${promotion.usageCount}/$usageLimitDisplay lượt", fontSize = 10.sp, color = Color.Gray)
                        Text("Hạn: $expiryDate", fontSize = 10.sp, color = Color.Gray)
                    }
                    LinearProgressIndicator(
                        progress = { progressValue },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = if (progressValue > 0.8f) Color(0xFFFFB950) else Color(0xFF2159BC),
                        trackColor = Color(0xFFF1F3F4)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Row {
                    IconButton(onClick = onToggle) { Icon(if (promotion.status == "active") Icons.Default.Pause else Icons.Default.PlayArrow, null, Modifier.size(20.dp), tint = Color.Gray) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, Modifier.size(20.dp), tint = Color(0xFFFFB4AB)) }
                }
            }
        }
    }
}
