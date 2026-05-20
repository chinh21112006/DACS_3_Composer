package com.example.dacs_3_composer.ui.restaurant

import androidx.compose.foundation.layout.Box
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
import com.example.dacs_3_composer.ui.restaurant.home.RestaurantHomeScreen
import com.example.dacs_3_composer.ui.restaurant.menu.RestaurantMenuScreen
import com.example.dacs_3_composer.ui.restaurant.order.RestaurantOrderScreen
import com.example.dacs_3_composer.ui.restaurant.profile.RestauranProfileScreen
import com.example.dacs_3_composer.ui.restaurant.store.RestaurantManageScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MainRouteContainerRestaurant(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""

    // Danh sách các mục hiển thị dưới thanh BottomBar điều hướng của Nhà hàng
    val navigationItems = listOf(
        NavigationRestaurant.Dashboard,
        NavigationRestaurant.Orders,
        NavigationRestaurant.Store,
        NavigationRestaurant.Menu,
        NavigationRestaurant.Account
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
                            selectedTextColor = Color(0xFF2159BC),
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
            startDestination = NavigationRestaurant.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // 🎯 ĐỒNG BỘ: Gọi màn hình chính trống tham số để nó tự nạp dữ liệu Realtime từ ViewModel
            composable(NavigationRestaurant.Dashboard.route) {
                RestaurantHomeScreen()
            }

            // Màn hình Quản lý đơn hàng (Đã xử lý bộ lọc PENDING, PROCESSING, SHIPPING, COMPLETED, CANCELLED)
            composable(NavigationRestaurant.Orders.route) {
                RestaurantOrderScreen()
            }

            // Màn hình Quản lý thông tin cửa hàng
            composable(NavigationRestaurant.Store.route) {
                RestaurantManageScreen(
                    restaurantId = currentUserId, // Đồng bộ trực tiếp UID của tài khoản đối tác đang đăng nhập
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onEditCoverClick = { },
                    onEditDishClick = { }
                )
            }

            // Màn hình Quản lý danh sách thực đơn món ăn của quán
            composable(NavigationRestaurant.Menu.route) {
                RestaurantMenuScreen()
            }

            // Màn hình Hồ sơ cá nhân / Thiết lập tài khoản nhà hàng
            composable(NavigationRestaurant.Account.route) {
                RestauranProfileScreen()
            }
        }
    }
}