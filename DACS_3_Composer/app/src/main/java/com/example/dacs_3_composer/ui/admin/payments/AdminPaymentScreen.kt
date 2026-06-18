package com.example.dacs_3_composer.ui.admin.payments

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs_3_composer.data.model.Payment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPaymentScreen() {
    var payments by remember { mutableStateOf<List<Payment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("payments")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    payments = snapshot.toObjects(Payment::class.java)
                }
                isLoading = false
            }
    }

    val filteredPayments = payments.filter { 
        it.orderId.contains(searchQuery, ignoreCase = true) || 
        it.transactionId?.contains(searchQuery, ignoreCase = true) == true 
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý giao dịch", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF8F9FA))) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Tìm theo mã đơn hoặc mã GD") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2159BC))
                }
            } else if (filteredPayments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không tìm thấy giao dịch nào.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredPayments) { payment ->
                        AdminPaymentItem(payment)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPaymentItem(payment: Payment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Đơn: #${payment.orderId.takeLast(8).uppercase()}", fontWeight = FontWeight.Bold, color = Color(0xFF191C1D))
                StatusChip(status = payment.paymentStatus)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "Số tiền", fontSize = 12.sp, color = Color.Gray)
                    Text(text = "${String.format(Locale.getDefault(), "%,.0f", payment.amount)}đ", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2159BC))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Thời gian", fontSize = 12.sp, color = Color.Gray)
                    Text(text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(payment.createdAt)), fontSize = 13.sp)
                }
            }
            if (!payment.transactionId.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFF1F3F4))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Mã giao dịch PayOS: ${payment.transactionId}", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val color = when(status) {
        "SUCCESS" -> Color(0xFF2ECC71)
        "PENDING" -> Color(0xFFE67E22)
        else -> Color(0xFFDC3545)
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = if (status == "SUCCESS") "Thành công" else if (status == "PENDING") "Chờ xử lý" else "Thất bại",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
