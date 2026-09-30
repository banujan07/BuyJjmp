package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ConfirmDialog
import com.example.ui.theme.BuyJumpBlue
import com.example.ui.theme.BuyJumpOrange
import com.example.ui.theme.BuyJumpRed
import com.example.ui.viewmodel.BuyJumpViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileScreen(
    viewModel: BuyJumpViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val wishlistItems by viewModel.wishlistItems.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAddressSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Account", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFFEFF6FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = BuyJumpBlue,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser?.name ?: "Guest User",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = currentUser?.email ?: "Sign in to manage orders & addresses",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            if (currentUser?.phone?.isNotBlank() == true) {
                                Text(
                                    text = currentUser?.phone.orEmpty(),
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        if (currentUser == null) {
                            Button(
                                onClick = { viewModel.navigateTo(Screen.LOGIN) },
                                colors = ButtonDefaults.buttonColors(containerColor = BuyJumpBlue),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Login")
                            }
                        }
                    }
                }
            }

            // Quick Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.navigateTo(Screen.CUSTOMER_ORDERS) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${orders.size}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = BuyJumpBlue
                            )
                            Text(
                                text = "My Orders",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BuyJumpBlue
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.navigateTo(Screen.CUSTOMER_WISHLIST) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${wishlistItems.size}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = BuyJumpRed
                            )
                            Text(
                                text = "Wishlist",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BuyJumpRed
                            )
                        }
                    }
                }
            }

            // Customer Actions Group
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        ProfileMenuRow(
                            icon = Icons.Default.ShoppingBag,
                            title = "All Placed Orders",
                            subtitle = "View order status, receipts & tracking",
                            onClick = { viewModel.navigateTo(Screen.CUSTOMER_ORDERS) }
                        )
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        ProfileMenuRow(
                            icon = Icons.Default.Favorite,
                            title = "Wishlist Items",
                            subtitle = "Items saved for later purchase",
                            onClick = { viewModel.navigateTo(Screen.CUSTOMER_WISHLIST) }
                        )
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        ProfileMenuRow(
                            icon = Icons.Default.LocationOn,
                            title = "Shipping Addresses",
                            subtitle = "Manage home, work and pickup locations",
                            onClick = { showAddressSheet = true }
                        )
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        ProfileMenuRow(
                            icon = Icons.Default.Notifications,
                            title = "Notification Preferences",
                            subtitle = "Order updates, flash deals alerts",
                            onClick = { viewModel.showMessage("Push notifications enabled") }
                        )
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        ProfileMenuRow(
                            icon = Icons.Default.Lock,
                            title = "Change Password",
                            subtitle = "Update security credentials",
                            onClick = { viewModel.showMessage("Password reset instructions sent to your email") }
                        )
                    }
                }
            }

            // Logout / Sign in action
            item {
                if (currentUser != null) {
                    OutlinedButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("customer_logout_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BuyJumpRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Out", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { viewModel.navigateTo(Screen.LOGIN) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BuyJumpBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Sign In or Register", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        ConfirmDialog(
            show = true,
            title = "Log Out",
            message = "Are you sure you want to log out of BuyJump?",
            confirmButtonText = "Log Out",
            isDestructive = true,
            onConfirm = {
                viewModel.logout()
                showLogoutDialog = false
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    if (showAddressSheet) {
        AlertDialog(
            onDismissRequest = { showAddressSheet = false },
            title = { Text("Saved Shipping Address", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Default Address: 742 Evergreen Terrace, Springfield, 97477",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Recipient: ${currentUser?.name ?: "Sarah Jenkins"} • ${currentUser?.phone ?: "+1 (555) 987-6543"}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showAddressSheet = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BuyJumpBlue,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = subtitle, fontSize = 11.sp, color = Color(0xFF64748B))
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
    }
}
