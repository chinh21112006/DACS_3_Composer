package com.example.dacs_3_composer.ui.auth

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dacs_3_composer.ui.auth.login.LoginScreen
import com.example.dacs_3_composer.ui.auth.signup.RegisterScreen
import com.example.dacs_3_composer.ui.user.MainRouteContainerUser

// Định nghĩa tên các màn hình (Routes)
sealed class AuthScreen(val route: String) {
    object Login : AuthScreen("login")
    object SignUp : AuthScreen("signup")
}

@Composable
fun AuthNavHost(
    authViewModel: AuthViewModel = viewModel() // Khởi tạo ViewModel chung cho luồng Auth
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Lắng nghe trạng thái thông báo lỗi/thành công từ Firebase qua ViewModel
    val authState by authViewModel.authState.collectAsState()

    // Hiển thị Toast thông báo và xử lý nhảy màn hình theo Phân Quyền
    LaunchedEffect(authState) {
        if (authState.isNotEmpty()) {
            // Không hiển thị Toast khi đang chạy các hiệu ứng loading tránh phiền phức
            if (authState != "Loading..." && authState != "Đợi xí, đang xử lý..." && authState != "Đang kết nối với Google...") {
                Toast.makeText(context, authState, Toast.LENGTH_SHORT).show()
            }

            // 1. Luồng xử lý sau khi ĐĂNG KÝ thành công
            if (authState == "Đăng ký thành công!") {
                navController.navigate(AuthScreen.Login.route) {
                    popUpTo(AuthScreen.SignUp.route) { inclusive = true }
                }
                authViewModel.clearAuthState()
            }

            // 2. Luồng xử lý sau khi ĐĂNG NHẬP thành công tùy vào từng ROLE
            else if (authState == "Đăng nhập User thành công!") {
                navController.navigate("user_home") {
                    popUpTo(AuthScreen.Login.route) { inclusive = true }
                }
                authViewModel.clearAuthState()
            }
            else if (authState == "Đăng nhập Restaurant thành công!") {
                // 🌟 BỔ SUNG: Nhảy sang luồng giao diện của Chủ nhà hàng quản lý món ăn
                navController.navigate("restaurant_home") {
                    popUpTo(AuthScreen.Login.route) { inclusive = true }
                }
                authViewModel.clearAuthState()
            }
            else if (authState == "Đăng nhập Shipper thành công!") {
                // 🌟 BỔ SUNG: Nhảy sang luồng giao diện của Tài xế đi giao hàng
                navController.navigate("shipper_home") {
                    popUpTo(AuthScreen.Login.route) { inclusive = true }
                }
                authViewModel.clearAuthState()
            }
            else if (authState == "Đăng nhập Admin thành công!") {
                // 🌟 BỔ SUNG: Nhảy sang luồng giao diện Tổng quản lý hệ thống Admin
                navController.navigate("admin_home") {
                    popUpTo(AuthScreen.Login.route) { inclusive = true }
                }
                authViewModel.clearAuthState()
            }

            // 3. XỬ LÝ KHI CÓ LỖI HOẶC HỦY (Báo xong dọn sạch trạng thái để không bị lặp Toast)
            else if (authState != "Loading..." && authState != "Đợi xí, đang xử lý..." && authState != "Đang kết nối với Google...") {
                authViewModel.clearAuthState()
            }
        }
    }

    // Cấu hình danh sách các màn hình trong toàn App
    NavHost(
        navController = navController,
        startDestination = AuthScreen.Login.route // Màn hình đầu tiên xuất hiện là Login
    ) {
        // ==========================================
        // KHỐI 1: MÀN HÌNH ĐĂNG NHẬP (LOGIN)
        // ==========================================
        composable(route = AuthScreen.Login.route) {
            LoginScreen(
                onLoginClick = { email, password ->
                    authViewModel.loginUser(email, password)
                },
                onSignUpClick = {
                    navController.navigate(AuthScreen.SignUp.route)
                },
                onFacebookClick = { /* Xử lý FB nếu cần */ },
                onGoogleClick = {
                    // 🌟 KẾT NỐI: Gọi hàm đăng nhập Google từ ViewModel lên UI thành công!
                    authViewModel.loginWithGoogle(context)
                }
            )
        }

        // ==========================================
        // KHỐI 2: MÀN HÌNH ĐĂNG KÝ (SIGN UP)
        // ==========================================
        composable(route = AuthScreen.SignUp.route) {
            RegisterScreen(
                onRegisterClick = { email, password, confirmPass ->
                    authViewModel.registerUser(email, password, confirmPass)
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // ==========================================
        // KHỐI 3: GIAO DIỆN USER (MÀN HÌNH KHÁCH MUA HÀNG)
        // ==========================================
        composable(route = "user_home") {
            MainRouteContainerUser(
                onLogout = {
                    authViewModel.logoutUser()
                    navController.navigate(AuthScreen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ==========================================
        // KHỐI 4: GIAO DIỆN RESTAURANT (CHỦ QUÁN ĂN) - Chờ thiết kế UI
        // ==========================================
        composable(route = "restaurant_home") {
            // Tạm thời hiển thị một màn hình trống báo danh tính cho tới khi bạn code UI riêng
            androidx.compose.material3.Text("Giao diện Quản lý của Nhà hàng (Đang phát triển)")
        }

        // ==========================================
        // KHỐI 5: GIAO DIỆN SHIPPER (TÀI XẾ) - Chờ thiết kế UI
        // ==========================================
        composable(route = "shipper_home") {
            androidx.compose.material3.Text("Giao diện Nhận đơn của Shipper (Đang phát triển)")
        }

        // ==========================================
        // KHỐI 6: GIAO DIỆN ADMIN (QUẢN TRỊ VIÊN) - Chờ thiết kế UI
        // ==========================================
        composable(route = "admin_home") {
            androidx.compose.material3.Text("Giao diện Tổng quản lý Admin Hệ Thống (Đang phát triển)")
        }
    }
}