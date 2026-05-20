package com.example.dacs_3_composer.ui.admin

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dacs_3_composer.ui.admin.analytics.AdminAnalyticsScreen
import com.example.dacs_3_composer.ui.admin.category.AdminCategoryScreen
import com.example.dacs_3_composer.ui.admin.complaint.AdminComplaintScreen
// Import thêm các màn hình Shipper/Khách hàng nếu bạn đã tạo file riêng, hoặc tạm thời dùng Box thay thế:
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import com.example.dacs_3_composer.ui.admin.customer.AdminCustomerScreen
import com.example.dacs_3_composer.ui.admin.shipper.AdminShipperScreen

@Composable
fun MainRouteContainerAdmin(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    // Danh sách 5 mục hiển thị dưới thanh BottomBar điều hướng của Super Admin giống hệt ảnh mẫu
    val navigationItems = listOf(
        NavigationAdmin.Overview,
        NavigationAdmin.Orders,
        NavigationAdmin.Shippers,
        NavigationAdmin.Categories,
        NavigationAdmin.Customers
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                navigationItems.forEach { item ->
                    val isSelected = currentRoute == item.route

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color(0xFF2159BC), // Màu xanh chủ đạo của app giống nút nhấn
                            indicatorColor = Color(0xFF2159BC),
                            unselectedIconColor = Color(0xFFC4C7C5),
                            unselectedTextColor = Color(0xFF727785)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavigationAdmin.Overview.route, // Màn hình mặc định ban đầu là Báo cáo & Thống kê
            modifier = Modifier.padding(paddingValues)
        ) {
            // Tab 1: Báo cáo & Thống kê
            composable(NavigationAdmin.Overview.route) {
                AdminAnalyticsScreen()
            }

            // Tab 2: Đơn hàng & Xử lý Khiếu nại
            composable(NavigationAdmin.Orders.route) {
                AdminComplaintScreen()
            }

            // Tab 3: Quản lý Shipper (Nhãn hiển thị UI: Cửa hàng)
            composable(NavigationAdmin.Shippers.route) {
                AdminShipperScreen()

            }

            // Tab 4: Quản lý Danh mục chung (Nhãn hiển thị UI: Thực đơn)
            composable(NavigationAdmin.Categories.route) {
                AdminCategoryScreen()
            }

            // Tab 5: Quản lý Khách hàng (Nhãn hiển thị UI: Cá nhân)
            composable(NavigationAdmin.Customers.route) {
                AdminCustomerScreen()
            }
        }
    }
}