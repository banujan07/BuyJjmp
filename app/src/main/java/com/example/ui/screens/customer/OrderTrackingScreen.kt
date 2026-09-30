package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.OrderStatusTimeline
import com.example.ui.theme.BuyJumpBlue
import com.example.ui.theme.BuyJumpEmerald
import com.example.ui.theme.BuyJumpOrange
import com.example.ui.theme.BuyJumpRed
import com.example.ui.viewmodel.BuyJumpViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    orderId: String,
    viewModel: BuyJumpViewModel,
    onBack: () -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    val order = orders.find { it.id == orderId }

    var showCancelDialog by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())

    if (order == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Order $orderId not found")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Order", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("tracking_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
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
            // Order ID & Status Header
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Order Number", fontSize = 11.sp, color = Color(0xFF64748B))
                                Text(
                                    text = order.id,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BuyJumpBlue
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (order.status) {
                                    Order.STATUS_DELIVERED -> Color(0xFFECFDF5)
                                    Order.STATUS_CANCELLED -> Color(0xFFFEF2F2)
                                    else -> Color(0xFFEFF6FF)
                                }
                            ) {
                                Text(
                                    text = order.status.uppercase(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (order.status) {
                                        Order.STATUS_DELIVERED -> BuyJumpEmerald
                                        Order.STATUS_CANCELLED -> BuyJumpRed
                                        else -> BuyJumpBlue
                                    },
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Placed on " + dateFormat.format(Date(order.createdAt)),
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Visual Status Progress Timeline
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Delivery Progress",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        OrderStatusTimeline(order = order)
                    }
                }
            }

            // Delivery Destination Card
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
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = order.customerName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = order.shippingAddress,
                            fontSize = 13.sp,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = "Phone: ${order.phone}",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        if (order.deliveryInstructions.isNotBlank()) {
                            Text(
                                text = "Note: ${order.deliveryInstructions}",
                                fontSize = 12.sp,
                                color = BuyJumpBlue,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Payment Information Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payment, contentDescription = null, tint = BuyJumpBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Payment Information", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Method", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text(order.paymentMethod, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Transaction ID", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text(order.paymentId, fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Payment Status", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text(order.paymentStatus.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BuyJumpEmerald)
                        }
                    }
                }
            }

            // Purchased Items Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Items in Package (${order.items.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        order.items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.productTitle,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Sold by ${item.storeName} • Qty: ${item.quantity}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Text(
                                    text = "$${String.format("%.2f", item.total)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Paid", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "$${String.format("%.2f", order.totalAmount)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = BuyJumpOrange
                            )
                        }
                    }
                }
            }

            // Cancel Order Button (if allowed)
            if (order.isCancellable) {
                item {
                    OutlinedButton(
                        onClick = { showCancelDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("cancel_order_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BuyJumpRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, tint = BuyJumpRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancel Order", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showCancelDialog) {
        ConfirmDialog(
            show = true,
            title = "Cancel Order",
            message = "Are you sure you want to cancel order ${order.id}? Any payment will be automatically refunded.",
            confirmButtonText = "Yes, Cancel Order",
            isDestructive = true,
            onConfirm = {
                viewModel.cancelOrder(order.id)
                showCancelDialog = false
            },
            onDismiss = { showCancelDialog = false }
        )
    }
}
