package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.components.ProductCard
import com.example.ui.theme.BuyJumpBlue
import com.example.ui.theme.BuyJumpOrange
import com.example.ui.viewmodel.BuyJumpViewModel
import com.example.ui.viewmodel.ProductSort

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerShopScreen(
    viewModel: BuyJumpViewModel,
    onBack: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val products by viewModel.customerVisibleProducts.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val wishlistItems by viewModel.wishlistItems.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Search Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("shop_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search products, stores, brands...", fontSize = 13.sp) },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BuyJumpBlue,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("shop_search_input")
                    )
                }

                // Recent searches tags when empty search
                if (searchQuery.isEmpty() && recentSearches.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(recentSearches) { recent ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { viewModel.setSearchQuery(recent) }
                            ) {
                                Text(
                                    text = recent,
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Filter & Sort Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { viewModel.setCategoryFilter(null) },
                            label = { Text("All", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BuyJumpBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    items(categories.filter { it.isActive }) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.id,
                            onClick = {
                                if (selectedCategory == cat.id) viewModel.setCategoryFilter(null)
                                else viewModel.setCategoryFilter(cat.id)
                            },
                            label = { Text(cat.name, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BuyJumpBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Sort Dropdown
                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier.testTag("sort_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort Products",
                            tint = BuyJumpBlue
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Popular") },
                            onClick = {
                                viewModel.setSort(ProductSort.POPULAR)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (selectedSort == ProductSort.POPULAR) Icon(Icons.Default.Check, null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Newest") },
                            onClick = {
                                viewModel.setSort(ProductSort.NEWEST)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (selectedSort == ProductSort.NEWEST) Icon(Icons.Default.Check, null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Price: Low to High") },
                            onClick = {
                                viewModel.setSort(ProductSort.PRICE_LOW_HIGH)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (selectedSort == ProductSort.PRICE_LOW_HIGH) Icon(Icons.Default.Check, null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Price: High to Low") },
                            onClick = {
                                viewModel.setSort(ProductSort.PRICE_HIGH_LOW)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (selectedSort == ProductSort.PRICE_HIGH_LOW) Icon(Icons.Default.Check, null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Highest Rated") },
                            onClick = {
                                viewModel.setSort(ProductSort.RATING)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (selectedSort == ProductSort.RATING) Icon(Icons.Default.Check, null)
                            }
                        )
                    }
                }
            }
        }

        // Results count info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${products.size} products found",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B)
            )
            if (selectedCategory != null || searchQuery.isNotEmpty()) {
                Text(
                    text = "Clear filters",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BuyJumpOrange,
                    modifier = Modifier.clickable { viewModel.clearSearch() }
                )
            }
        }

        // Product Grid or Empty State
        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No products found",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Try adjusting your search query or removing category filters",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.clearSearch() },
                        colors = ButtonDefaults.buttonColors(containerColor = BuyJumpBlue)
                    ) {
                        Text("Reset Filters")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                val pairs = products.chunked(2)
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
}
