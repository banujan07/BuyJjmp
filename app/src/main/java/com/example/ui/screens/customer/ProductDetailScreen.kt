package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Product
import com.example.ui.components.RatingStars
import com.example.ui.theme.BuyJumpBlue
import com.example.ui.theme.BuyJumpEmerald
import com.example.ui.theme.BuyJumpOrange
import com.example.ui.theme.BuyJumpRed
import com.example.ui.viewmodel.BuyJumpViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    viewModel: BuyJumpViewModel,
    onBack: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val wishlistItems by viewModel.wishlistItems.collectAsState()
    val product = products.find { it.id == productId }

    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Product not found")
        }
        return
    }

    val isWishlisted = wishlistItems.any { it.productId == product.id }
    var selectedImageIndex by remember { mutableIntStateOf(0) }
    var purchaseQuantity by remember { mutableIntStateOf(1) }

    val images = if (product.images.isNotEmpty()) product.images else listOf("https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=700")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleWishlist(product) },
                        modifier = Modifier.testTag("detail_wishlist_button")
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isWishlisted) BuyJumpRed else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.CUSTOMER_CART) },
                        modifier = Modifier.testTag("detail_cart_button")
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            // Add to Cart & Buy Now Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.addToCart(product, purchaseQuantity)
                        },
                        enabled = product.stock > 0,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("detail_add_cart_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BuyJumpBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add to Cart", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.addToCart(product, purchaseQuantity)
                            viewModel.navigateTo(Screen.CHECKOUT)
                        },
                        enabled = product.stock > 0,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("detail_buy_now_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = BuyJumpOrange),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buy Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            // Main Image Display
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color(0xFFF8FAFC))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(images.getOrNull(selectedImageIndex) ?: images.first())
                            .crossfade(true)
                            .build(),
                        contentDescription = product.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )

                    if (product.discountPercent > 0) {
                        Surface(
                            color = BuyJumpRed,
                            shape = RoundedCornerShape(bottomEnd = 12.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                text = "${product.discountPercent}% OFF",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Image Thumbnails Row
            if (images.size > 1) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(images) { idx, imgUrl ->
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = if (selectedImageIndex == idx) 2.dp else 1.dp,
                                        color = if (selectedImageIndex == idx) BuyJumpOrange else Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedImageIndex = idx }
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(imgUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            // Product Details Block
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Category Chip & Stock badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEFF6FF)
                            ) {
                                Text(
                                    text = product.categoryName,
                                    color = BuyJumpBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (product.stock > 0) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                            ) {
                                Text(
                                    text = if (product.stock > 0) "In Stock (${product.stock} units)" else "Out of Stock",
                                    color = if (product.stock > 0) BuyJumpEmerald else BuyJumpRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Title
                        Text(
                            text = product.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Rating & Reviews & Sold count
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RatingStars(rating = product.rating, starSize = 16.dp, reviewCount = product.reviewCount)
                            Text(text = "•", color = Color(0xFF94A3B8))
                            Text(
                                text = "${product.soldCount} sold",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Price Row
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$${String.format("%.2f", product.price)}",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = BuyJumpOrange
                            )
                            if (product.originalPrice > product.price) {
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "$${String.format("%.2f", product.originalPrice)}",
                                    fontSize = 15.sp,
                                    textDecoration = TextDecoration.LineThrough,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier.padding(bottom = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Sold by Seller Card (Section 10)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { viewModel.openSellerStore(product.sellerId) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color(0xFFEFF6FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = BuyJumpBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sold by",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = product.storeName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BuyJumpBlue
                            )
                        }
                        Button(
                            onClick = { viewModel.openSellerStore(product.sellerId) },
                            colors = ButtonDefaults.buttonColors(containerColor = BuyJumpBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Visit Store", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Quantity Selector (Prevents purchase above available stock!)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Quantity",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledTonalIconButton(
                                onClick = { if (purchaseQuantity > 1) purchaseQuantity-- },
                                enabled = purchaseQuantity > 1,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease")
                            }

                            Text(
                                text = "$purchaseQuantity",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            FilledTonalIconButton(
                                onClick = {
                                    if (purchaseQuantity < product.stock) purchaseQuantity++
                                },
                                enabled = purchaseQuantity < product.stock,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase")
                            }
                        }
                    }
                }
            }

            // Description
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Product Description",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = product.description,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            // Specifications Table
            if (product.specifications.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Specifications",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            product.specifications.forEach { (specKey, specVal) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = specKey,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF64748B),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = specVal,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1.2f)
                                    )
                                }
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                            }
                        }
                    }
                }
            }
        }
    }
}
