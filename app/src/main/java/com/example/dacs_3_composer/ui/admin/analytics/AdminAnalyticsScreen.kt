package com.example.dacs_3_composer.ui.admin.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dacs_3_composer.ui.admin.analytics.components.*

@Composable
fun AdminAnalyticsScreen(
    viewModel: AdminAnalyticsViewModel = viewModel()
) {
    // Thu thập dữ liệu realtime tự động đẩy về từ Firestore backend
    val analyticsData by viewModel.analyticsState.collectAsState()

    Scaffold(
        topBar = { AnalyticsTopBar() },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Đổ dữ liệu thật vào các ô lưới thống kê (Người dùng, Nhà hàng, Shipper, Đơn hàng...)
            item {
                OverviewStatsGrid(data = analyticsData)
            }

            // 2. Đổ dữ liệu mảng doanh thu thực tính toán từ Firebase lên biểu đồ cột
            item {
                RevenueChartCard(revenueDays = analyticsData.weeklyRevenue)
            }

            // 3. Khối thông tin gợi ý chuyên sâu dưới đáy
            item { DeepInsightCard() }
        }
    }
}