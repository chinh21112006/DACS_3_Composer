package com.example.dacs_3_composer.ui.user

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
import com.example.dacs_3_composer.ui.chat.MessageCenterScreen
import com.example.dacs_3_composer.ui.chat.ChatDetailScreen

@Composable
fun MainRouteContainerUser(
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    val cartViewModel: CartViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Ẩn Bottom Bar khi ở các màn hình chi tiết hoặc chat
    val shouldShowBottomBar = currentRoute !in listOf(
        "message_center",
        "chat_detail/{conversationId}",
        "cart",
        "order_tracking/{orderId}",
        "manage_address"
    ) && currentRoute?.startsWith("restaurant_detail") != true

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
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomBar(navController = navController)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            NavHost(navController = navController, startDestination = "home") {
                composable("home") {
                    HomeScreen(
                        navController = navController,
                        onNavigateToSearch = { query ->
                            navController.navigate("search/$query")
                        }
                    )
                }
                
                composable("notification") {
                    NotificationScreen()
                }

                composable("message_center") {
                    MessageCenterScreen(
                        onConversationClick = { convId -> 
                            navController.navigate("chat_detail/$convId") 
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "chat_detail/{conversationId}",
                    arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val convId = backStackEntry.arguments?.getString("conversationId") ?: ""
                    ChatDetailScreen(
                        conversationId = convId,
                        onBackClick = { navController.popBackStack() }
                    )
                }

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

                composable("order") {
                    OrderScreen(
                        suggestedRestaurants = sampleRestaurant,
                        chuyenHienThiTrangChiTiet = { orderId ->
                            navController.navigate("order_tracking/$orderId")
                        }
                    )
                }

                composable(
                    route = "order_tracking/{orderId}",
                    arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                    OrderTrackingScreen(
                        orderId = orderId,
                        onBackClick = { navController.popBackStack() }
                    )
                }

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

                composable("manage_address") {
                    com.example.dacs_3_composer.ui.user.profile.AddressScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }

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