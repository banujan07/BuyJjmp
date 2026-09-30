package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.ui.theme.BuyJumpBlue
import com.example.ui.theme.BuyJumpEmerald
import com.example.ui.theme.BuyJumpOrange

@Composable
fun OrderStatusTimeline(
    order: Order,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        "Placed",
        "Confirmed",
        "Processing",
        "Shipped",
        "Delivered"
    )

    val currentStep = order.progressStepIndex
    val isCancelled = order.status == Order.STATUS_CANCELLED

    Column(modifier = modifier.fillMaxWidth()) {
        if (isCancelled) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEE2E2), CircleShape)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ORDER CANCELLED",
                    color = Color(0xFFDC2626),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, stepName ->
                    val isDone = index <= currentStep
                    val isCurrent = index == currentStep

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Node Circle
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(
                                    when {
                                        isCurrent -> BuyJumpOrange
                                        isDone -> BuyJumpEmerald
                                        else -> Color(0xFFE2E8F0)
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color(0xFF94A3B8), CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Label
                        Text(
                            text = stepName,
                            fontSize = 10.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            color = when {
                                isCurrent -> BuyJumpOrange
                                isDone -> Color(0xFF0F172A)
                                else -> Color(0xFF94A3B8)
                            }
                        )
                    }

                    // Connecting Line between nodes
                    if (index < steps.size - 1) {
                        Box(
                            modifier = Modifier
                                .weight(0.7f)
                                .height(3.dp)
                                .offset(y = (-10).dp)
                                .background(
                                    if (index < currentStep) BuyJumpEmerald else Color(0xFFE2E8F0)
                                )
                        )
                    }
                }
            }
        }
    }
}
