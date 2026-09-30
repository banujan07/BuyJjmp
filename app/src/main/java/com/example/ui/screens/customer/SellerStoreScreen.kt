package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Seller
import com.example.ui.components.ProductCard
import com.example.ui.theme.BuyJumpBlue
import com.example.ui.theme.BuyJumpEmerald
import com.example.ui.theme.BuyJumpOrange
import com.example.ui.viewmodel.BuyJumpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerStoreScreen(
    sellerId: String,
    viewModel: BuyJumpViewModel,
    onBack: () -> Unit
) {
    val sellers by viewModel.sellers.collectAsState()
    val products by viewModel.products.collectAsState()
    val wishlistItems by viewModel.wishlistItems.collectAsState()

    val seller = sellers.find { it.id == sellerId }
    val storeProducts = products.filter { it.sellerId == sellerId && it.isCustomerVisible }

    if (seller == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Store not found")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(seller.storeName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Store Banner & Profile Header
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(seller.logoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = seller.storeName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = seller.storeName,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified Seller",
                                        tint = BuyJumpBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${seller.rating} Positive Rating",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = seller.storeDescription,
                            fontSize = 12.sp,
                            color = Color(0xFF475569),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = "${storeProducts.size} Products",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BuyJumpBlue,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFECFDF5)
                            ) {
                                Text(
                                    text = "Active Verified Seller",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BuyJumpEmerald,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Products Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Store Products (${storeProducts.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Product Grid
            val pairs = storeProducts.chunked(2)
            items(pairs) { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (prod in pair) {
                        ProductCard(
                            product = prod,
                            onProductClick = { viewModel.openProductDetail(it.id) },
                            onAddToCart = { viewModel.addToCart(it) },
                            onToggleWishlist = { viewModel.toggleWishlist(it) },
                            isWishlisted = wishlistItems.any { it.productId == prod.id },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
