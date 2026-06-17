package com.example.dacs_3_composer.ui.user

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dacs_3_composer.R
import com.example.dacs_3_composer.data.model.Restaurant
import com.example.dacs_3_composer.ui.user.home.HomeScreen
import com.example.dacs_3_composer.ui.user.order.OrderScreen
import com.example.dacs_3_composer.ui.user.order.OrderDetailScreen
import com.example.dacs_3_composer.ui.user.order.OrderViewModel
import com.example.dacs_3_composer.ui.user.profile.ProfileScreen
import com.example.dacs_3_composer.ui.user.profileRestaurant.RestaurantDetailScreen
import com.example.dacs_3_composer.ui.user.search.SearchScreen
import com.example.dacs_3_composer.ui.user.cart.CartViewModel
import com.example.dacs_3_composer.ui.user.cart.CartScreen
import com.example.dacs_3_composer.ui.user.notification.NotificationScreen
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun MainRouteContainerUser(
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()

    // Khởi tạo các ViewModel dùng chung ở cấp cha cao nhất
    val cartViewModel: CartViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()

    val sampleRestaurant = listOf(Restaurant(
        name = "The Coffee House",
        description = "Cà phê, trà, bánh ngọt...",
        rating = 4.8f,
        time = "25-30 phút",
        distance = "1.2 km",
        promo = "Free Ship",
        imageRes = R.drawable.banner1
    ))

    Scaffold(
        bottomBar = { BottomBarUser(navController = navController) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            NavHost(navController = navController, startDestination = "home") {
                // Trang chủ
                composable("home") {
                    HomeScreen(
                        navController = navController,
                        onNavigateToSearch = { query ->
                            navController.navigate("search/$query")
                        }
                    )
                }

                // Trang thông báo
                composable("notification") {
                    NotificationScreen()
                }

                // Tìm kiếm món ăn/nhà hàng
                composable(
                    route = "search/{query}"
                ) { backStackEntry ->
                    val query = backStackEntry.arguments?.getString("query") ?: ""
                    SearchScreen(
                        searchQuery = query,
                        navController = navController,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // Trang Quản lý Đơn hàng
                composable("order") {
                    OrderScreen(
                        suggestedRestaurants = sampleRestaurant,
                        orderViewModel = orderViewModel,
                        chuyenHienThiTrangChiTiet = { orderId ->
                            navController.navigate("order_tracking/$orderId")
                        }
                    )
                }

                // Tuyến đường hiển thị chi tiết hóa đơn đặt hàng (OrderDetailScreen)
                composable(
                    route = "order_tracking/{orderId}",
                    arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val orderId = backStackEntry.arguments?.getString("orderId") ?: ""

                    OrderDetailScreen(
                        orderId = orderId,
                        orderViewModel = orderViewModel,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                // Trang thông tin cá nhân
                composable("profile") {
                    ProfileScreen(
                        onLogoutClick = onLogout,
                        onNavigateToOrderHistory = {
                            navController.navigate("order") {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToManageAddress = {
                            navController.navigate("manage_address")
                        }
                    )
                }

                // Trang Quản lý danh sách địa chỉ đã lưu
                composable("manage_address") {
                    com.example.dacs_3_composer.ui.user.profile.AddressScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // Tuyến đường chi tiết quán ăn
                composable(
                    route = "restaurant_detail/{restaurantId}"
                ) { backStackEntry ->
                    val restaurantId = backStackEntry.arguments?.getString("restaurantId") ?: ""

                    RestaurantDetailScreen(
                        restaurantId = restaurantId,
                        cartViewModel = cartViewModel,
                        onBackClick = { navController.popBackStack() },
                        onAddToCart = { dish ->
                            cartViewModel.addToCart(dish)
                        },
                        onViewCartClick = {
                            navController.navigate("cart")
                        }
                    )
                }

                // Tuyến đường đến màn hình giỏ hàng
                composable("cart") {
                    CartScreen(
                        cartViewModel = cartViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomBarUser(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Danh sách các màn hình gốc chân đế hiển thị Thanh BottomBar
    val mainDestinations = listOf("home", "notification", "order", "profile")
    if (currentRoute !in mainDestinations) return // Tự động ẩn thanh điều hướng khi vào màn hình chi tiết hoặc giỏ hàng!

    NavigationBar {
        // 1. Home
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = {
                navController.navigate("home") {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "Trang chủ") },
            label = { Text("Home") }
        )

        // 2. Thông báo
        NavigationBarItem(
            selected = currentRoute == "notification",
            onClick = {
                navController.navigate("notification") {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Thông báo") },
            label = { Text("Notification") }
        )

        // 3. Đơn hàng
        NavigationBarItem(
            selected = currentRoute == "order",
            onClick = {
                navController.navigate("order"){
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.Receipt, contentDescription = "Đơn hàng") },
            label = { Text("Product") }
        )

        // 4. Cá nhân
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = {
                navController.navigate("profile"){
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.Person, contentDescription = "Cá nhân") },
            label = { Text("Profile") }
        )
    }
}