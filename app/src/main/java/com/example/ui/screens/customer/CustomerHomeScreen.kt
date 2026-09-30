package com.example.ui.screens.customer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.Category
import com.example.data.model.Product
import com.example.data.model.Seller
import com.example.ui.components.BuyJumpLogo
import com.example.ui.components.ProductCard
import com.example.ui.theme.BuyJumpBlue
import com.example.ui.theme.BuyJumpOrange
import com.example.ui.theme.BuyJumpRed
import com.example.ui.viewmodel.BuyJumpViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun CustomerHomeScreen(
    viewModel: BuyJumpViewModel,
    onNavigateToShop: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val products by viewModel.customerVisibleProducts.collectAsState()
    val sellers by viewModel.sellers.collectAsState()
    val cartCount by viewModel.cartTotalItemsCount.collectAsState()
    val wishlistItems by viewModel.wishlistItems.collectAsState()
    val activeCategories = categories.filter { it.isActive }
    val flashDeals = products.filter { it.isFlashDeal }
    val featuredProducts = products.filter { it.isFeatured }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Logo & Action Icons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BuyJumpLogo(size = 36.dp, tagline = "Marketplace")

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Cart with Badge
                        IconButton(
                            onClick = onNavigateToCart,
                            modifier = Modifier.testTag("home_cart_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (cartCount > 0) {
                                        Badge(
                                            containerColor = BuyJumpOrange,
                                            contentColor = Color.White
                                        ) {
                                            Text(text = "$cartCount", fontSize = 10.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Cart",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Notification Icon
                        IconButton(
                            onClick = onNavigateToNotifications,
                            modifier = Modifier.testTag("home_notif_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Profile Icon
                        IconButton(
                            onClick = onNavigateToProfile,
                            modifier = Modifier.testTag("home_profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                tint = BuyJumpBlue
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar Trigger
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clickable { onNavigateToShop() }
                        .testTag("home_search_bar_trigger")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Search 10,000+ products, stores, brands...",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Main Home Scrollable Feed
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Hero Promo Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(150.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigateToShop() }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_promo_banner),
                        contentDescription = "Mega Sale Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Gradient Overlay with call-to-action
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.6f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Column {
                            Surface(
                                color = BuyJumpOrange,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "BUYJUMP EXCLUSIVE",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "SUPER MEGA DEALS",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Up to 50% Off Top Brands",
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Categories Horizontal Scroll
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Categories",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "See All",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BuyJumpBlue,
                            modifier = Modifier.clickable { viewModel.navigateTo(Screen.CUSTOMER_CATEGORIES) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(activeCategories) { category ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(72.dp)
                                    .clickable {
                                        viewModel.setCategoryFilter(category.id)
                                        viewModel.navigateTo(Screen.CUSTOMER_SHOP)
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEFF6FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(category.imageUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = category.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = category.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // Flash Deals Section
            if (flashDeals.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = BuyJumpOrange,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Flash Deals",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = BuyJumpRed,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "08:42:15",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "View More",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BuyJumpOrange,
                                modifier = Modifier.clickable { onNavigateToShop() }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(flashDeals) { product ->
                                ProductCard(
                                    product = product,
                                    onProductClick = { viewModel.openProductDetail(it.id) },
                                    onAddToCart = { viewModel.addToCart(it) },
                                    onToggleWishlist = { viewModel.toggleWishlist(it) },
                                    isWishlisted = wishlistItems.any { it.productId == product.id },
                                    modifier = Modifier.width(170.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Top Verified Seller Stores
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Featured Seller Stores",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(sellers.filter { it.isApproved }) { seller ->
                            Card(
                                modifier = Modifier
                                    .width(200.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { viewModel.openSellerStore(seller.id) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(seller.logoUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = seller.storeName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = seller.storeName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color(0xFFF59E0B),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "${seller.rating}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Featured & Recommended Products
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp, start = 16.dp, end = 16.dp)
                ) {
                    Text(
                        text = "Recommended For You",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Products Grid in Column
            val chunkedProducts = products.chunked(2)
            items(chunkedProducts) { pair ->
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
