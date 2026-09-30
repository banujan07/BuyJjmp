package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.ui.theme.BuyJumpBlue
import com.example.ui.theme.BuyJumpEmerald
import com.example.ui.theme.BuyJumpOrange
import com.example.ui.theme.BuyJumpRed
import com.example.ui.viewmodel.BuyJumpViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerOrdersScreen(
    viewModel: BuyJumpViewModel,
    onBack: () -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val userOrders = orders.filter { it.userId == currentUser?.id || currentUser == null }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders (${userOrders.size})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        if (userOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No orders placed yet", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Your placed orders will show their real-time delivery status here", fontSize = 13.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 4.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.navigateTo(Screen.CUSTOMER_SHOP) },
                        colors = ButtonDefaults.buttonColors(containerColor = BuyJumpBlue)
                    ) {
                        Text("Explore Marketplace")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(userOrders) { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { viewModel.openOrderTracking(order.id) }
                            .testTag("order_item_${order.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = order.id,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = BuyJumpBlue
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (order.status) {
                                        Order.STATUS_DELIVERED -> Color(0xFFECFDF5)
                                        Order.STATUS_CANCELLED -> Color(0xFFFEF2F2)
                                        else -> Color(0xFFEFF6FF)
                                    }
                                ) {
                                    Text(
                                        text = order.status.uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (order.status) {
                                            Order.STATUS_DELIVERED -> BuyJumpEmerald
                                            Order.STATUS_CANCELLED -> BuyJumpRed
                                            else -> BuyJumpBlue
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = dateFormat.format(Date(order.createdAt)),
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

                            // Items preview
                            order.items.take(2).forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${item.quantity}x ${item.productTitle}",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "$${String.format("%.2f", item.total)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            if (order.items.size > 2) {
                                Text(
                                    text = "+ ${order.items.size - 2} more items",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Total Paid", fontSize = 11.sp, color = Color(0xFF64748B))
                                    Text(
                                        text = "$${String.format("%.2f", order.totalAmount)}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = BuyJumpOrange
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Track Order", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BuyJumpBlue)
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = BuyJumpBlue)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
