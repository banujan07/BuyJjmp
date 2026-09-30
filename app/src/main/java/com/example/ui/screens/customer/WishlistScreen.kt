package com.example.ui.screens.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.BuyJumpBlue
import com.example.ui.theme.BuyJumpOrange
import com.example.ui.viewmodel.BuyJumpViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    viewModel: BuyJumpViewModel,
    onBack: () -> Unit
) {
    val wishlistItems by viewModel.wishlistItems.collectAsState()
    val products by viewModel.products.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Wishlist (${wishlistItems.size})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        if (wishlistItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Your wishlist is empty", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Save items you want to buy later by tapping the heart icon!", fontSize = 13.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 4.dp))
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = { viewModel.navigateTo(Screen.CUSTOMER_SHOP) },
                        colors = ButtonDefaults.buttonColors(containerColor = BuyJumpBlue)
                    ) {
                        Text("Discover Products")
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
                items(wishlistItems) { item ->
                    val prod = products.find { it.id == item.productId }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { viewModel.openProductDetail(item.productId) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(item.productImage)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = item.productTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(72.dp)
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
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$${String.format("%.2f", item.price)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = BuyJumpOrange
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                IconButton(
                                    onClick = { prod?.let { viewModel.toggleWishlist(it) } }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Remove",
                                        tint = Color(0xFFEF4444)
                                    )
                                }

                                if (prod != null && prod.stock > 0) {
                                    FilledTonalButton(
                                        onClick = {
                                            viewModel.addToCart(prod)
                                            viewModel.toggleWishlist(prod)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Move to Cart", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
