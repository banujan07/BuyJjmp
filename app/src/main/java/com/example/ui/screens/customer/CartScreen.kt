package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.CartItem
import com.example.ui.components.ConfirmDialog
import com.example.ui.theme.BuyJumpBlue
import com.example.ui.theme.BuyJumpOrange
import com.example.ui.theme.BuyJumpRed
import com.example.ui.viewmodel.BuyJumpViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: BuyJumpViewModel,
    onBack: () -> Unit
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val subtotal by viewModel.cartSubtotal.collectAsState()
    val selectedCount = cartItems.count { it.isSelected }
    val deliveryFee = if (subtotal > 0) 5.00 else 0.00
    val total = subtotal + deliveryFee

    var itemToDelete by remember { mutableStateOf<CartItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Cart (${cartItems.size})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (cartItems.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearCart() }) {
                            Text("Clear", color = BuyJumpRed, fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", color = Color(0xFF64748B), fontSize = 13.sp)
                            Text("$${String.format("%.2f", subtotal)}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Delivery Fee", color = Color(0xFF64748B), fontSize = 13.sp)
                            Text("$${String.format("%.2f", deliveryFee)}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFE2E8F0))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total Amount", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color(0xFF64748B))
                                Text(
                                    text = "$${String.format("%.2f", total)}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BuyJumpOrange
                                )
                            }

                            Button(
                                onClick = { viewModel.navigateTo(Screen.CHECKOUT) },
                                enabled = selectedCount > 0,
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("proceed_to_checkout_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = BuyJumpOrange),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Checkout ($selectedCount)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.RemoveShoppingCart,
                        contentDescription = null,
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Your shopping cart is empty",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Browse our thousands of products and jump into deals!",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.navigateTo(Screen.CUSTOMER_SHOP) },
                        colors = ButtonDefaults.buttonColors(containerColor = BuyJumpBlue)
                    ) {
                        Text("Start Shopping")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .testTag("cart_item_${item.productId}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isSelected,
                                onCheckedChange = { viewModel.toggleCartSelection(item.productId) },
                                colors = CheckboxDefaults.colors(checkedColor = BuyJumpBlue)
                            )

                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(item.productImage)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = item.productTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.storeName,
                                    fontSize = 11.sp,
                                    color = BuyJumpBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = item.productTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$${String.format("%.2f", item.price)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = BuyJumpOrange
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Quantity Stepper & Remove
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                    ) {
                                        IconButton(
                                            onClick = { viewModel.updateCartQuantity(item.productId, item.quantity - 1) },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Minus", modifier = Modifier.size(16.dp))
                                        }
                                        Text(
                                            text = "${item.quantity}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                        IconButton(
                                            onClick = { viewModel.updateCartQuantity(item.productId, item.quantity + 1) },
                                            enabled = item.quantity < item.maxStock,
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Plus", modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    IconButton(
                                        onClick = { itemToDelete = item },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Remove",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog before removal
    if (itemToDelete != null) {
        ConfirmDialog(
            show = true,
            title = "Remove Item",
            message = "Remove '${itemToDelete?.productTitle}' from your cart?",
            confirmButtonText = "Remove",
            isDestructive = true,
            onConfirm = {
                itemToDelete?.let { viewModel.removeFromCart(it.productId) }
                itemToDelete = null
            },
            onDismiss = { itemToDelete = null }
        )
    }
}
