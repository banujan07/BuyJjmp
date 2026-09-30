package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.BuyJumpRepository
import com.example.data.repository.BuyJumpRepositoryImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Screen {
    CUSTOMER_HOME,
    CUSTOMER_CATEGORIES,
    CUSTOMER_CART,
    CUSTOMER_ORDERS,
    CUSTOMER_PROFILE,
    CUSTOMER_SHOP,
    CUSTOMER_WISHLIST,
    PRODUCT_DETAIL,
    SELLER_STORE_VIEW,
    CHECKOUT,
    ORDER_TRACKING,
    LOGIN,
    REGISTER,
    SELLER_REGISTER,
    SELLER_PENDING_APPROVAL,
    SELLER_PORTAL,
    ADMIN_PORTAL
}

enum class SellerTab {
    DASHBOARD,
    PRODUCTS,
    ADD_PRODUCT,
    ORDERS,
    EARNINGS
}

enum class AdminTab {
    DASHBOARD,
    PRODUCT_APPROVALS,
    CATEGORIES,
    SELLERS,
    COMMISSION,
    ORDERS,
    PAYOUTS,
    USERS
}

enum class ProductSort {
    NEWEST,
    PRICE_LOW_HIGH,
    PRICE_HIGH_LOW,
    POPULAR,
    RATING
}

class BuyJumpViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BuyJumpRepository = BuyJumpRepositoryImpl.getInstance(application)

    val currentUser = repository.currentUser
    val products = repository.products
    val categories = repository.categories
    val sellers = repository.sellers
    val orders = repository.orders
    val cartItems = repository.cartItems
    val wishlistItems = repository.wishlistItems
    val commissionSetting = repository.commissionSetting
    val payouts = repository.payouts

    // Navigation State
    private val _currentScreen = MutableStateFlow(Screen.CUSTOMER_HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _screenHistory = mutableListOf<Screen>()

    private val _sellerTab = MutableStateFlow(SellerTab.DASHBOARD)
    val sellerTab: StateFlow<SellerTab> = _sellerTab.asStateFlow()

    private val _adminTab = MutableStateFlow(AdminTab.DASHBOARD)
    val adminTab: StateFlow<AdminTab> = _adminTab.asStateFlow()

    // Selected Entities
    private val _selectedProductId = MutableStateFlow<String?>(null)
    val selectedProductId: StateFlow<String?> = _selectedProductId.asStateFlow()

    private val _selectedSellerId = MutableStateFlow<String?>(null)
    val selectedSellerId: StateFlow<String?> = _selectedSellerId.asStateFlow()

    private val _selectedOrderId = MutableStateFlow<String?>(null)
    val selectedOrderId: StateFlow<String?> = _selectedOrderId.asStateFlow()

    // Search & Filter State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _selectedSort = MutableStateFlow(ProductSort.POPULAR)
    val selectedSort: StateFlow<ProductSort> = _selectedSort.asStateFlow()

    private val _recentSearches = MutableStateFlow(listOf("Headphones", "Sneakers", "Smartwatch", "Hoodie"))
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    // Toast / Feedback State
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Loading / Action State
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Filtered Customer Products
    val customerVisibleProducts = combine(
        products,
        searchQuery,
        selectedCategoryFilter,
        selectedSort
    ) { allProds, query, catFilter, sort ->
        var list = allProds.filter { it.isCustomerVisible }

        if (catFilter != null) {
            list = list.filter { it.categoryId == catFilter }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) ||
                        it.description.lowercase().contains(q) ||
                        it.categoryName.lowercase().contains(q) ||
                        it.storeName.lowercase().contains(q)
            }
        }

        when (sort) {
            ProductSort.NEWEST -> list.sortedByDescending { it.createdAt }
            ProductSort.PRICE_LOW_HIGH -> list.sortedBy { it.price }
            ProductSort.PRICE_HIGH_LOW -> list.sortedByDescending { it.price }
            ProductSort.POPULAR -> list.sortedByDescending { it.soldCount }
            ProductSort.RATING -> list.sortedByDescending { it.rating }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart calculations
    val cartSubtotal = cartItems.map { items ->
        items.filter { it.isSelected }.sumOf { it.itemTotal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartTotalItemsCount = cartItems.map { items ->
        items.sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Navigation Methods
    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            _screenHistory.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (_screenHistory.isNotEmpty()) {
            val prev = _screenHistory.removeAt(_screenHistory.lastIndex)
            _currentScreen.value = prev
            return true
        }
        if (_currentScreen.value != Screen.CUSTOMER_HOME) {
            _currentScreen.value = Screen.CUSTOMER_HOME
            return true
        }
        return false
    }

    fun setSellerTab(tab: SellerTab) {
        _sellerTab.value = tab
    }

    fun setAdminTab(tab: AdminTab) {
        _adminTab.value = tab
    }

    fun openProductDetail(productId: String) {
        _selectedProductId.value = productId
        navigateTo(Screen.PRODUCT_DETAIL)
    }

    fun openSellerStore(sellerId: String) {
        _selectedSellerId.value = sellerId
        navigateTo(Screen.SELLER_STORE_VIEW)
    }

    fun openOrderTracking(orderId: String) {
        _selectedOrderId.value = orderId
        navigateTo(Screen.ORDER_TRACKING)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank() && !_recentSearches.value.contains(query.trim())) {
            _recentSearches.value = (listOf(query.trim()) + _recentSearches.value).take(6)
        }
    }

    fun setCategoryFilter(categoryId: String?) {
        _selectedCategoryFilter.value = categoryId
    }

    fun setSort(sort: ProductSort) {
        _selectedSort.value = sort
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _selectedCategoryFilter.value = null
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    // Auth Actions
    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.login(email, pass)
            _isLoading.value = false
            result.onSuccess { user ->
                when {
                    user.isAdmin -> {
                        showMessage("Welcome, Administrator!")
                        _currentScreen.value = Screen.ADMIN_PORTAL
                    }
                    user.isSeller -> {
                        if (user.status == Seller.STATUS_PENDING) {
                            _currentScreen.value = Screen.SELLER_PENDING_APPROVAL
                        } else {
                            showMessage("Welcome back to Seller Portal!")
                            _currentScreen.value = Screen.SELLER_PORTAL
                        }
                    }
                    else -> {
                        showMessage("Welcome back, ${user.name}!")
                        _currentScreen.value = Screen.CUSTOMER_HOME
                    }
                }
            }.onFailure { err ->
                showMessage(err.message ?: "Login failed")
            }
        }
    }

    fun registerCustomer(name: String, email: String, phone: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.registerCustomer(name, email, phone, pass)
            _isLoading.value = false
            result.onSuccess {
                showMessage("Account created successfully!")
                _currentScreen.value = Screen.CUSTOMER_HOME
            }.onFailure {
                showMessage(it.message ?: "Registration failed")
            }
        }
    }

    fun registerSeller(
        fullName: String,
        email: String,
        phone: String,
        pass: String,
        storeName: String,
        description: String,
        businessAddress: String,
        pickupAddress: String,
        logoUri: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.registerSeller(
                fullName, email, phone, pass, storeName, description, businessAddress, pickupAddress, logoUri
            )
            _isLoading.value = false
            result.onSuccess {
                showMessage("Seller application submitted for Admin approval!")
                _currentScreen.value = Screen.SELLER_PENDING_APPROVAL
            }.onFailure {
                showMessage(it.message ?: "Seller registration failed")
            }
        }
    }

    fun logout() {
        repository.logout()
        _screenHistory.clear()
        _currentScreen.value = Screen.CUSTOMER_HOME
        showMessage("Logged out successfully")
    }

    // Cart Actions
    fun addToCart(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            repository.addToCart(product, quantity)
            showMessage("Added to Cart: ${product.title}")
        }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            repository.removeFromCart(productId)
        }
    }

    fun updateCartQuantity(productId: String, newQty: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(productId, newQty)
        }
    }

    fun toggleCartSelection(productId: String) {
        viewModelScope.launch {
            repository.toggleCartSelection(productId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    fun requestPayout(sellerId: String, amount: Double, bankDetails: String) {
        viewModelScope.launch {
            val res = repository.requestPayout(sellerId, amount, bankDetails)
            res.onSuccess {
                showMessage("Payout request submitted successfully")
            }.onFailure {
                showMessage("Payout request failed: ${it.message}")
            }
        }
    }

    // Wishlist Actions
    fun toggleWishlist(product: Product) {
        viewModelScope.launch {
            val res = repository.toggleWishlist(product)
            res.onSuccess { saved ->
                if (saved) showMessage("Added to Wishlist") else showMessage("Removed from Wishlist")
            }
        }
    }

    fun isProductInWishlist(productId: String): Boolean {
        return repository.isInWishlist(productId)
    }

    // Checkout & Order Actions
    fun placeOrder(
        shippingAddress: String,
        phone: String,
        instructions: String,
        paymentMethod: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.createOrder(shippingAddress, phone, instructions, paymentMethod)
            _isLoading.value = false
            result.onSuccess { order ->
                showMessage("Order placed successfully! Tracking ID: ${order.id}")
                _selectedOrderId.value = order.id
                _currentScreen.value = Screen.ORDER_TRACKING
            }.onFailure { err ->
                showMessage("Checkout failed: ${err.message}")
            }
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            val result = repository.cancelOrder(orderId)
            result.onSuccess {
                showMessage("Order $orderId has been cancelled")
            }.onFailure {
                showMessage("Cannot cancel: ${it.message}")
            }
        }
    }

    // Seller Product Actions
    fun addSellerProduct(
        title: String,
        description: String,
        categoryId: String,
        categoryName: String,
        price: Double,
        originalPrice: Double,
        discountPercent: Int,
        stock: Int,
        images: List<String>,
        specifications: Map<String, String>
    ) {
        val user = currentUser.value ?: return
        val seller = sellers.value.find { it.userId == user.id }
        val storeName = seller?.storeName ?: "Seller Store"
        val sellerId = seller?.id ?: user.id

        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.addProduct(
                sellerId = sellerId,
                storeName = storeName,
                title = title,
                description = description,
                categoryId = categoryId,
                categoryName = categoryName,
                price = price,
                originalPrice = originalPrice,
                discountPercent = discountPercent,
                stock = stock,
                images = images,
                specifications = specifications
            )
            _isLoading.value = false
            result.onSuccess {
                showMessage("Product submitted! Pending Admin approval before going live.")
                _sellerTab.value = SellerTab.PRODUCTS
            }.onFailure {
                showMessage("Failed to save product: ${it.message}")
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
            showMessage("Product removed")
        }
    }

    // Admin Actions
    fun approveProduct(productId: String) {
        viewModelScope.launch {
            val res = repository.approveProduct(productId)
            res.onSuccess {
                showMessage("Product approved and activated in marketplace!")
            }
        }
    }

    fun rejectProduct(productId: String, reason: String) {
        viewModelScope.launch {
            val res = repository.rejectProduct(productId, reason)
            res.onSuccess {
                showMessage("Product rejected")
            }
        }
    }

    fun approveSeller(sellerId: String) {
        viewModelScope.launch {
            val res = repository.approveSeller(sellerId)
            res.onSuccess {
                showMessage("Seller approved! They can now manage their store.")
            }
        }
    }

    fun rejectSeller(sellerId: String, reason: String) {
        viewModelScope.launch {
            val res = repository.rejectSeller(sellerId, reason)
            res.onSuccess {
                showMessage("Seller rejected")
            }
        }
    }

    fun toggleSellerStatus(sellerId: String) {
        viewModelScope.launch {
            val res = repository.toggleSellerStatus(sellerId)
            res.onSuccess {
                showMessage("Seller status updated to: ${it.status}")
            }
        }
    }

    fun addCategory(name: String, description: String, imageUrl: String, commissionRate: Double) {
        viewModelScope.launch {
            val res = repository.addCategory(name, description, imageUrl, commissionRate)
            res.onSuccess {
                showMessage("Category '${it.name}' created with ${it.commissionRate}% commission")
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            repository.deleteCategory(categoryId)
            showMessage("Category deleted")
        }
    }

    fun toggleCategoryStatus(categoryId: String) {
        viewModelScope.launch {
            val res = repository.toggleCategoryStatus(categoryId)
            res.onSuccess {
                showMessage("Category status updated to: ${it.status}")
            }
        }
    }

    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            val res = repository.updateOrderStatus(orderId, status)
            res.onSuccess {
                showMessage("Order $orderId status changed to: $status")
            }
        }
    }

    fun updateCommissionSettings(globalRate: Double, categoryRates: Map<String, Double>) {
        viewModelScope.launch {
            val res = repository.updateCommissionSettings(globalRate, categoryRates)
            res.onSuccess {
                showMessage("Commission rates updated successfully")
            }
        }
    }

    fun updatePayoutStatus(payoutId: String, status: String, paymentRef: String) {
        viewModelScope.launch {
            val res = repository.updatePayoutStatus(payoutId, status, paymentRef)
            res.onSuccess {
                showMessage("Payout $payoutId marked as: $status")
            }
        }
    }

    // Local Image Upload from Device Disk
    suspend fun uploadImageFile(uri: Uri): Result<String> {
        return repository.uploadLocalImage(getApplication(), uri)
    }
}
