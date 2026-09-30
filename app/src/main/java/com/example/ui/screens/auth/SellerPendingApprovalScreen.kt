package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BuyJumpLogo
import com.example.ui.theme.BuyJumpBlue
import com.example.ui.theme.BuyJumpOrange
import com.example.ui.viewmodel.BuyJumpViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun SellerPendingApprovalScreen(
    viewModel: BuyJumpViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val sellers by viewModel.sellers.collectAsState()
    val seller = sellers.find { it.userId == currentUser?.id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BuyJumpLogo(size = 50.dp, tagline = "Seller Network")

        Spacer(modifier = Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .size(72.dp)
                .background(Color(0xFFFEF3C7), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.HourglassTop,
                contentDescription = null,
                tint = BuyJumpOrange,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Seller Application Pending Review",
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Thank you for applying to sell on BuyJump! Your store '${seller?.storeName ?: "Your Store"}' is currently under review by our Admin team to ensure compliance with marketplace standards.",
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFF64748B),
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Verification Status", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Color(0xFFFEF3C7), shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = "STATUS: PENDING ADMIN APPROVAL",
                            color = Color(0xFFB45309),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Once approved, you will be able to upload products, manage inventory, view orders, and receive payouts.",
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedButton(
            onClick = { viewModel.logout() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Back to Marketplace")
        }
    }
}
