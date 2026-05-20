package com.example.dacs_3_composer.ui.admin.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.dacs_3_composer.ui.admin.customer.components.*

@Composable
fun AdminCustomerScreen(
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { CustomerTopBar() },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Thanh tìm kiếm
            item { CustomerSearchBar() }

            // 2. Khối thẻ thống kê
            item { CustomerStatsRow() }

            // 3. Danh sách khách hàng mẫu theo bản vẽ
            item {
                CustomerCardItem(
                    name = "Nguyễn Anh Tuấn",
                    phone = "0908 123 456",
                    orderCount = "24 đơn",
                    totalSpent = "4,250,000đ",
                    status = "ACTIVE"
                )
            }

            item {
                CustomerCardItem(
                    name = "Trần Thị Hoa",
                    phone = "0912 987 654",
                    orderCount = "08 đơn",
                    totalSpent = "1,120,000đ",
                    status = "LOCKED"
                )
            }

            item {
                CustomerCardItem(
                    name = "Lê Văn Việt",
                    phone = "0983 456 789",
                    orderCount = "52 đơn",
                    totalSpent = "12,890,000đ",
                    status = "ACTIVE"
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}