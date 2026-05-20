package com.example.dacs_3_composer.ui.shipper

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dacs_3_composer.ui.shipper.dashboard.ShipperDashboardScreen
import com.example.dacs_3_composer.ui.shipper.dashboard.ShipperViewModel
import com.example.dacs_3_composer.ui.shipper.dashboard.detail.ShipperOrderDetailScreen
import com.example.dacs_3_composer.ui.shipper.orders.ShipperOrdersScreen
import com.example.dacs_3_composer.ui.shipper.profile.ShipperProfileScreen

@Composable
fun MainRouteContainerShipper(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    // Khởi tạo ViewModel dùng chung cho luồng Shipper ở cấp Container
    val shipperViewModel: ShipperViewModel = viewModel()

    val navigationItems = listOf(
        NavigationShipper.Dashboard,
        NavigationShipper.Orders,
        NavigationShipper.Account
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Check ẩn BottomBar chuẩn xác hơn khi có argument dynamic hoặc vào màn chi tiết
    val shouldShowBottomBar = currentRoute?.startsWith("shipper_order_detail") != true

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
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
                                item.icon?.let {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = item.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            label = {
                                Text(text = item.title, style = MaterialTheme.typography.labelSmall)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color(0xFF1A56DB),
                                indicatorColor = Color(0xFF1A56DB),
                                unselectedIconColor = Color(0xFFC4C7C5),
                                unselectedTextColor = Color(0xFF727785)
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavigationShipper.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Màn hình 1: Tổng quan
            composable(NavigationShipper.Dashboard.route) {

                ShipperDashboardScreen(
                    onOrderClick = { id -> // ✅ SỬA: Lấy id từ màn hình trả ra
                        // ✅ SỬA: Truyền chính xác định dạng chuỗi chứa ID
                        navController.navigate("shipper_order_detail/$id")
                    }
                )
            }

            // Màn hình 2: Đơn hàng của tôi
            composable(NavigationShipper.Orders.route) {
                ShipperOrdersScreen(
                    viewModel = shipperViewModel,
                    onNavigateToDetail = { id ->
                        navController.navigate("shipper_order_detail/$id")
                    }
                )
            }


            // Màn hình 4: Thông tin cá nhân Shipper (Đã nối thành công)
            composable(NavigationShipper.Account.route) {
                ShipperProfileScreen(
                    onLogoutClick = {
                        // Xử lý logic đăng xuất tài khoản ở đây
                    },
                    onNavigateToSection = { sectionKey ->
                        // Điều hướng đến các phân hệ nhỏ (Info, Vehicle, Payout...) nếu cần thiết
                    }
                )
            }

            // Màn hình phụ: Chi tiết đơn hàng & Bản đồ dẫn đường (Ẩn BottomBar)
            composable(
                route = "shipper_order_detail/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""

                ShipperOrderDetailScreen(
                    orderId = orderId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}