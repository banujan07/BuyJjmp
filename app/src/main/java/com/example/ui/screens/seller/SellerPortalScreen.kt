package com.example.ui.screens.seller

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.SellerPayout
import com.example.ui.components.BuyJumpLogo
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.ImageUploadPicker
import com.example.ui.theme.BuyJumpBlue
import com.example.ui.theme.BuyJumpEmerald
import com.example.ui.theme.BuyJumpOrange
import com.example.ui.theme.BuyJumpRed
import com.example.ui.viewmodel.BuyJumpViewModel
import com.example.ui.viewmodel.SellerTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerPortalScreen(
    viewModel: BuyJumpViewModel
) {
    val currentTab by viewModel.sellerTab.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val sellers by viewModel.sellers.collectAsState()
    val products by viewModel.products.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val payouts by viewModel.payouts.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val seller = sellers.find { it.userId == currentUser?.id } ?: sellers.firstOrNull()
    val sellerId = seller?.id.orEmpty()

    val myProducts = products.filter { it.sellerId == sellerId }
    val myOrders = orders.filter { order -> order.sellerIds.contains(sellerId) }
    val myPayouts = payouts.filter { it.sellerId == sellerId }

    // Stats
    val totalSales = seller?.totalSales ?: 0.0
    val totalOrdersCount = myOrders.size
    val pendingApprovalCount = myProducts.count { it.approvalStatus == Product.APPROVAL_PENDING }
    val activeCount = myProducts.count { it.approvalStatus == Product.APPROVAL_APPROVED && it.status == Product.STATUS_ACTIVE }
    val outOfStockCount = myProducts.count { it.stock <= 0 }

    val myOrderItems = myOrders.flatMap { it.items }.filter { it.sellerId == sellerId }
    val grossSales = myOrderItems.sumOf { it.total }
    val totalCommissionDeducted = myOrderItems.sumOf { it.commissionAmount }
    val netSellerEarnings = grossSales - totalCommissionDeducted
    val pendingPayoutAmount = myPayouts.filter { it.status == SellerPayout.STATUS_PENDING }.sumOf { it.netAmount }
    val paidPayoutAmount = myPayouts.filter { it.status == SellerPayout.STATUS_PAID }.sumOf { it.netAmount }

    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(seller?.storeName ?: "Seller Portal", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(4.dp)) {
                                Text("VERIFIED SELLER", color = BuyJumpEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                        Text("BuyJump Merchant Center", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Exit to Customer View", tint = BuyJumpRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == SellerTab.DASHBOARD,
                    onClick = { viewModel.setSellerTab(SellerTab.DASHBOARD) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = currentTab == SellerTab.PRODUCTS,
                    onClick = { viewModel.setSellerTab(SellerTab.PRODUCTS) },
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "Products") },
                    label = { Text("Products", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = currentTab == SellerTab.ADD_PRODUCT,
                    onClick = { viewModel.setSellerTab(SellerTab.ADD_PRODUCT) },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "Add") },
                    label = { Text("Add Product", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = currentTab == SellerTab.ORDERS,
                    onClick = { viewModel.setSellerTab(SellerTab.ORDERS) },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Orders") },
                    label = { Text("Orders", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = currentTab == SellerTab.EARNINGS,
                    onClick = { viewModel.setSellerTab(SellerTab.EARNINGS) },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Earnings") },
                    label = { Text("Earnings", fontSize = 10.sp) }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentTab) {
                SellerTab.DASHBOARD -> SellerDashboardView(
                    totalSales = if (grossSales > 0) grossSales else totalSales,
                    commission = totalCommissionDeducted,
                    netEarnings = netSellerEarnings,
                    pendingPayout = pendingPayoutAmount,
                    paidPayout = paidPayoutAmount,
                    totalProducts = myProducts.size,
                    activeProducts = activeCount,
                    pendingProducts = pendingApprovalCount,
                    outOfStock = outOfStockCount,
                    totalOrders = totalOrdersCount,
                    recentOrders = myOrders.take(3),
                    onGoToAddProduct = { viewModel.setSellerTab(SellerTab.ADD_PRODUCT) },
                    onGoToOrders = { viewModel.setSellerTab(SellerTab.ORDERS) }
                )
                SellerTab.PRODUCTS -> SellerProductsListView(
                    products = myProducts,
                    onAddNew = { viewModel.setSellerTab(SellerTab.ADD_PRODUCT) },
                    onDelete = { viewModel.deleteProduct(it) }
                )
                SellerTab.ADD_PRODUCT -> SellerAddProductView(
                    viewModel = viewModel,
                    categories = categories.filter { it.isActive },
                    onProductCreated = { viewModel.setSellerTab(SellerTab.PRODUCTS) }
                )
                SellerTab.ORDERS -> SellerOrdersListView(
                    orders = myOrders,
                    sellerId = sellerId,
                    onUpdateStatus = { orderId, status -> viewModel.updateOrderStatus(orderId, status) }
                )
                SellerTab.EARNINGS -> SellerEarningsView(
                    grossSales = grossSales,
                    commission = totalCommissionDeducted,
                    netEarnings = netSellerEarnings,
                    pendingPayout = pendingPayoutAmount,
                    paidPayout = paidPayoutAmount,
                    payouts = myPayouts,
                    onRequestPayout = { amt, bank ->
                        viewModel.requestPayout(sellerId, amt, bank)
                    }
                )
            }
        }
    }

    if (showLogoutDialog) {
        ConfirmDialog(
            show = true,
            title = "Exit Seller Portal",
            message = "Return to BuyJump marketplace customer view?",
            confirmButtonText = "Exit to Shopping",
            onConfirm = {
                viewModel.logout()
                showLogoutDialog = false
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@Composable
private fun SellerDashboardView(
    totalSales: Double,
    commission: Double,
    netEarnings: Double,
    pendingPayout: Double,
    paidPayout: Double,
    totalProducts: Int,
    activeProducts: Int,
    pendingProducts: Int,
    outOfStock: Int,
    totalOrders: Int,
    recentOrders: List<Order>,
    onGoToAddProduct: () -> Unit,
    onGoToOrders: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Store Performance Overview", fontWeight = FontWeight.Black, fontSize = 18.sp)
        }

        // Primary Financial Summary
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BuyJumpBlue),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Total Net Earnings", color = Color(0xFF93C5FD), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text("$${String.format("%.2f", netEarnings)}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Gross Sales", color = Color(0xFF93C5FD), fontSize = 11.sp)
                            Text("$${String.format("%.2f", totalSales)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("Marketplace Commission", color = Color(0xFF93C5FD), fontSize = 11.sp)
                            Text("$${String.format("%.2f", commission)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("Pending Payout", color = Color(0xFF93C5FD), fontSize = 11.sp)
                            Text("$${String.format("%.2f", pendingPayout)}", color = BuyJumpOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Product & Inventory Metrics Grid
        item {
            Text("Inventory & Order Status", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricMiniCard("Total Products", "$totalProducts", Icons.Default.Inventory2, BuyJumpBlue, Modifier.weight(1f))
                MetricMiniCard("Active Live", "$activeProducts", Icons.Default.CheckCircle, BuyJumpEmerald, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricMiniCard("Pending Approval", "$pendingProducts", Icons.Default.Pending, BuyJumpOrange, Modifier.weight(1f))
                MetricMiniCard("Out of Stock", "$outOfStock", Icons.Default.Warning, BuyJumpRed, Modifier.weight(1f))
            }
        }

        // Quick Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onGoToAddProduct,
                    colors = ButtonDefaults.buttonColors(containerColor = BuyJumpOrange),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Product", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onGoToOrders,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Receipt, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View Orders", fontSize = 12.sp)
                }
            }
        }

        // Recent Orders Section
        if (recentOrders.isNotEmpty()) {
            item {
                Text("Recent Customer Orders", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            items(recentOrders) { ord ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(ord.id, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BuyJumpBlue)
                            Text("${ord.items.size} items • to ${ord.customerName}", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEFF6FF)
                        ) {
                            Text(ord.status.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BuyJumpBlue, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricMiniCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(value, fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(title, fontSize = 11.sp, color = Color(0xFF64748B))
            }
        }
    }
}

@Composable
private fun SellerProductsListView(
    products: List<Product>,
    onAddNew: () -> Unit,
    onDelete: (String) -> Unit
) {
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Manage Products (${products.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Button(
                    onClick = onAddNew,
                    colors = ButtonDefaults.buttonColors(containerColor = BuyJumpBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Product", fontSize = 12.sp)
                }
            }
        }

        items(products) { prod ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(prod.primaryImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = prod.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(prod.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("$${String.format("%.2f", prod.price)} • Stock: ${prod.stock}", fontSize = 12.sp, color = BuyJumpOrange, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(4.dp))

                        // Approval Status Badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (prod.approvalStatus) {
                                Product.APPROVAL_APPROVED -> Color(0xFFDCFCE7)
                                Product.APPROVAL_REJECTED -> Color(0xFFFEE2E2)
                                else -> Color(0xFFFEF3C7)
                            }
                        ) {
                            Text(
                                text = when (prod.approvalStatus) {
                                    Product.APPROVAL_APPROVED -> "APPROVED & LIVE"
                                    Product.APPROVAL_REJECTED -> "REJECTED: ${prod.rejectionReason ?: "Policy"}"
                                    else -> "PENDING ADMIN APPROVAL"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (prod.approvalStatus) {
                                    Product.APPROVAL_APPROVED -> BuyJumpEmerald
                                    Product.APPROVAL_REJECTED -> BuyJumpRed
                                    else -> Color(0xFFB45309)
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    IconButton(onClick = { productToDelete = prod }) {
                        Icon(Icons.Default.DeleteOutline, null, tint = BuyJumpRed)
                    }
                }
            }
        }
    }

    if (productToDelete != null) {
        ConfirmDialog(
            show = true,
            title = "Delete Product",
            message = "Are you sure you want to delete '${productToDelete?.title}'?",
            confirmButtonText = "Delete",
            isDestructive = true,
            onConfirm = {
                productToDelete?.let { onDelete(it.id) }
                productToDelete = null
            },
            onDismiss = { productToDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SellerAddProductView(
    viewModel: BuyJumpViewModel,
    categories: List<com.example.data.model.Category>,
    onProductCreated: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var originalPriceText by remember { mutableStateOf("") }
    var discountText by remember { mutableStateOf("0") }
    var stockText by remember { mutableStateOf("10") }
    var selectedCatId by remember { mutableStateOf(categories.firstOrNull()?.id.orEmpty()) }
    var uploadedImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var specKey by remember { mutableStateOf("") }
    var specVal by remember { mutableStateOf("") }
    val specs = remember { mutableStateMapOf<String, String>() }

    var formError by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Add New Product", fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text("Products submitted will be reviewed by Admin before appearing in customer search", fontSize = 12.sp, color = Color(0xFF64748B))
        }

        if (formError != null) {
            item {
                Surface(color = Color(0xFFFEF2F2), shape = RoundedCornerShape(8.dp)) {
                    Text(formError.orEmpty(), color = BuyJumpRed, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                }
            }
        }

        // Image Upload Section (Real local disk/device file picker)
        item {
            ImageUploadPicker(
                images = uploadedImages,
                onImagesChanged = { uploadedImages = it },
                onUploadUri = { viewModel.uploadImageFile(it) },
                maxImages = 4,
                label = "Product Images (Device Gallery / Disk)"
            )
        }

        item {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Product Title *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("add_product_title_input")
            )
        }

        // Category Selection
        item {
            Text("Category *", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            var expanded by remember { mutableStateOf(false) }
            val currentCatName = categories.find { it.id == selectedCatId }?.name ?: "Select Category"

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = currentCatName,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = {
                                selectedCatId = cat.id
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Price ($) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f).testTag("add_product_price_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = originalPriceText,
                    onValueChange = { originalPriceText = it },
                    label = { Text("Original Price ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = discountText,
                    onValueChange = { discountText = it },
                    label = { Text("Discount %") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = stockText,
                    onValueChange = { stockText = it },
                    label = { Text("Initial Stock *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("add_product_stock_input"),
                    singleLine = true
                )
            }
        }

        item {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description *") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth().testTag("add_product_desc_input")
            )
        }

        item {
            Button(
                onClick = {
                    val p = priceText.toDoubleOrNull()
                    val s = stockText.toIntOrNull()
                    if (title.isBlank() || p == null || s == null || description.isBlank()) {
                        formError = "Please fill in title, valid price, stock, and description"
                    } else {
                        val catName = categories.find { it.id == selectedCatId }?.name ?: "General"
                        viewModel.addSellerProduct(
                            title = title,
                            description = description,
                            categoryId = selectedCatId,
                            categoryName = catName,
                            price = p,
                            originalPrice = originalPriceText.toDoubleOrNull() ?: p,
                            discountPercent = discountText.toIntOrNull() ?: 0,
                            stock = s,
                            images = uploadedImages,
                            specifications = specs.toMap()
                        )
                        onProductCreated()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("submit_product_button"),
                colors = ButtonDefaults.buttonColors(containerColor = BuyJumpOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SUBMIT PRODUCT FOR APPROVAL", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SellerOrdersListView(
    orders: List<Order>,
    sellerId: String,
    onUpdateStatus: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Store Orders (${orders.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (orders.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("No orders placed yet for your products", color = Color(0xFF64748B))
                }
            }
        }

        items(orders) { ord ->
            val myItems = ord.items.filter { it.sellerId == sellerId }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(ord.id, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BuyJumpBlue)
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFEFF6FF)) {
                            Text(ord.status.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BuyJumpBlue, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Customer: ${ord.customerName} • ${ord.phone}", fontSize = 12.sp, color = Color(0xFF475569))
                    Text("Address: ${ord.shippingAddress}", fontSize = 12.sp, color = Color(0xFF64748B))

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    myItems.forEach { itm ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${itm.quantity}x ${itm.productTitle}", fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Text("$${String.format("%.2f", itm.total)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Update order fulfillment status
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (ord.status == Order.STATUS_CONFIRMED) {
                            Button(
                                onClick = { onUpdateStatus(ord.id, Order.STATUS_PROCESSING) },
                                colors = ButtonDefaults.buttonColors(containerColor = BuyJumpBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Mark Processing", fontSize = 11.sp)
                            }
                        }
                        if (ord.status == Order.STATUS_PROCESSING) {
                            Button(
                                onClick = { onUpdateStatus(ord.id, Order.STATUS_SHIPPED) },
                                colors = ButtonDefaults.buttonColors(containerColor = BuyJumpOrange),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Mark Shipped", fontSize = 11.sp)
                            }
                        }
                        if (ord.status == Order.STATUS_SHIPPED) {
                            Button(
                                onClick = { onUpdateStatus(ord.id, Order.STATUS_DELIVERED) },
                                colors = ButtonDefaults.buttonColors(containerColor = BuyJumpEmerald),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Mark Delivered", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SellerEarningsView(
    grossSales: Double,
    commission: Double,
    netEarnings: Double,
    pendingPayout: Double,
    paidPayout: Double,
    payouts: List<SellerPayout>,
    onRequestPayout: (Double, String) -> Unit
) {
    var showRequestSheet by remember { mutableStateOf(false) }
    var withdrawalAmount by remember { mutableStateOf("") }
    var bankAccountInfo by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Seller Earnings & Marketplace Payouts", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Earnings Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gross Sales", color = Color(0xFF64748B), fontSize = 13.sp)
                        Text("$${String.format("%.2f", grossSales)}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Marketplace Commission Deducted", color = Color(0xFF64748B), fontSize = 13.sp)
                        Text("-$${String.format("%.2f", commission)}", fontWeight = FontWeight.SemiBold, color = BuyJumpRed, fontSize = 13.sp)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Net Seller Payable", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("$${String.format("%.2f", netEarnings)}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = BuyJumpEmerald)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showRequestSheet = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BuyJumpOrange),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AccountBalance, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Request Payout")
                    }
                }
            }
        }

        item {
            Text("Payout Records (${payouts.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        items(payouts) { payout ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(payout.id, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BuyJumpBlue)
                        Text("Net Amount: $${String.format("%.2f", payout.netAmount)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Bank: ${payout.bankDetails}", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (payout.status) {
                            SellerPayout.STATUS_PAID -> Color(0xFFDCFCE7)
                            SellerPayout.STATUS_PROCESSING -> Color(0xFFEFF6FF)
                            else -> Color(0xFFFEF3C7)
                        }
                    ) {
                        Text(
                            text = payout.status.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (payout.status) {
                                SellerPayout.STATUS_PAID -> BuyJumpEmerald
                                SellerPayout.STATUS_PROCESSING -> BuyJumpBlue
                                else -> Color(0xFFB45309)
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }

    if (showRequestSheet) {
        AlertDialog(
            onDismissRequest = { showRequestSheet = false },
            title = { Text("Request Earnings Payout", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = withdrawalAmount,
                        onValueChange = { withdrawalAmount = it },
                        label = { Text("Withdrawal Amount ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = bankAccountInfo,
                        onValueChange = { bankAccountInfo = it },
                        label = { Text("Bank / Account Routing Info") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = withdrawalAmount.toDoubleOrNull() ?: 100.0
                        onRequestPayout(amt, bankAccountInfo.ifEmpty { "Direct Bank Deposit" })
                        showRequestSheet = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BuyJumpOrange)
                ) {
                    Text("Submit Payout Request")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRequestSheet = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
