package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BuyJumpLogo
import com.example.ui.theme.BuyJumpBlue
import com.example.ui.theme.BuyJumpOrange
import com.example.ui.viewmodel.BuyJumpViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: BuyJumpViewModel,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("login_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Official BuyJump Branding Header
            BuyJumpLogo(size = 56.dp, tagline = "Marketplace")

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome Back",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Sign in to access your orders, cart & wishlist",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                placeholder = { Text("e.g. name@example.com") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null, tint = BuyJumpBlue)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_email_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                placeholder = { Text("••••••••") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = BuyJumpBlue)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input")
            )

            // Forgot Password
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Forgot Password?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BuyJumpBlue,
                    modifier = Modifier.clickable {
                        viewModel.showMessage("Password recovery link sent to your registered email")
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Submit Button
            Button(
                onClick = {
                    viewModel.login(email, password)
                },
                enabled = email.isNotBlank() && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("login_submit_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = BuyJumpBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text("LOGIN", fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Don't have an account? Create Account
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Don't have an account?", fontSize = 13.sp, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Create Account",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BuyJumpOrange,
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(Screen.REGISTER) }
                        .testTag("login_create_account_link")
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Are you a Seller? Seller Login
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF1F5F9),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { viewModel.navigateTo(Screen.SELLER_REGISTER) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Are you a Seller?",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Seller Registration & Portal",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BuyJumpBlue
                    )
                }
            }
        }
    }
}
