package com.example.dacs_3_composer.ui.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs_3_composer.R

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    onSignUpClick: () -> Unit = {},
    onFacebookClick: () -> Unit = {},
    onGoogleClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Tiêu đề
        Spacer(modifier = Modifier.height(80.dp))

        Text(
            text = "Login",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        // Đẩy nguyên phần dưới xuống
        Spacer(modifier = Modifier.height(250.dp))

        // =========================
        // FORM LOGIN
        // =========================

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp),
            placeholder = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp),
            placeholder = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                onLoginClick(email, password)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Login",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            Text(
                text = "It is first for you ?",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Sign up now !",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6200EE),
                modifier = Modifier.clickable {
                    onSignUpClick()
                }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "OR Sign In with",
            fontSize = 18.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(60.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Facebook
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .clickable { onFacebookClick() }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon_facebook),
                    contentDescription = "Facebook",
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Google
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .clickable { onGoogleClick() }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon_google),
                    contentDescription = "Google",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}