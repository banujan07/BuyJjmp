package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Product
import com.example.ui.theme.BuyJumpBlue
import com.example.ui.theme.BuyJumpEmerald
import com.example.ui.theme.BuyJumpOrange
import com.example.ui.theme.BuyJumpRed

@Composable
fun ProductCard(
    product: Product,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onToggleWishlist: (Product) -> Unit,
    isWishlisted: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onProductClick(product) }
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column {
            // Image Container with Badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color(0xFFF1F5F9))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.primaryImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Discount Badge
                if (product.discountPercent > 0) {
                    Surface(
                        color = BuyJumpRed,
                        shape = RoundedCornerShape(topStart = 0.dp, bottomEnd = 10.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "-${product.discountPercent}%",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Wishlist Icon Button
                IconButton(
                    onClick = { onToggleWishlist(product) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.85f), CircleShape)
                        .testTag("wishlist_btn_${product.id}")
                ) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Wishlist",
                        tint = if (isWishlisted) BuyJumpRed else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Out of stock overlay
                if (product.stock <= 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "OUT OF STOCK",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Info Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Seller Store Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = BuyJumpBlue,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = product.storeName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BuyJumpBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Title
                Text(
                    text = product.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.height(36.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Rating & Sold Count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RatingStars(
                        rating = product.rating,
                        starSize = 13.dp,
                        showNumeric = true
                    )
                    if (product.soldCount > 0) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${product.soldCount} sold",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Price and Add to Cart Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "$${String.format("%.2f", product.price)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = BuyJumpOrange
                        )
                        if (product.originalPrice > product.price) {
                            Text(
                                text = "$${String.format("%.2f", product.originalPrice)}",
                                fontSize = 11.sp,
                                textDecoration = TextDecoration.LineThrough,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Quick Add to Cart Button
                    IconButton(
                        onClick = { onAddToCart(product) },
                        enabled = product.stock > 0,
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                if (product.stock > 0) BuyJumpBlue else Color(0xFFE2E8F0),
                                RoundedCornerShape(10.dp)
                            )
                            .testTag("add_cart_btn_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddShoppingCart,
                            contentDescription = "Add to Cart",
                            tint = if (product.stock > 0) Color.White else Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
