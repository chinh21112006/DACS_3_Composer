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
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: AdminAnalyticsViewModel = viewModel()
) {
    val adminInfo by viewModel.adminInfo.collectAsState()

    Scaffold(
        topBar = { 
            AnalyticsTopBar(
                name = adminInfo?.name ?: "Admin",
                avatarUrl = adminInfo?.avatarUrl ?: "",
                onChatClick = onChatClick,
                onAvatarClick = onProfileClick
            ) 
        },
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

            // 1. Ô lưới 4 chỉ số thống kê
            item { OverviewStatsGrid() }

            // 2. Thẻ biểu đồ doanh thu Tuần / Tháng
            item { RevenueChartCard() }

            // 3. Tab danh mục xếp hạng (Nhà hàng, Món ăn, Shipper)
            item { TopRankingSection() }

            // 4. Khối gợi ý vận hành thông minh dưới đáy
            item { DeepInsightCard() }
        }
    }
}