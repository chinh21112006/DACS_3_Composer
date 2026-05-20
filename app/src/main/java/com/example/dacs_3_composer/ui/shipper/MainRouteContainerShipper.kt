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
import com.example.dacs_3_composer.ui.shipper.profile.ShipperEditProfileScreen // 🎯 Nhớ Import màn hình này vào nhé!
import com.example.dacs_3_composer.ui.shipper.profile.ShipperProfileViewModel

@Composable
fun MainRouteContainerShipper(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    // Khởi tạo ViewModel dùng chung
    val shipperViewModel: ShipperViewModel = viewModel()
    val profileViewModel: ShipperProfileViewModel = viewModel()

    val navigationItems = listOf(
        NavigationShipper.Dashboard,
        NavigationShipper.Orders,
        NavigationShipper.Account
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 🎯 CẬP NHẬT: Ẩn BottomBar khi ở màn hình chi tiết đơn hàng HOẶC màn hình sửa thông tin cá nhân
    val shouldShowBottomBar = currentRoute?.startsWith("shipper_order_detail") != true
            && currentRoute != "shipper_edit_profile"

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
                    onOrderClick = { id ->
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

            // Màn hình 3: Thông tin cá nhân Shipper
            composable(NavigationShipper.Account.route) {
                // Tự động reload thông tin mới mỗi khi Shipper quay lại màn hình này
                LaunchedEffect(Unit) {
                    profileViewModel.loadProfile()
                }

                ShipperProfileScreen(
                    viewModel = profileViewModel,
                    onLogoutClick = {
                        // 🎯 CẬP NHẬT: Kích hoạt callback truyền ngược về MainActivity
                        onLogout()
                    },
                    onNavigateToSection = { sectionKey ->
                        // 🎯 CẬP NHẬT: Điều hướng qua Route mới khi chọn mục EDIT_PROFILE
                        if (sectionKey == "EDIT_PROFILE") {
                            navController.navigate("shipper_edit_profile")
                        }
                    }
                )
            }

            // 🎯 THÊM MỚI: Màn hình chỉnh sửa thông tin cá nhân (Ẩn BottomBar)
            composable("shipper_edit_profile") {
                ShipperEditProfileScreen(
                    viewModel = profileViewModel,
                    onBackClick = {
                        navController.popBackStack()
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