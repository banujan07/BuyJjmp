package com.example.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BuyJumpLogo
import com.example.ui.theme.BuyJumpBlue
import com.example.ui.theme.BuyJumpOrange
import com.example.ui.viewmodel.BuyJumpViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: BuyJumpViewModel,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
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
            BuyJumpLogo(size = 48.dp, tagline = "Marketplace")
            Spacer(modifier = Modifier.height(18.dp))

            Text("Create Account", fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text("Join BuyJump to shop thousands of verified stores", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 2.dp, bottom = 20.dp))

            if (errorMessage != null) {
                Text(text = errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = BuyJumpBlue) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("reg_name_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, null, tint = BuyJumpBlue) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("reg_email_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                leadingIcon = { Icon(Icons.Default.Phone, null, tint = BuyJumpBlue) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("reg_phone_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = BuyJumpBlue) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("reg_pass_input")
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    if (name.isBlank() || email.isBlank() || password.isBlank()) {
                        errorMessage = "Please fill in all required fields"
                    } else {
                        viewModel.registerCustomer(name, email, phone, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("reg_submit_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = BuyJumpBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SIGN UP", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account?", fontSize = 13.sp, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Login",
                    fontWeight = FontWeight.Bold,
                    color = BuyJumpOrange,
                    modifier = Modifier.clickable { viewModel.navigateTo(Screen.LOGIN) }
                )
            }
        }
    }
}
