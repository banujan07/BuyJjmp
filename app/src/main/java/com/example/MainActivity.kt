package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.admin.AdminPortalScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.auth.SellerPendingApprovalScreen
import com.example.ui.screens.auth.SellerRegisterScreen
import com.example.ui.screens.customer.*
import com.example.ui.screens.seller.SellerPortalScreen
import com.example.ui.theme.BuyJumpBlue
import com.example.ui.theme.BuyJumpOrange
import com.example.ui.theme.BuyJumpTheme
import com.example.ui.viewmodel.BuyJumpViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: BuyJumpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BuyJumpTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val cartCount by viewModel.cartTotalItemsCount.collectAsState()
                val userMessage by viewModel.userMessage.collectAsState()

                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()

                // Trigger snackbar on user message
                LaunchedEffect(userMessage) {
                    userMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearMessage()
                    }
                }

                // Handle system back navigation
                val isCustomerSubscreen = currentScreen !in listOf(
                    Screen.CUSTOMER_HOME,
                    Screen.CUSTOMER_CATEGORIES,
                    Screen.CUSTOMER_CART,
                    Screen.CUSTOMER_ORDERS,
                    Screen.CUSTOMER_PROFILE,
                    Screen.SELLER_PORTAL,
                    Screen.ADMIN_PORTAL
                )

                BackHandler(enabled = isCustomerSubscreen || currentScreen != Screen.CUSTOMER_HOME) {
                    viewModel.navigateBack()
                }

                val showBottomNav = currentScreen in listOf(
                    Screen.CUSTOMER_HOME,
                    Screen.CUSTOMER_CATEGORIES,
                    Screen.CUSTOMER_CART,
                    Screen.CUSTOMER_ORDERS,
                    Screen.CUSTOMER_PROFILE
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        if (showBottomNav) {
                            // Section 32: Exactly Customer Navigation (Home, Categories, Cart, Orders, Profile).
                            // Strictly NO Admin, NO Seller, NO Role Switcher!
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp
                            ) {
                                NavigationBarItem(
                                    selected = currentScreen == Screen.CUSTOMER_HOME,
                                    onClick = { viewModel.navigateTo(Screen.CUSTOMER_HOME) },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("Home", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = BuyJumpBlue,
                                        selectedTextColor = BuyJumpBlue,
                                        indicatorColor = BuyJumpBlue.copy(alpha = 0.12f)
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen == Screen.CUSTOMER_CATEGORIES,
                                    onClick = { viewModel.navigateTo(Screen.CUSTOMER_CATEGORIES) },
                                    icon = { Icon(Icons.Default.Category, contentDescription = "Categories") },
                                    label = { Text("Categories", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = BuyJumpBlue,
                                        selectedTextColor = BuyJumpBlue,
                                        indicatorColor = BuyJumpBlue.copy(alpha = 0.12f)
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen == Screen.CUSTOMER_CART,
                                    onClick = { viewModel.navigateTo(Screen.CUSTOMER_CART) },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                if (cartCount > 0) {
                                                    Badge(containerColor = BuyJumpOrange) {
                                                        Text("$cartCount")
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                                        }
                                    },
                                    label = { Text("Cart", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = BuyJumpBlue,
                                        selectedTextColor = BuyJumpBlue,
                                        indicatorColor = BuyJumpBlue.copy(alpha = 0.12f)
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen == Screen.CUSTOMER_ORDERS,
                                    onClick = { viewModel.navigateTo(Screen.CUSTOMER_ORDERS) },
                                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Orders") },
                                    label = { Text("Orders", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = BuyJumpBlue,
                                        selectedTextColor = BuyJumpBlue,
                                        indicatorColor = BuyJumpBlue.copy(alpha = 0.12f)
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen == Screen.CUSTOMER_PROFILE,
                                    onClick = { viewModel.navigateTo(Screen.CUSTOMER_PROFILE) },
                                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                    label = { Text("Profile", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = BuyJumpBlue,
                                        selectedTextColor = BuyJumpBlue,
                                        indicatorColor = BuyJumpBlue.copy(alpha = 0.12f)
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            Screen.CUSTOMER_HOME -> CustomerHomeScreen(
                                viewModel = viewModel,
                                onNavigateToShop = { viewModel.navigateTo(Screen.CUSTOMER_SHOP) },
                                onNavigateToCart = { viewModel.navigateTo(Screen.CUSTOMER_CART) },
                                onNavigateToNotifications = { viewModel.showMessage("No new notifications") },
                                onNavigateToProfile = { viewModel.navigateTo(Screen.CUSTOMER_PROFILE) }
                            )
                            Screen.CUSTOMER_CATEGORIES -> CustomerCategoriesScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.navigateBack() }
                            )
                            Screen.CUSTOMER_SHOP -> CustomerShopScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.navigateBack() }
                            )
                            Screen.PRODUCT_DETAIL -> {
                                val prodId = viewModel.selectedProductId.collectAsState().value
                                if (prodId != null) {
                                    ProductDetailScreen(
                                        productId = prodId,
                                        viewModel = viewModel,
                                        onBack = { viewModel.navigateBack() }
                                    )
                                }
                            }
                            Screen.SELLER_STORE_VIEW -> {
                                val selId = viewModel.selectedSellerId.collectAsState().value
                                if (selId != null) {
                                    SellerStoreScreen(
                                        sellerId = selId,
                                        viewModel = viewModel,
                                        onBack = { viewModel.navigateBack() }
                                    )
                                }
                            }
                            Screen.CUSTOMER_CART -> CartScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.navigateBack() }
                            )
                            Screen.CHECKOUT -> CheckoutScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.navigateBack() }
                            )
                            Screen.CUSTOMER_ORDERS -> CustomerOrdersScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.navigateBack() }
                            )
                            Screen.ORDER_TRACKING -> {
                                val ordId = viewModel.selectedOrderId.collectAsState().value
                                if (ordId != null) {
                                    OrderTrackingScreen(
                                        orderId = ordId,
                                        viewModel = viewModel,
                                        onBack = { viewModel.navigateBack() }
                                    )
                                }
                            }
                            Screen.CUSTOMER_WISHLIST -> WishlistScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.navigateBack() }
                            )
                            Screen.CUSTOMER_PROFILE -> CustomerProfileScreen(
                                viewModel = viewModel
                            )
                            Screen.LOGIN -> LoginScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.navigateBack() }
                            )
                            Screen.REGISTER -> RegisterScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.navigateBack() }
                            )
                            Screen.SELLER_REGISTER -> SellerRegisterScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.navigateBack() }
                            )
                            Screen.SELLER_PENDING_APPROVAL -> SellerPendingApprovalScreen(
                                viewModel = viewModel
                            )
                            Screen.SELLER_PORTAL -> SellerPortalScreen(
                                viewModel = viewModel
                            )
                            Screen.ADMIN_PORTAL -> AdminPortalScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
