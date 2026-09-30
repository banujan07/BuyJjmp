package com.example.ui.screens.admin

import androidx.compose.foundation.background
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
import com.example.data.model.*
import com.example.ui.components.ConfirmDialog
import com.example.ui.theme.BuyJumpBlue
import com.example.ui.theme.BuyJumpEmerald
import com.example.ui.theme.BuyJumpOrange
import com.example.ui.theme.BuyJumpRed
import com.example.ui.viewmodel.AdminTab
import com.example.ui.viewmodel.BuyJumpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPortalScreen(
    viewModel: BuyJumpViewModel
) {
    val currentTab by viewModel.adminTab.collectAsState()
    val products by viewModel.products.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val sellers by viewModel.sellers.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val payouts by viewModel.payouts.collectAsState()
    val commissionSetting by viewModel.commissionSetting.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }

    // Platform Aggregates
    val pendingProductApprovals = products.filter { it.approvalStatus == Product.APPROVAL_PENDING }
    val pendingSellerApprovals = sellers.filter { it.status == Seller.STATUS_PENDING }
    val totalSalesAmount = orders.sumOf { it.totalAmount }
    val totalCommissionAmount = orders.flatMap { it.items }.sumOf { it.commissionAmount }
    val totalSellerPayables = totalSalesAmount - totalCommissionAmount
    val pendingPayoutsAmount = payouts.filter { it.status == SellerPayout.STATUS_PENDING }.sumOf { it.netAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("BuyJump Admin Console", fontWeight = FontWeight.Black, fontSize = 17.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(color = BuyJumpRed, shape = RoundedCornerShape(4.dp)) {
                            Text("ADMIN", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }, modifier = Modifier.testTag("admin_logout_button")) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Log Out", tint = BuyJumpRed)
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
                    selected = currentTab == AdminTab.DASHBOARD,
                    onClick = { viewModel.setAdminTab(AdminTab.DASHBOARD) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 9.sp) }
                )
                NavigationBarItem(
                    selected = currentTab == AdminTab.PRODUCT_APPROVALS,
                    onClick = { viewModel.setAdminTab(AdminTab.PRODUCT_APPROVALS) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (pendingProductApprovals.isNotEmpty()) {
                                    Badge(containerColor = BuyJumpRed) {
                                        Text("${pendingProductApprovals.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.FactCheck, contentDescription = "Approvals")
                        }
                    },
                    label = { Text("Approvals", fontSize = 9.sp) }
                )
                NavigationBarItem(
                    selected = currentTab == AdminTab.CATEGORIES,
                    onClick = { viewModel.setAdminTab(AdminTab.CATEGORIES) },
                    icon = { Icon(Icons.Default.Category, contentDescription = "Categories") },
                    label = { Text("Categories", fontSize = 9.sp) }
                )
                NavigationBarItem(
                    selected = currentTab == AdminTab.SELLERS,
                    onClick = { viewModel.setAdminTab(AdminTab.SELLERS) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (pendingSellerApprovals.isNotEmpty()) {
                                    Badge(containerColor = BuyJumpOrange) {
                                        Text("${pendingSellerApprovals.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = "Sellers")
                        }
                    },
                    label = { Text("Sellers", fontSize = 9.sp) }
                )
                NavigationBarItem(
                    selected = currentTab == AdminTab.COMMISSION,
                    onClick = { viewModel.setAdminTab(AdminTab.COMMISSION) },
                    icon = { Icon(Icons.Default.Percent, contentDescription = "Rates") },
                    label = { Text("Rates", fontSize = 9.sp) }
                )
                NavigationBarItem(
                    selected = currentTab == AdminTab.ORDERS,
                    onClick = { viewModel.setAdminTab(AdminTab.ORDERS) },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Orders") },
                    label = { Text("Orders", fontSize = 9.sp) }
                )
                NavigationBarItem(
                    selected = currentTab == AdminTab.PAYOUTS,
                    onClick = { viewModel.setAdminTab(AdminTab.PAYOUTS) },
                    icon = { Icon(Icons.Default.Paid, contentDescription = "Payouts") },
                    label = { Text("Payouts", fontSize = 9.sp) }
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
                AdminTab.DASHBOARD -> AdminDashboardView(
                    totalSales = totalSalesAmount,
                    commission = totalCommissionAmount,
                    payables = totalSellerPayables,
                    pendingPayouts = pendingPayoutsAmount,
                    totalProducts = products.size,
                    pendingProducts = pendingProductApprovals.size,
                    totalSellers = sellers.size,
                    pendingSellers = pendingSellerApprovals.size,
                    totalOrders = orders.size,
                    onNavigateToTab = { viewModel.setAdminTab(it) }
                )
                AdminTab.PRODUCT_APPROVALS -> AdminProductApprovalsView(
                    pendingProducts = pendingProductApprovals,
                    onApprove = { viewModel.approveProduct(it) },
                    onReject = { id, reason -> viewModel.rejectProduct(id, reason) }
                )
                AdminTab.CATEGORIES -> AdminCategoriesView(
                    categories = categories,
                    onAddCategory = { name, desc, img, rate -> viewModel.addCategory(name, desc, img, rate) },
                    onToggleStatus = { viewModel.toggleCategoryStatus(it) },
                    onDeleteCategory = { viewModel.deleteCategory(it) }
                )
                AdminTab.SELLERS -> AdminSellersView(
                    sellers = sellers,
                    onApprove = { viewModel.approveSeller(it) },
                    onReject = { id, reason -> viewModel.rejectSeller(id, reason) },
                    onToggleStatus = { viewModel.toggleSellerStatus(it) }
                )
                AdminTab.COMMISSION -> AdminCommissionView(
                    settings = commissionSetting,
                    categories = categories,
                    onSave = { global, rates -> viewModel.updateCommissionSettings(global, rates) }
                )
                AdminTab.ORDERS -> AdminOrdersView(
                    orders = orders,
                    onUpdateStatus = { id, st -> viewModel.updateOrderStatus(id, st) }
                )
                AdminTab.PAYOUTS -> AdminPayoutsView(
                    payouts = payouts,
                    onMarkPaid = { id, ref -> viewModel.updatePayoutStatus(id, SellerPayout.STATUS_PAID, ref) }
                )
                AdminTab.USERS -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("User management")
                }
            }
        }
    }

    if (showLogoutDialog) {
        ConfirmDialog(
            show = true,
            title = "Exit Admin Console",
            message = "Return to BuyJump marketplace customer view?",
            confirmButtonText = "Exit Admin",
            onConfirm = {
                viewModel.logout()
                showLogoutDialog = false
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@Composable
private fun AdminDashboardView(
    totalSales: Double,
    commission: Double,
    payables: Double,
    pendingPayouts: Double,
    totalProducts: Int,
    pendingProducts: Int,
    totalSellers: Int,
    pendingSellers: Int,
    totalOrders: Int,
    onNavigateToTab: (AdminTab) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Marketplace Financials & Revenue", fontWeight = FontWeight.Black, fontSize = 17.sp)
        }

        // Commission & Gross Sales Hero Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BuyJumpBlue),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("BuyJump Platform Commission Earned", color = Color(0xFF93C5FD), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text("$${String.format("%.2f", commission)}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Total GMV Sales", color = Color(0xFF93C5FD), fontSize = 11.sp)
                            Text("$${String.format("%.2f", totalSales)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("Seller Payables", color = Color(0xFF93C5FD), fontSize = 11.sp)
                            Text("$${String.format("%.2f", payables)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("Pending Payouts", color = Color(0xFF93C5FD), fontSize = 11.sp)
                            Text("$${String.format("%.2f", pendingPayouts)}", color = BuyJumpOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Platform Action Center
        item {
            Text("Pending Management Actions", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminStatCard(
                    title = "Pending Products",
                    value = "$pendingProducts",
                    badge = if (pendingProducts > 0) "Review" else "Clear",
                    color = BuyJumpOrange,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(AdminTab.PRODUCT_APPROVALS) }
                )
                AdminStatCard(
                    title = "Pending Sellers",
                    value = "$pendingSellers",
                    badge = if (pendingSellers > 0) "Approve" else "Clear",
                    color = BuyJumpRed,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(AdminTab.SELLERS) }
                )
            }
        }

        // Overall Counts
        item {
            Text("Platform Ecosystem", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminStatCard("Total Products", "$totalProducts", "Catalog", BuyJumpBlue, Modifier.weight(1f)) {
                    onNavigateToTab(AdminTab.PRODUCT_APPROVALS)
                }
                AdminStatCard("Total Sellers", "$totalSellers", "Merchants", BuyJumpEmerald, Modifier.weight(1f)) {
                    onNavigateToTab(AdminTab.SELLERS)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminStatCard("Total Orders", "$totalOrders", "Fulfilled", BuyJumpBlue, Modifier.weight(1f)) {
                    onNavigateToTab(AdminTab.ORDERS)
                }
                AdminStatCard("Commission System", "Configurable", "Rules", BuyJumpOrange, Modifier.weight(1f)) {
                    onNavigateToTab(AdminTab.COMMISSION)
                }
            }
        }
    }
}

@Composable
private fun AdminStatCard(
    title: String,
    value: String,
    badge: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clip(RoundedCornerShape(14.dp)),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(4.dp)) {
                    Text(badge, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// 1. Product Approvals Screen (Section 18)
@Composable
private fun AdminProductApprovalsView(
    pendingProducts: List<Product>,
    onApprove: (String) -> Unit,
    onReject: (String, String) -> Unit
) {
    var rejectDialogTarget by remember { mutableStateOf<Product?>(null) }
    var rejectionReason by remember { mutableStateOf("Image quality does not meet marketplace standards") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Product Approvals (${pendingProducts.size})", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text("Review products submitted by sellers before they become visible in the customer marketplace", fontSize = 12.sp, color = Color(0xFF64748B))
        }

        if (pendingProducts.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, null, tint = BuyJumpEmerald, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("All product submissions reviewed!", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        items(pendingProducts) { prod ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(prod.primaryImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = prod.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(prod.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Store: ${prod.storeName} • Cat: ${prod.categoryName}", fontSize = 11.sp, color = BuyJumpBlue)
                            Text("Price: $${String.format("%.2f", prod.price)} • Stock: ${prod.stock}", fontSize = 12.sp, color = BuyJumpOrange, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(prod.description, fontSize = 12.sp, color = Color(0xFF475569), maxLines = 3)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { onApprove(prod.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = BuyJumpEmerald),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve & Publish", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { rejectDialogTarget = prod },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BuyJumpRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reject", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    if (rejectDialogTarget != null) {
        AlertDialog(
            onDismissRequest = { rejectDialogTarget = null },
            title = { Text("Reject Product", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Provide rejection reason for seller:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        rejectDialogTarget?.let { onReject(it.id, rejectionReason) }
                        rejectDialogTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BuyJumpRed)
                ) {
                    Text("Confirm Rejection")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { rejectDialogTarget = null }) { Text("Cancel") }
            }
        )
    }
}

// 2. Categories Management (Section 8)
@Composable
private fun AdminCategoriesView(
    categories: List<Category>,
    onAddCategory: (String, String, String, Double) -> Unit,
    onToggleStatus: (String) -> Unit,
    onDeleteCategory: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var catName by remember { mutableStateOf("") }
    var catDesc by remember { mutableStateOf("") }
    var catImage by remember { mutableStateOf("") }
    var catCommission by remember { mutableStateOf("10.0") }

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
                Column {
                    Text("Dynamic Categories (${categories.size})", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("Adding categories syncs immediately to all apps", fontSize = 11.sp, color = Color(0xFF64748B))
                }
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BuyJumpBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", fontSize = 12.sp)
                }
            }
        }

        items(categories) { cat ->
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
                            .data(cat.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = cat.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(50.dp).clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(cat.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(cat.description, fontSize = 11.sp, color = Color(0xFF64748B), maxLines = 1)
                        Text("Commission Rate: ${cat.commissionRate}%", fontSize = 11.sp, color = BuyJumpOrange, fontWeight = FontWeight.Bold)
                    }

                    Switch(
                        checked = cat.isActive,
                        onCheckedChange = { onToggleStatus(cat.id) }
                    )

                    IconButton(onClick = { onDeleteCategory(cat.id) }) {
                        Icon(Icons.Default.DeleteOutline, null, tint = BuyJumpRed)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Dynamic Category", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        label = { Text("Category Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = catDesc,
                        onValueChange = { catDesc = it },
                        label = { Text("Short Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = catCommission,
                        onValueChange = { catCommission = it },
                        label = { Text("Commission Rate (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = catImage,
                        onValueChange = { catImage = it },
                        label = { Text("Banner Image URL") },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rate = catCommission.toDoubleOrNull() ?: 10.0
                        onAddCategory(catName, catDesc, catImage, rate)
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BuyJumpBlue)
                ) {
                    Text("Create Category")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// 3. Sellers Management (Section 30)
@Composable
private fun AdminSellersView(
    sellers: List<Seller>,
    onApprove: (String) -> Unit,
    onReject: (String, String) -> Unit,
    onToggleStatus: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Marketplace Sellers (${sellers.size})", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }

        items(sellers) { sel ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(sel.logoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = sel.storeName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(50.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(sel.storeName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Owner: ${sel.fullName} • ${sel.email}", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("Total GMV Sales: $${String.format("%.2f", sel.totalSales)}", fontSize = 11.sp, color = BuyJumpOrange, fontWeight = FontWeight.Bold)
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (sel.status) {
                                Seller.STATUS_ACTIVE -> Color(0xFFDCFCE7)
                                Seller.STATUS_REJECTED -> Color(0xFFFEE2E2)
                                else -> Color(0xFFFEF3C7)
                            }
                        ) {
                            Text(
                                text = sel.status.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (sel.status) {
                                    Seller.STATUS_ACTIVE -> BuyJumpEmerald
                                    Seller.STATUS_REJECTED -> BuyJumpRed
                                    else -> Color(0xFFB45309)
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (sel.status == Seller.STATUS_PENDING) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onApprove(sel.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = BuyJumpEmerald),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Approve Seller", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { onReject(sel.id, "Verification documents incomplete") },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BuyJumpRed),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reject", fontSize = 12.sp)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { onToggleStatus(sel.id) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (sel.status == Seller.STATUS_ACTIVE) "Deactivate Store" else "Reactivate Store", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// 4. Commission Configuration (Section 26)
@Composable
private fun AdminCommissionView(
    settings: CommissionSetting,
    categories: List<Category>,
    onSave: (Double, Map<String, Double>) -> Unit
) {
    var globalRate by remember { mutableStateOf(settings.globalRatePercent.toString()) }
    val ratesMap = remember { mutableStateMapOf<String, String>().apply {
        settings.categoryRates.forEach { (k, v) -> put(k, v.toString()) }
    } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Marketplace Commission Rules", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text("Configure platform take-rate globally and per merchandise category", fontSize = 12.sp, color = Color(0xFF64748B))
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Global Fallback Commission Rate (%)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = globalRate,
                        onValueChange = { globalRate = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Text("Category Specific Rates", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        items(categories) { cat ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(cat.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(cat.description, fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                    var currentText = ratesMap[cat.id] ?: "${cat.commissionRate}"
                    OutlinedTextField(
                        value = currentText,
                        onValueChange = {
                            ratesMap[cat.id] = it
                        },
                        label = { Text("%") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.width(90.dp)
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    val g = globalRate.toDoubleOrNull() ?: 10.0
                    val converted = ratesMap.mapValues { it.value.toDoubleOrNull() ?: 10.0 }
                    onSave(g, converted)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BuyJumpOrange),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("SAVE COMMISSION RULES", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 5. Orders Management
@Composable
private fun AdminOrdersView(
    orders: List<Order>,
    onUpdateStatus: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("All Platform Orders (${orders.size})", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }

        items(orders) { ord ->
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

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Customer: ${ord.customerName} (${ord.customerEmail})", fontSize = 12.sp, color = Color(0xFF475569))
                    Text("Total Amount: $${String.format("%.2f", ord.totalAmount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BuyJumpOrange)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { onUpdateStatus(ord.id, Order.STATUS_PROCESSING) },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Process", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { onUpdateStatus(ord.id, Order.STATUS_SHIPPED) },
                            colors = ButtonDefaults.buttonColors(containerColor = BuyJumpOrange),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Ship", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { onUpdateStatus(ord.id, Order.STATUS_DELIVERED) },
                            colors = ButtonDefaults.buttonColors(containerColor = BuyJumpEmerald),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Deliver", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

// 6. Payouts Management
@Composable
private fun AdminPayoutsView(
    payouts: List<SellerPayout>,
    onMarkPaid: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Seller Payout Requests (${payouts.size})", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }

        items(payouts) { p ->
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
                        Text(p.id, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BuyJumpBlue)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (p.status == SellerPayout.STATUS_PAID) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = p.status.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (p.status == SellerPayout.STATUS_PAID) BuyJumpEmerald else Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Store: ${p.storeName}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Net Payout Amount: $${String.format("%.2f", p.netAmount)}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = BuyJumpOrange)
                    Text("Bank: ${p.bankDetails}", fontSize = 11.sp, color = Color(0xFF64748B))

                    if (p.status == SellerPayout.STATUS_PENDING) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onMarkPaid(p.id, "ACH-TXN-" + System.currentTimeMillis().toString().takeLast(6)) },
                            colors = ButtonDefaults.buttonColors(containerColor = BuyJumpEmerald),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Mark as Transferred & Paid")
                        }
                    }
                }
            }
        }
    }
}
