package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BuyJumpLogo
import com.example.ui.components.ImageUploadPicker
import com.example.ui.theme.BuyJumpBlue
import com.example.ui.theme.BuyJumpOrange
import com.example.ui.viewmodel.BuyJumpViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerRegisterScreen(
    viewModel: BuyJumpViewModel,
    onBack: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var storeName by remember { mutableStateOf("") }
    var storeDescription by remember { mutableStateOf("") }
    var businessAddress by remember { mutableStateOf("") }
    var pickupAddress by remember { mutableStateOf("") }
    var logoImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seller Registration", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    BuyJumpLogo(size = 44.dp, tagline = "Seller Network")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Apply as a BuyJump Marketplace Seller", fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text("Join hundreds of brands reaching millions of customers daily", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 2.dp))
                }
            }

            if (errorText != null) {
                item {
                    Surface(color = Color(0xFFFEF2F2), shape = RoundedCornerShape(8.dp)) {
                        Text(errorText.orEmpty(), color = Color(0xFFDC2626), fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                    }
                }
            }

            // Contact Info
            item {
                Text("Personal & Contact Details", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BuyJumpBlue)
            }

            item {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Legal Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("seller_name_input")
                )
            }

            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Business / Owner Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth().testTag("seller_email_input")
                )
            }

            item {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("seller_phone_input")
                )
            }

            item {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("seller_pass_input")
                )
            }

            // Store Info
            item {
                Text("Marketplace Store Information", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BuyJumpBlue, modifier = Modifier.padding(top = 10.dp))
            }

            item {
                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text("Store Name (Visible to Customers)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("seller_store_input")
                )
            }

            item {
                OutlinedTextField(
                    value = storeDescription,
                    onValueChange = { storeDescription = it },
                    label = { Text("Store Description") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("seller_desc_input")
                )
            }

            item {
                OutlinedTextField(
                    value = businessAddress,
                    onValueChange = { businessAddress = it },
                    label = { Text("Registered Business Address") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = pickupAddress,
                    onValueChange = { pickupAddress = it },
                    label = { Text("Logistics / Pickup Warehouse Address") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Store Logo Picker (Local disk upload)
            item {
                ImageUploadPicker(
                    images = logoImages,
                    onImagesChanged = { logoImages = it },
                    onUploadUri = { viewModel.uploadImageFile(it) },
                    maxImages = 1,
                    label = "Store Logo (Choose from Local Storage)"
                )
            }

            item {
                Button(
                    onClick = {
                        if (fullName.isBlank() || email.isBlank() || storeName.isBlank() || password.isBlank()) {
                            errorText = "Please fill in all mandatory fields"
                        } else {
                            viewModel.registerSeller(
                                fullName = fullName,
                                email = email,
                                phone = phone,
                                pass = password,
                                storeName = storeName,
                                description = storeDescription,
                                businessAddress = businessAddress,
                                pickupAddress = pickupAddress,
                                logoUri = logoImages.firstOrNull().orEmpty()
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("seller_register_submit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = BuyJumpOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SUBMIT APPLICATION FOR APPROVAL", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
