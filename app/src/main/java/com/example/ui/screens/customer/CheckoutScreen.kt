package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BuyJumpBlue
import com.example.ui.theme.BuyJumpEmerald
import com.example.ui.theme.BuyJumpOrange
import com.example.ui.theme.BuyJumpRed
import com.example.ui.viewmodel.BuyJumpViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: BuyJumpViewModel,
    onBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val subtotal by viewModel.cartSubtotal.collectAsState()
    val deliveryFee = 5.00
    val total = subtotal + deliveryFee
    val selectedCartItems = cartItems.filter { it.isSelected }

    // Form Fields
    var recipientName by remember { mutableStateOf(currentUser?.name.orEmpty()) }
    var email by remember { mutableStateOf(currentUser?.email.orEmpty()) }
    var phone by remember { mutableStateOf(currentUser?.phone.orEmpty()) }
    var streetAddress by remember { mutableStateOf(currentUser?.addresses?.firstOrNull()?.street ?: "742 Evergreen Terrace") }
    var city by remember { mutableStateOf(currentUser?.addresses?.firstOrNull()?.city ?: "Springfield") }
    var postalCode by remember { mutableStateOf(currentUser?.addresses?.firstOrNull()?.postalCode ?: "97477") }
    var deliveryInstructions by remember { mutableStateOf("") }

    // Payment Selection
    var selectedPaymentMethod by remember { mutableStateOf("card") } // "card" or "cod"

    // Card Details (Validated, never persisted raw!)
    var rawCardNumber by remember { mutableStateOf("4242424242424242") }
    var cardExpiry by remember { mutableStateOf("12/28") } // MM/YY
    var cardCvv by remember { mutableStateOf("123") } // Exactly 3 digits
    var cardHolder by remember { mutableStateOf(currentUser?.name ?: "Sarah Jenkins") }

    // Validation Errors
    var formError by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    fun validateAndSubmit() {
        formError = null

        if (recipientName.isBlank()) {
            formError = "Recipient name is required"
            return
        }
        if (phone.isBlank()) {
            formError = "Phone number is required for delivery notifications"
            return
        }
        if (streetAddress.isBlank() || city.isBlank()) {
            formError = "Full shipping address and city are required"
            return
        }

        if (selectedPaymentMethod == "card") {
            // Validate Card Number: Digits only, maximum 16 digits
            val cleanCard = rawCardNumber.filter { it.isDigit() }
            if (cleanCard.length != 16) {
                formError = "Card number must be exactly 16 digits"
                return
            }

            // Validate Expiry: MM/YY
            val expiryParts = cardExpiry.split("/")
            if (expiryParts.size != 2 || expiryParts[0].length != 2 || expiryParts[1].length != 2) {
                formError = "Expiry must be in MM/YY format"
                return
            }
            val month = expiryParts[0].toIntOrNull() ?: 0
            val year = (expiryParts[1].toIntOrNull() ?: 0) + 2000

            if (month !in 1..12) {
                formError = "Expiry month must be between 01 and 12"
                return
            }

            val currentCal = Calendar.getInstance()
            val currentYear = currentCal.get(Calendar.YEAR)
            val currentMonth = currentCal.get(Calendar.MONTH) + 1

            if (year < currentYear || (year == currentYear && month < currentMonth)) {
                formError = "Card expiry date cannot be in the past"
                return
            }

            // Validate CVV: Exactly 3 digits, numbers only
            val cleanCvv = cardCvv.filter { it.isDigit() }
            if (cleanCvv.length != 3) {
                formError = "CVV must be exactly 3 numeric digits"
                return
            }

            if (cardHolder.isBlank()) {
                formError = "Cardholder name is required"
                return
            }
        }

        isProcessing = true
        val fullAddress = "$streetAddress, $city, $postalCode"
        val paymentMethodName = if (selectedPaymentMethod == "card") {
            "Credit Card (•••• ${rawCardNumber.takeLast(4)})"
        } else {
            "Cash on Delivery"
        }

        viewModel.placeOrder(
            shippingAddress = fullAddress,
            phone = phone,
            instructions = deliveryInstructions,
            paymentMethod = paymentMethodName
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total to Pay", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text(
                            text = "$${String.format("%.2f", total)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = BuyJumpOrange
                        )
                    }

                    Button(
                        onClick = { validateAndSubmit() },
                        enabled = !isProcessing,
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("confirm_place_order_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = BuyJumpOrange),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Place Order", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Validation Error Banner
            if (formError != null) {
                item {
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(10.dp),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = BuyJumpRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formError.orEmpty(),
                                color = BuyJumpRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Customer Contact Info Section
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = BuyJumpBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Customer Information", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = recipientName,
                            onValueChange = { recipientName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_name_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_phone_input"),
                            singleLine = true
                        )
                    }
                }
            }

            // Delivery Address Section
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = BuyJumpBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delivery Address", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = streetAddress,
                            onValueChange = { streetAddress = it },
                            label = { Text("Street Address") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_address_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("City") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = postalCode,
                                onValueChange = { postalCode = it },
                                label = { Text("Postal Code") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = deliveryInstructions,
                            onValueChange = { deliveryInstructions = it },
                            label = { Text("Delivery Instructions (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            // Payment Architecture Section (Strict Validation, No Raw Card Storage)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payment, contentDescription = null, tint = BuyJumpBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Payment Method", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Radio choices
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedPaymentMethod == "card",
                                onClick = { selectedPaymentMethod = "card" },
                                colors = RadioButtonDefaults.colors(selectedColor = BuyJumpBlue)
                            )
                            Text("Credit / Debit Card (Secure Tokenized Gateway)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedPaymentMethod == "cod",
                                onClick = { selectedPaymentMethod = "cod" },
                                colors = RadioButtonDefaults.colors(selectedColor = BuyJumpBlue)
                            )
                            Text("Cash on Delivery (COD)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        if (selectedPaymentMethod == "card") {
                            Spacer(modifier = Modifier.height(8.dp))

                            // Card Number
                            OutlinedTextField(
                                value = rawCardNumber,
                                onValueChange = { input ->
                                    // Digits only, max 16 digits
                                    val digitsOnly = input.filter { it.isDigit() }
                                    if (digitsOnly.length <= 16) {
                                        rawCardNumber = digitsOnly
                                    }
                                },
                                label = { Text("Card Number (16 Digits)") },
                                placeholder = { Text("•••• •••• •••• ••••") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                leadingIcon = {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = BuyJumpBlue)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("checkout_card_number_input"),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Expiry MM/YY
                                OutlinedTextField(
                                    value = cardExpiry,
                                    onValueChange = { input ->
                                        if (input.length <= 5) cardExpiry = input
                                    },
                                    label = { Text("Expiry (MM/YY)") },
                                    placeholder = { Text("12/28") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )

                                // CVV 3 Digits
                                OutlinedTextField(
                                    value = cardCvv,
                                    onValueChange = { input ->
                                        // Numbers only, maximum 3 digits!
                                        val digits = input.filter { it.isDigit() }
                                        if (digits.length <= 3) {
                                            cardCvv = digits
                                        }
                                    },
                                    label = { Text("CVV (3 Digits)") },
                                    placeholder = { Text("123") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = cardHolder,
                                onValueChange = { cardHolder = it },
                                label = { Text("Cardholder Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = BuyJumpEmerald, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "256-bit SSL encrypted. Raw card data is never stored on our database.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }

            // Order Summary Breakdown
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Order Items (${selectedCartItems.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        selectedCartItems.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.quantity}x ${item.productTitle}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "$${String.format("%.2f", item.itemTotal)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text("$${String.format("%.2f", subtotal)}", fontSize = 13.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Delivery Fee", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text("$${String.format("%.2f", deliveryFee)}", fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Grand Total", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("$${String.format("%.2f", total)}", fontSize = 17.sp, fontWeight = FontWeight.Black, color = BuyJumpOrange)
                        }
                    }
                }
            }
        }
    }
}
