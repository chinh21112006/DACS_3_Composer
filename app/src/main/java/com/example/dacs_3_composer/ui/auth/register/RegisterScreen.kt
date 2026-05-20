package com.example.dacs_3_composer.ui.auth.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onRegisterClick: (String, String, String) -> Unit = { _, _, _ -> },
    onNavigateToLogin: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp), // Đồng bộ padding 24.dp như LoginScreen
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Tiêu đề
        Spacer(modifier = Modifier.height(80.dp))

        Text(
            text = "ĐĂNG KÝ",
            fontSize = 34.sp, // Đồng bộ kích thước font chữ tiêu đề lớn
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        // Đẩy nguyên phần dưới xuống giống hệt màn hình Login
        // Bạn có thể hạ xuống 150.dp - 180.dp nếu thấy 3 ô nhập liệu bị tràn quá màn hình nhé!
        Spacer(modifier = Modifier.height(200.dp))

        // =========================
        // FORM REGISTER
        // =========================

        // Ô nhập Email (edtEmailSignUp)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp), // Đồng bộ chiều cao 65.dp
            placeholder = { Text("Nhập Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Ô nhập Mật khẩu (edtPassSignUp)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp),
            placeholder = { Text("Nhập Mật khẩu") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Ô nhập lại Mật khẩu (Confirm Password)
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp),
            placeholder = { Text("Nhập lại Mật khẩu") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(50.dp))

        // Nút Đăng ký ngay (btnRegister)
        Button(
//            Chuyển phát cho AuthNavHost
            onClick = {
                onRegisterClick(email, password, confirmPassword)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE) // Giữ nguyên tone màu chủ đạo
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Đăng ký ngay",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Dòng chữ điều hướng quay lại phần Đăng nhập
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Bạn đã có tài khoản ?",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Đăng nhập ngay !",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6200EE),
                modifier = Modifier.clickable {
                    onNavigateToLogin()
                }
            )
        }

        // Đệm một khoảng dưới đáy để khi cuộn không bị sát mép màn hình
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    RegisterScreen()
}