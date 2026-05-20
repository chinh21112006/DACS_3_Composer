package com.example.dacs_3_composer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dacs_3_composer.ui.admin.MainRouteContainerAdmin
import com.example.dacs_3_composer.ui.auth.AuthViewModel
import com.example.dacs_3_composer.ui.auth.login.LoginScreen
import com.example.dacs_3_composer.ui.auth.signup.RegisterScreen
import com.example.dacs_3_composer.ui.user.MainRouteContainerUser
import com.example.dacs_3_composer.ui.restaurant.MainRouteContainerRestaurant
import com.example.dacs_3_composer.ui.shipper.MainRouteContainerShipper
import com.example.dacs_3_composer.ui.theme.DACS_3_ComposerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DACS_3_ComposerTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val authState by authViewModel.authState.collectAsState()
                val context = LocalContext.current

                LaunchedEffect(authState) {
                    if (authState.isNotEmpty()) {
                        if (authState != "Loading..." && authState != "Đang kết nối với Google...") {
                            Toast.makeText(context, authState, Toast.LENGTH_SHORT).show()
                        }

                        when (authState) {
                            "Đăng nhập User thành công!" -> {
                                navController.navigate("main_user") {
                                    popUpTo("login") { inclusive = true }
                                }
                                authViewModel.clearAuthState()
                            }
                            "Đăng nhập Restaurant thành công!" -> {
                                navController.navigate("main_restaurant") {
                                    popUpTo("login") { inclusive = true }
                                }
                                authViewModel.clearAuthState()
                            }
                            "Đăng nhập Shipper thành công!" -> {
                                navController.navigate("main_shipper") { // Đã sửa chính tả thành "main_shipper"
                                    popUpTo("login") { inclusive = true }
                                }
                                authViewModel.clearAuthState()
                            }
                            // 💡 Bổ sung điều hướng cho Admin thành công nếu cần
                            "Đăng nhập Admin thành công!" -> {
                                navController.navigate("main_admin") {
                                    popUpTo("login") { inclusive = true }
                                }
                                authViewModel.clearAuthState()
                            }
                            "Đăng ký thành công!" -> {
                                navController.popBackStack()
                                authViewModel.clearAuthState()
                            }
                            "Đã đăng xuất!" -> {
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                                authViewModel.clearAuthState()
                            }
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        LoginScreen(
                            onLoginClick = { email, password ->
                                authViewModel.loginUser(email, password)
                            },
                            onSignUpClick = {
                                navController.navigate("register")
                            },
                            onGoogleClick = {
                                authViewModel.loginWithGoogle(context)
                            }
                        )
                    }

                    composable("register") {
                        RegisterScreen(
                            onRegisterClick = { email, password, confirmPass ->
                                authViewModel.registerUser(email, password, confirmPass)
                            },
                            onNavigateToLogin = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("main_user") {
                        MainRouteContainerUser(
                            onLogout = {
                                authViewModel.logoutUser()
                            }
                        )
                    }

                    composable("main_restaurant") {
                        MainRouteContainerRestaurant()
                    }
                    composable("main_admin") {
                        MainRouteContainerAdmin()
                    }

                    composable("main_shipper") {
                        MainRouteContainerShipper()
                    }
                }
            }
        }
    }
}
