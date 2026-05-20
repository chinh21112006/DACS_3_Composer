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
import com.example.dacs_3_composer.ui.user.orderDaital.OrderTrackingScreen
import com.example.dacs_3_composer.ui.user.profile.ProfileScreen
import com.example.dacs_3_composer.ui.user.profileRestaurant.RestaurantDetailScreen
import com.example.dacs_3_composer.ui.user.search.SearchScreen
import com.example.dacs_3_composer.ui.user.cart.CartViewModel
import com.example.dacs_3_composer.ui.user.cart.CartScreen
import com.example.dacs_3_composer.ui.user.notification.NotificationScreen

@Composable
fun MainRouteContainerUser(
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()

    // Khởi tạo bộ não giỏ hàng dùng chung ở cấp cha cao nhất
    val cartViewModel: CartViewModel = viewModel()

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
        bottomBar = { BottomBar(navController = navController) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            NavHost(navController = navController, startDestination = "home") {
                // Trang chủ
                // Bên trong NavHost của MainRouteContainerUser.kt
                composable("home") {
                    HomeScreen(
                        navController = navController,
                        // 🌟 Định nghĩa hành động khi bấm tìm kiếm: nhảy sang màn hình search kèm tham số
                        onNavigateToSearch = { query ->
                            navController.navigate("search/$query")
                        }
                    )
                }
                // 🌟 THÊM ROUTE NÀY VÀO ĐỂ HỨNG ĐIỀU HƯỚNG TỪ BOTTOMBAR MỚI
                // Tìm đoạn này trong khối NavHost của MainRouteContainerUser.kt:
                composable("notification") {
                    // 🚀 Thế màn hình thông báo thật chúng ta vừa làm vào đây
                    NotificationScreen()
                }
                composable(
                    route = "search/{query}"
                ) { backStackEntry ->
                    val query = backStackEntry.arguments?.getString("query") ?: ""
                    SearchScreen(
                        searchQuery = query,
                        navController = navController, // 🚀 Thêm dòng này vào
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // 🌟 ĐÃ SỬA: Khi bấm nút "Theo dõi đơn", ta nhận mã id đơn hàng thật và truyền đi
                composable("order") {
                    OrderScreen(
                        suggestedRestaurants = sampleRestaurant,
                        chuyenHienThiTrangChiTiet = { orderId ->
                            navController.navigate("order_tracking/$orderId")
                        }
                    )
                }

                // 🌟 ĐÃ SỬA: Cấu hình tuyến đường nhận tham số mã đơn hàng động {orderId} từ màn hình Order bắn sang
                composable(
                    route = "order_tracking/{orderId}",
                    arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val orderId = backStackEntry.arguments?.getString("orderId") ?: ""

                    OrderTrackingScreen(
                        orderId = orderId, // 🚀 Bắn mã đơn hàng thật vào màn hình chi tiết để lấy dữ liệu từ Firebase
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                composable("profile") {
                    ProfileScreen(
                        onLogoutClick = onLogout,
                        // 🚀 THÊM MỚI 2 SỰ KIỆN ĐIỀU HƯỚNG RA NGOÀI
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
                // 🌟 THÊM ROUTE CHO MÀN HÌNH ĐỊA CHỈ MỚI
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