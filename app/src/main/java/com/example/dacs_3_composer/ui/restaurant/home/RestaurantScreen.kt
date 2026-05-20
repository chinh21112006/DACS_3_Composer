package com.example.dacs_3_composer.ui.restaurant.home

import RestaurantTopBar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dacs_3_composer.ui.restaurant.home.components.*
import ui.restaurant.home.components.StatsMiniCardRow
import ui.restaurant.home.components.WeeklyRevenueCard

@Composable
fun RestaurantHomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: RestaurantHomeViewModel = viewModel() // Khởi tạo kết nối dữ liệu Firebase
) {
    val topDishesList by homeViewModel.topDishes.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { RestaurantTopBar() }
        item { GreetingSection() }

        // 1. Thẻ tổng doanh thu thực tế từ Firebase
        item { RevenueCard(revenue = homeViewModel.totalRevenue) }

        // 2. Thẻ tổng số đơn hàng thực tế
        item { StatsMiniCardRow(totalOrders = homeViewModel.totalOrdersCount) }

        // 3. Khối biểu đồ cột tuần động
        item { WeeklyRevenueCard(chartWeights = homeViewModel.weeklyChartData) }

        // 4. Tiêu đề danh mục món chạy nhất
        item { TopDishesSectionTitle() }

        // 5. Danh sách tối đa 3 món được tính toán tự động xếp hạng từ cao xuống thấp
        items(topDishesList) { dish ->
            TopSellingDishItem(
                rank = dish.rank,
                dishName = dish.dishName,
                ordersCount = dish.ordersCount,
                imageUrl = dish.imageUrl
            )
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}