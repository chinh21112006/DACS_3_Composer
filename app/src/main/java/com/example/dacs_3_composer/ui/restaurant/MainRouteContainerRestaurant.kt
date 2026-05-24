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
import com.example.dacs_3_composer.ui.restaurant.profile.settings.ActivityHistoryScreen
import com.example.dacs_3_composer.ui.restaurant.profile.settings.HelpSupportScreen
import com.example.dacs_3_composer.ui.restaurant.profile.settings.NotificationSettingsScreen
import com.example.dacs_3_composer.ui.restaurant.profile.settings.RestaurantInfoScreen
import com.example.dacs_3_composer.ui.restaurant.profile.settings.SecurityScreen
import com.example.dacs_3_composer.ui.restaurant.store.RestaurantManageScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MainRouteContainerRestaurant(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""

    val navigationItems = listOf(
        NavigationRestaurant.Dashboard,
        NavigationRestaurant.Orders,
        NavigationRestaurant.Store,
        NavigationRestaurant.Menu,
        NavigationRestaurant.Account
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                navigationItems.forEach { item ->
                    // 🎯 ĐỒNG BỘ: Highlight mục "Tài khoản" cho tất cả các trang cài đặt con
                    val isSettingsRoute = currentRoute == "restaurant_info_settings" || 
                                         currentRoute == "notification_settings" || 
                                         currentRoute == "activity_history" || 
                                         currentRoute == "security_settings" ||
                                         currentRoute == "help_support"
                    
                    val isSelected = currentRoute == item.route || (item == NavigationRestaurant.Account && isSettingsRoute)

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
            composable(NavigationRestaurant.Dashboard.route) {
                RestaurantHomeScreen()
            }

            composable(NavigationRestaurant.Orders.route) {
                RestaurantOrderScreen()
            }

            composable(NavigationRestaurant.Store.route) {
                RestaurantManageScreen(
                    restaurantId = currentUserId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onEditCoverClick = { },
                    onEditDishClick = { }
                )
            }

            composable(NavigationRestaurant.Menu.route) {
                RestaurantMenuScreen()
            }

            composable(NavigationRestaurant.Account.route) {
                RestauranProfileScreen(
                    onStoreInfoClick = {
                        navController.navigate("restaurant_info_settings")
                    },
                    onNotificationSettingClick = {
                        navController.navigate("notification_settings")
                    },
                    onActivityHistoryClick = {
                        navController.navigate("activity_history")
                    },
                    onSecurityClick = {
                        navController.navigate("security_settings")
                    },
                    onHelpClick = {
                        navController.navigate("help_support")
                    },
                    onLogoutClick = onLogout
                )
            }

            composable("restaurant_info_settings") {
                RestaurantInfoScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable("notification_settings") {
                NotificationSettingsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable("activity_history") {
                ActivityHistoryScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable("security_settings") {
                SecurityScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable("help_support") {
                HelpSupportScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
