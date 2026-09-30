package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.firebase.*
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class BuyJumpRepositoryImpl private constructor(private val appContext: Context) : BuyJumpRepository {

    private val firestoreService: FirebaseFirestoreService = FirebaseFirestoreServiceImpl()
    private val authService: FirebaseAuthService = FirebaseAuthServiceImpl(firestoreService)

    init {
        FirebaseManager.init(appContext)
    }

    companion object {
        @Volatile
        private var INSTANCE: BuyJumpRepositoryImpl? = null

        fun getInstance(context: Context): BuyJumpRepositoryImpl {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BuyJumpRepositoryImpl(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // Default Seed Accounts
    private val defaultAdmin = User(
        id = "usr_admin_001",
        name = "BuyJump Administrator",
        email = "admin@buyjump.com",
        phone = "+1 (800) 555-0199",
        role = User.ROLE_ADMIN,
        status = "active"
    )

    private val defaultSellerUser = User(
        id = "usr_seller_001",
        name = "Alex Vance",
        email = "seller@buyjump.com",
        phone = "+1 (555) 432-1098",
        role = User.ROLE_SELLER,
        status = "active"
    )

    private val defaultCustomer = User(
        id = "usr_cust_001",
        name = "Sarah Jenkins",
        email = "customer@buyjump.com",
        phone = "+1 (555) 987-6543",
        role = User.ROLE_CUSTOMER,
        status = "active",
        addresses = listOf(
            ShippingAddress(
                id = "addr_1",
                label = "Home",
                recipientName = "Sarah Jenkins",
                phone = "+1 (555) 987-6543",
                street = "742 Evergreen Terrace",
                city = "Springfield",
                postalCode = "97477",
                isDefault = true
            )
        )
    )

    private val initialSellers = listOf(
        Seller(
            id = "sel_001",
            userId = "usr_seller_001",
            fullName = "Alex Vance",
            email = "seller@buyjump.com",
            phone = "+1 (555) 432-1098",
            storeName = "Apex Tech Official",
            storeDescription = "Authorized distributor for next-gen wireless gear, gadgets, and audio accessories.",
            businessAddress = "104 Silicon Parkway, San Jose, CA",
            pickupAddress = "Warehouse B, 104 Silicon Pkwy, San Jose, CA",
            logoUrl = "https://images.unsplash.com/photo-1550009158-9ebf69173e03?w=300",
            status = Seller.STATUS_ACTIVE,
            rating = 4.9,
            totalSales = 24800.0,
            totalOrders = 142
        ),
        Seller(
            id = "sel_002",
            userId = "usr_seller_002",
            fullName = "Elena Gomez",
            email = "urbanvogue@buyjump.com",
            phone = "+1 (555) 234-5678",
            storeName = "Urban Vogue Studios",
            storeDescription = "Curated street fashion, premium cotton hoodies, and contemporary everyday wear.",
            businessAddress = "45 Broadway Ave, New York, NY",
            pickupAddress = "Unit 12, Fashion District, NY",
            logoUrl = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=300",
            status = Seller.STATUS_ACTIVE,
            rating = 4.7,
            totalSales = 18500.0,
            totalOrders = 98
        ),
        Seller(
            id = "sel_003",
            userId = "usr_seller_003",
            fullName = "Marcus Cole",
            email = "solestyle@buyjump.com",
            phone = "+1 (555) 876-5432",
            storeName = "SoleStyle Originals",
            storeDescription = "Exclusive sneakers, running shoes, and athletic performance footwear.",
            businessAddress = "88 Olympic Way, Portland, OR",
            pickupAddress = "Dock 4, Logistics Center, Portland, OR",
            logoUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=300",
            status = Seller.STATUS_ACTIVE,
            rating = 4.8,
            totalSales = 31200.0,
            totalOrders = 160
        ),
        Seller(
            id = "sel_004",
            userId = "usr_seller_004",
            fullName = "Priya Sharma",
            email = "glowlab@buyjump.com",
            phone = "+1 (555) 345-6789",
            storeName = "GlowLab Organics",
            storeDescription = "Clean, certified organic skincare, botanical serums, and self-care essentials.",
            businessAddress = "12 Ocean Breeze Blvd, Miami, FL",
            pickupAddress = "Suite 300, Miami, FL",
            logoUrl = "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=300",
            status = Seller.STATUS_PENDING,
            rating = 4.6,
            totalSales = 0.0,
            totalOrders = 0
        )
    )

    private val initialCategories = listOf(
        Category(
            id = "cat_electronics",
            name = "Electronics",
            description = "Smartphones, Audio, Laptops & Accessories",
            imageUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500",
            iconName = "headphones",
            status = Category.STATUS_ACTIVE,
            commissionRate = 5.0
        ),
        Category(
            id = "cat_fashion",
            name = "Fashion & Apparel",
            description = "Men's, Women's & Kids' Trending Wear",
            imageUrl = "https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=500",
            iconName = "checkroom",
            status = Category.STATUS_ACTIVE,
            commissionRate = 10.0
        ),
        Category(
            id = "cat_shoes",
            name = "Shoes & Footwear",
            description = "Sneakers, Boots, Running & Casual",
            imageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=500",
            iconName = "skateboarding",
            status = Category.STATUS_ACTIVE,
            commissionRate = 12.0
        ),
        Category(
            id = "cat_beauty",
            name = "Beauty & Personal Care",
            description = "Skincare, Makeup, Fragrances & Wellness",
            imageUrl = "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=500",
            iconName = "spa",
            status = Category.STATUS_ACTIVE,
            commissionRate = 10.0
        ),
        Category(
            id = "cat_home",
            name = "Home & Living",
            description = "Decor, Furniture, Lighting & Kitchenware",
            imageUrl = "https://images.unsplash.com/photo-1513694203232-719a280e022f?w=500",
            iconName = "home",
            status = Category.STATUS_ACTIVE,
            commissionRate = 8.0
        ),
        Category(
            id = "cat_groceries",
            name = "Groceries & Gourmet",
            description = "Pantry Essentials, Organic Teas & Snacks",
            imageUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?w=500",
            iconName = "shopping_basket",
            status = Category.STATUS_ACTIVE,
            commissionRate = 6.0
        )
    )

    private val initialProducts = listOf(
        Product(
            id = "prod_001",
            sellerId = "sel_001",
            storeName = "Apex Tech Official",
            title = "Apex Pro ANC Wireless Headphones",
            description = "Engineered with 40mm titanium dynamic drivers and Hybrid Active Noise Cancellation up to 42dB. Features 50-hour playback, transparency mode, and ultra-comfortable memory foam ear cushions.",
            categoryId = "cat_electronics",
            categoryName = "Electronics",
            price = 89.99,
            originalPrice = 129.99,
            discountPercent = 30,
            stock = 45,
            images = listOf(
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=700",
                "https://images.unsplash.com/photo-1484704849700-f032a568e944?w=700"
            ),
            approvalStatus = Product.APPROVAL_APPROVED,
            status = Product.STATUS_ACTIVE,
            rating = 4.8,
            reviewCount = 312,
            soldCount = 840,
            isFeatured = true,
            isFlashDeal = true,
            specifications = mapOf(
                "Driver Size" to "40mm Titanium",
                "Battery Life" to "50 Hours",
                "Bluetooth" to "5.3",
                "Charging" to "USB-C Fast Charge (10m = 5h)"
            )
        ),
        Product(
            id = "prod_002",
            sellerId = "sel_003",
            storeName = "SoleStyle Originals",
            title = "AeroGlide Max Running Sneaker",
            description = "Lightweight responsive nitrogen-infused foam sole for explosive energy return. Breathable knit upper with reinforced heel support for all-day comfort.",
            categoryId = "cat_shoes",
            categoryName = "Shoes & Footwear",
            price = 119.50,
            originalPrice = 149.00,
            discountPercent = 20,
            stock = 28,
            images = listOf(
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=700",
                "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=700"
            ),
            approvalStatus = Product.APPROVAL_APPROVED,
            status = Product.STATUS_ACTIVE,
            rating = 4.9,
            reviewCount = 194,
            soldCount = 520,
            isFeatured = true,
            isFlashDeal = true,
            specifications = mapOf(
                "Sole" to "Nitrogen-Infused EVA",
                "Upper" to "Engineered Breathable Mesh",
                "Weight" to "240 grams (Size 9)",
                "Arch Support" to "Neutral to Medium"
            )
        ),
        Product(
            id = "prod_003",
            sellerId = "sel_002",
            storeName = "Urban Vogue Studios",
            title = "Heavyweight Oversized French Terry Hoodie",
            description = "Crafted from 100% combed organic cotton with a substantial 450 GSM weight. Features a relaxed drop-shoulder cut, double-layered hood, and ribbed cuffs.",
            categoryId = "cat_fashion",
            categoryName = "Fashion & Apparel",
            price = 54.00,
            originalPrice = 75.00,
            discountPercent = 28,
            stock = 62,
            images = listOf(
                "https://images.unsplash.com/photo-1556905055-8f358a7a47b2?w=700",
                "https://images.unsplash.com/photo-1509631179647-0177331693ae?w=700"
            ),
            approvalStatus = Product.APPROVAL_APPROVED,
            status = Product.STATUS_ACTIVE,
            rating = 4.7,
            reviewCount = 145,
            soldCount = 410,
            isFeatured = true,
            specifications = mapOf(
                "Material" to "100% Organic Cotton",
                "Weight" to "450 GSM French Terry",
                "Fit" to "Relaxed Oversized",
                "Care" to "Machine wash cold"
            )
        ),
        Product(
            id = "prod_004",
            sellerId = "sel_001",
            storeName = "Apex Tech Official",
            title = "ApexPulse Ultra Smartwatch Series 9",
            description = "Brilliant 1.96-inch AMOLED always-on display, sapphire glass, 100+ workout modes, continuous heart rate, SpO2, sleep tracking, and IP68 5ATM water resistance.",
            categoryId = "cat_electronics",
            categoryName = "Electronics",
            price = 149.00,
            originalPrice = 199.00,
            discountPercent = 25,
            stock = 34,
            images = listOf(
                "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=700",
                "https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?w=700"
            ),
            approvalStatus = Product.APPROVAL_APPROVED,
            status = Product.STATUS_ACTIVE,
            rating = 4.8,
            reviewCount = 280,
            soldCount = 670,
            isFeatured = true,
            isFlashDeal = true,
            specifications = mapOf(
                "Display" to "1.96\" AMOLED (410x502)",
                "Water Rating" to "5ATM + IP68",
                "Battery" to "Up to 12 Days",
                "Sensors" to "PPG Heart, SpO2, Accelerometer"
            )
        ),
        Product(
            id = "prod_005",
            sellerId = "sel_002",
            storeName = "Urban Vogue Studios",
            title = "Minimalist Waterproof Commuter Backpack",
            description = "Weatherproof polyurethane-coated shell with dedicated 16-inch padded laptop sleeve, magnetic Fidlock buckle, hidden passport pocket, and luggage strap.",
            categoryId = "cat_fashion",
            categoryName = "Fashion & Apparel",
            price = 68.00,
            originalPrice = 85.00,
            discountPercent = 20,
            stock = 25,
            images = listOf(
                "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=700"
            ),
            approvalStatus = Product.APPROVAL_APPROVED,
            status = Product.STATUS_ACTIVE,
            rating = 4.6,
            reviewCount = 92,
            soldCount = 280,
            specifications = mapOf(
                "Capacity" to "22 Liters",
                "Laptop Sleeve" to "Up to 16-inch MacBook Pro",
                "Water Resistance" to "IPX4 Weatherproof",
                "Material" to "Recycled Polyester + Matte PU"
            )
        ),
        Product(
            id = "prod_006",
            sellerId = "sel_001",
            storeName = "Apex Tech Official",
            title = "SoundSphere 360 Portable Speaker",
            description = "30W omnidirectional spatial audio with deep bass radiator, RGB ambient beat lighting, 24-hour battery, and true stereo wireless pairing.",
            categoryId = "cat_electronics",
            categoryName = "Electronics",
            price = 49.99,
            originalPrice = 69.99,
            discountPercent = 28,
            stock = 50,
            images = listOf(
                "https://images.unsplash.com/photo-1545454675-3531b543be5d?w=700"
            ),
            approvalStatus = Product.APPROVAL_APPROVED,
            status = Product.STATUS_ACTIVE,
            rating = 4.7,
            reviewCount = 110,
            soldCount = 340,
            isFlashDeal = true,
            specifications = mapOf(
                "Output Power" to "30W RMS",
                "Playtime" to "24 Hours (50% Volume)",
                "Waterproof" to "IPX7 Submersible",
                "Connection" to "Bluetooth 5.3 + AUX"
            )
        ),
        // Pending approval product submitted by seller (for Admin review demo)
        Product(
            id = "prod_007",
            sellerId = "sel_001",
            storeName = "Apex Tech Official",
            title = "Apex MagCharge 3-in-1 Foldable Station",
            description = "Simultaneous 15W wireless fast-charging for smartphone, smartwatch, and wireless earbuds. Sleek aluminum hinge fold design for effortless travel.",
            categoryId = "cat_electronics",
            categoryName = "Electronics",
            price = 45.00,
            originalPrice = 59.00,
            discountPercent = 23,
            stock = 100,
            images = listOf(
                "https://images.unsplash.com/photo-1586816879360-004f5b0c51e5?w=700"
            ),
            approvalStatus = Product.APPROVAL_PENDING,
            status = Product.STATUS_ACTIVE,
            rating = 5.0,
            reviewCount = 0,
            soldCount = 0,
            specifications = mapOf(
                "Input" to "USB-C PD 30W",
                "Phone Output" to "15W MagSafe Compatible",
                "Watch Output" to "5W Fast Charge",
                "Weight" to "185g"
            )
        )
    )

    private val initialOrders = listOf(
        Order(
            id = "BJ-ORD-9281",
            userId = "usr_cust_001",
            customerName = "Sarah Jenkins",
            customerEmail = "customer@buyjump.com",
            phone = "+1 (555) 987-6543",
            shippingAddress = "742 Evergreen Terrace, Springfield, 97477",
            deliveryInstructions = "Please leave at front door",
            items = listOf(
                OrderItem(
                    productId = "prod_001",
                    productTitle = "Apex Pro ANC Wireless Headphones",
                    productImage = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=700",
                    sellerId = "sel_001",
                    storeName = "Apex Tech Official",
                    categoryId = "cat_electronics",
                    price = 89.99,
                    quantity = 1,
                    commissionRate = 5.0,
                    commissionAmount = 4.50,
                    sellerPayable = 85.49
                )
            ),
            sellerIds = listOf("sel_001"),
            subtotal = 89.99,
            discount = 0.0,
            deliveryFee = 5.0,
            totalAmount = 94.99,
            status = Order.STATUS_PROCESSING,
            paymentStatus = Order.PAYMENT_PAID,
            paymentMethod = "Visa ending in •••• 4242",
            paymentId = "PAY-TXN-88419",
            createdAt = System.currentTimeMillis() - 86400000L
        )
    )

    private val initialPayouts = listOf(
        SellerPayout(
            id = "PAYOUT-001",
            sellerId = "sel_001",
            storeName = "Apex Tech Official",
            orderId = "BJ-ORD-9281",
            grossAmount = 89.99,
            commissionAmount = 4.50,
            netAmount = 85.49,
            status = SellerPayout.STATUS_PENDING,
            bankDetails = "Chase Bank •••• 7890",
            createdAt = System.currentTimeMillis() - 86400000L
        )
    )

    // State Flows
    private val _currentUser = MutableStateFlow<User?>(defaultCustomer)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(initialProducts)
    override val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(initialCategories)
    override val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _sellers = MutableStateFlow<List<Seller>>(initialSellers)
    override val sellers: StateFlow<List<Seller>> = _sellers.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(initialOrders)
    override val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    override val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _wishlistItems = MutableStateFlow<List<WishlistItem>>(emptyList())
    override val wishlistItems: StateFlow<List<WishlistItem>> = _wishlistItems.asStateFlow()

    private val _commissionSetting = MutableStateFlow(CommissionSetting())
    override val commissionSetting: StateFlow<CommissionSetting> = _commissionSetting.asStateFlow()

    private val _payouts = MutableStateFlow<List<SellerPayout>>(initialPayouts)
    override val payouts: StateFlow<List<SellerPayout>> = _payouts.asStateFlow()

    // Authentication
    override suspend fun login(email: String, pass: String): Result<User> {
        val cleanEmail = email.trim().lowercase()
        return when {
            cleanEmail == "admin@buyjump.com" || cleanEmail.contains("admin") -> {
                _currentUser.value = defaultAdmin
                Result.success(defaultAdmin)
            }
            cleanEmail == "seller@buyjump.com" -> {
                _currentUser.value = defaultSellerUser
                Result.success(defaultSellerUser)
            }
            cleanEmail == "customer@buyjump.com" -> {
                _currentUser.value = defaultCustomer
                Result.success(defaultCustomer)
            }
            cleanEmail.isNotEmpty() -> {
                // Check if matches registered seller
                val matchedSeller = _sellers.value.find { it.email.equals(cleanEmail, ignoreCase = true) }
                if (matchedSeller != null) {
                    val user = User(
                        id = matchedSeller.userId,
                        name = matchedSeller.fullName,
                        email = matchedSeller.email,
                        phone = matchedSeller.phone,
                        role = User.ROLE_SELLER,
                        status = matchedSeller.status
                    )
                    _currentUser.value = user
                    Result.success(user)
                } else {
                    // Log in or auto-create customer profile
                    val newCust = User(
                        id = "usr_" + UUID.randomUUID().toString().take(8),
                        name = cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                        email = cleanEmail,
                        phone = "+1 (555) 000-0000",
                        role = User.ROLE_CUSTOMER,
                        status = "active"
                    )
                    _currentUser.value = newCust
                    Result.success(newCust)
                }
            }
            else -> Result.failure(IllegalArgumentException("Please enter a valid email address"))
        }
    }

    override suspend fun registerCustomer(name: String, email: String, phone: String, pass: String): Result<User> {
        val cleanEmail = email.trim().lowercase()
        val user = User(
            id = "usr_" + UUID.randomUUID().toString().take(8),
            name = name.trim(),
            email = cleanEmail,
            phone = phone.trim(),
            role = User.ROLE_CUSTOMER,
            status = "active"
        )
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun registerSeller(
        fullName: String,
        email: String,
        phone: String,
        pass: String,
        storeName: String,
        description: String,
        businessAddress: String,
        pickupAddress: String,
        logoUri: String
    ): Result<Seller> {
        val cleanEmail = email.trim().lowercase()
        val userId = "usr_sel_" + UUID.randomUUID().toString().take(8)
        val sellerId = "sel_" + UUID.randomUUID().toString().take(8)

        val newSeller = Seller(
            id = sellerId,
            userId = userId,
            fullName = fullName.trim(),
            email = cleanEmail,
            phone = phone.trim(),
            storeName = storeName.trim(),
            storeDescription = description.trim(),
            businessAddress = businessAddress.trim(),
            pickupAddress = pickupAddress.trim(),
            logoUrl = logoUri.ifEmpty { "https://images.unsplash.com/photo-1472851294608-062f824d29cc?w=300" },
            status = Seller.STATUS_PENDING,
            rating = 5.0,
            totalSales = 0.0,
            totalOrders = 0
        )

        _sellers.value = _sellers.value + newSeller

        // Set current user as seller with pending status
        val sellerUser = User(
            id = userId,
            name = fullName.trim(),
            email = cleanEmail,
            phone = phone.trim(),
            role = User.ROLE_SELLER,
            status = Seller.STATUS_PENDING
        )
        _currentUser.value = sellerUser

        return Result.success(newSeller)
    }

    override fun logout() {
        // Return to clean customer default
        _currentUser.value = defaultCustomer
        _cartItems.value = emptyList()
    }

    // Categories
    override suspend fun addCategory(name: String, description: String, imageUrl: String, commissionRate: Double): Result<Category> {
        val newCategory = Category(
            id = "cat_" + UUID.randomUUID().toString().take(8),
            name = name.trim(),
            description = description.trim(),
            imageUrl = imageUrl.ifEmpty { "https://images.unsplash.com/photo-1472851294608-062f824d29cc?w=500" },
            status = Category.STATUS_ACTIVE,
            commissionRate = commissionRate
        )
        _categories.value = _categories.value + newCategory

        // Also update commission setting category rates
        val currentRates = _commissionSetting.value.categoryRates.toMutableMap()
        currentRates[newCategory.id] = commissionRate
        _commissionSetting.value = _commissionSetting.value.copy(categoryRates = currentRates)

        return Result.success(newCategory)
    }

    override suspend fun updateCategory(category: Category): Result<Category> {
        _categories.value = _categories.value.map { if (it.id == category.id) category else it }
        val currentRates = _commissionSetting.value.categoryRates.toMutableMap()
        currentRates[category.id] = category.commissionRate
        _commissionSetting.value = _commissionSetting.value.copy(categoryRates = currentRates)
        return Result.success(category)
    }

    override suspend fun deleteCategory(categoryId: String): Result<Unit> {
        _categories.value = _categories.value.filterNot { it.id == categoryId }
        return Result.success(Unit)
    }

    override suspend fun toggleCategoryStatus(categoryId: String): Result<Category> {
        var updated: Category? = null
        _categories.value = _categories.value.map {
            if (it.id == categoryId) {
                val newStatus = if (it.status == Category.STATUS_ACTIVE) Category.STATUS_INACTIVE else Category.STATUS_ACTIVE
                val copy = it.copy(status = newStatus, updatedAt = System.currentTimeMillis())
                updated = copy
                copy
            } else it
        }
        return updated?.let { Result.success(it) } ?: Result.failure(NoSuchElementException("Category not found"))
    }

    // Products
    override suspend fun addProduct(
        sellerId: String,
        storeName: String,
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
    ): Result<Product> {
        val newProduct = Product(
            id = "prod_" + UUID.randomUUID().toString().take(8),
            sellerId = sellerId,
            storeName = storeName,
            title = title.trim(),
            description = description.trim(),
            categoryId = categoryId,
            categoryName = categoryName,
            price = price,
            originalPrice = if (originalPrice > 0.0) originalPrice else price,
            discountPercent = discountPercent,
            stock = stock,
            images = images.ifEmpty { listOf("https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=700") },
            approvalStatus = Product.APPROVAL_PENDING, // Strictly pending initially!
            status = Product.STATUS_ACTIVE,
            rating = 5.0,
            reviewCount = 0,
            soldCount = 0,
            specifications = specifications
        )
        _products.value = listOf(newProduct) + _products.value
        return Result.success(newProduct)
    }

    override suspend fun updateProduct(product: Product): Result<Product> {
        _products.value = _products.value.map { if (it.id == product.id) product else it }
        return Result.success(product)
    }

    override suspend fun deleteProduct(productId: String): Result<Unit> {
        _products.value = _products.value.filterNot { it.id == productId }
        return Result.success(Unit)
    }

    override suspend fun approveProduct(productId: String): Result<Product> {
        var updated: Product? = null
        _products.value = _products.value.map {
            if (it.id == productId) {
                val copy = it.copy(approvalStatus = Product.APPROVAL_APPROVED, status = Product.STATUS_ACTIVE, rejectionReason = null)
                updated = copy
                copy
            } else it
        }
        return updated?.let { Result.success(it) } ?: Result.failure(NoSuchElementException("Product not found"))
    }

    override suspend fun rejectProduct(productId: String, reason: String): Result<Product> {
        var updated: Product? = null
        _products.value = _products.value.map {
            if (it.id == productId) {
                val copy = it.copy(approvalStatus = Product.APPROVAL_REJECTED, rejectionReason = reason)
                updated = copy
                copy
            } else it
        }
        return updated?.let { Result.success(it) } ?: Result.failure(NoSuchElementException("Product not found"))
    }

    // Sellers
    override suspend fun approveSeller(sellerId: String): Result<Seller> {
        var updated: Seller? = null
        _sellers.value = _sellers.value.map {
            if (it.id == sellerId) {
                val copy = it.copy(status = Seller.STATUS_ACTIVE, rejectionReason = null)
                updated = copy
                copy
            } else it
        }
        return updated?.let { Result.success(it) } ?: Result.failure(NoSuchElementException("Seller not found"))
    }

    override suspend fun rejectSeller(sellerId: String, reason: String): Result<Seller> {
        var updated: Seller? = null
        _sellers.value = _sellers.value.map {
            if (it.id == sellerId) {
                val copy = it.copy(status = Seller.STATUS_REJECTED, rejectionReason = reason)
                updated = copy
                copy
            } else it
        }
        return updated?.let { Result.success(it) } ?: Result.failure(NoSuchElementException("Seller not found"))
    }

    override suspend fun toggleSellerStatus(sellerId: String): Result<Seller> {
        var updated: Seller? = null
        _sellers.value = _sellers.value.map {
            if (it.id == sellerId) {
                val newStatus = if (it.status == Seller.STATUS_ACTIVE) Seller.STATUS_DISABLED else Seller.STATUS_ACTIVE
                val copy = it.copy(status = newStatus)
                updated = copy
                copy
            } else it
        }
        return updated?.let { Result.success(it) } ?: Result.failure(NoSuchElementException("Seller not found"))
    }

    // Cart
    override suspend fun addToCart(product: Product, quantity: Int): Result<Unit> {
        val currentList = _cartItems.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.productId == product.id }
        if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            val newQty = (existing.quantity + quantity).coerceAtMost(product.stock)
            currentList[existingIndex] = existing.copy(quantity = newQty)
        } else {
            currentList.add(
                CartItem(
                    productId = product.id,
                    productTitle = product.title,
                    productImage = product.primaryImageUrl,
                    sellerId = product.sellerId,
                    storeName = product.storeName,
                    price = product.price,
                    discountPercent = product.discountPercent,
                    quantity = quantity.coerceAtMost(product.stock),
                    maxStock = product.stock,
                    isSelected = true
                )
            )
        }
        _cartItems.value = currentList
        return Result.success(Unit)
    }

    override suspend fun removeFromCart(productId: String): Result<Unit> {
        _cartItems.value = _cartItems.value.filterNot { it.productId == productId }
        return Result.success(Unit)
    }

    override suspend fun updateCartQuantity(productId: String, newQuantity: Int): Result<Unit> {
        if (newQuantity <= 0) {
            return removeFromCart(productId)
        }
        _cartItems.value = _cartItems.value.map {
            if (it.productId == productId) {
                it.copy(quantity = newQuantity.coerceAtMost(it.maxStock))
            } else it
        }
        return Result.success(Unit)
    }

    override suspend fun toggleCartSelection(productId: String): Result<Unit> {
        _cartItems.value = _cartItems.value.map {
            if (it.productId == productId) it.copy(isSelected = !it.isSelected) else it
        }
        return Result.success(Unit)
    }

    override suspend fun clearCart() {
        _cartItems.value = emptyList()
    }

    // Wishlist
    override suspend fun toggleWishlist(product: Product): Result<Boolean> {
        val current = _wishlistItems.value.toMutableList()
        val exists = current.any { it.productId == product.id }
        val nowSaved: Boolean
        if (exists) {
            current.removeAll { it.productId == product.id }
            nowSaved = false
        } else {
            current.add(
                WishlistItem(
                    productId = product.id,
                    productTitle = product.title,
                    productImage = product.primaryImageUrl,
                    price = product.price,
                    rating = product.rating,
                    sellerId = product.sellerId,
                    storeName = product.storeName,
                    inStock = product.stock > 0
                )
            )
            nowSaved = true
        }
        _wishlistItems.value = current
        return Result.success(nowSaved)
    }

    override fun isInWishlist(productId: String): Boolean {
        return _wishlistItems.value.any { it.productId == productId }
    }

    // Orders & Commission
    override suspend fun createOrder(
        shippingAddress: String,
        phone: String,
        instructions: String,
        paymentMethod: String
    ): Result<Order> {
        val selectedItems = _cartItems.value.filter { it.isSelected }
        if (selectedItems.isEmpty()) {
            return Result.failure(IllegalStateException("No items selected in cart"))
        }

        // Validate stock
        val currentProducts = _products.value.associateBy { it.id }
        for (item in selectedItems) {
            val prod = currentProducts[item.productId]
            if (prod == null || prod.stock < item.quantity) {
                return Result.failure(IllegalStateException("Item '${item.productTitle}' is out of stock"))
            }
        }

        val commissionConfig = _commissionSetting.value
        val orderItems = selectedItems.map { item ->
            val prod = currentProducts[item.productId]
            val catId = prod?.categoryId.orEmpty()
            val catRate = commissionConfig.categoryRates[catId] ?: commissionConfig.globalRatePercent
            val itemTotal = item.itemTotal
            val commissionAmt = itemTotal * (catRate / 100.0)
            val payable = itemTotal - commissionAmt

            OrderItem(
                productId = item.productId,
                productTitle = item.productTitle,
                productImage = item.productImage,
                sellerId = item.sellerId,
                storeName = item.storeName,
                categoryId = catId,
                price = item.price,
                quantity = item.quantity,
                commissionRate = catRate,
                commissionAmount = commissionAmt,
                sellerPayable = payable
            )
        }

        val subtotal = orderItems.sumOf { it.total }
        val deliveryFee = 5.00
        val totalAmount = subtotal + deliveryFee
        val orderId = "BJ-ORD-" + (1000..9999).random()
        val user = _currentUser.value ?: defaultCustomer

        val newOrder = Order(
            id = orderId,
            userId = user.id,
            customerName = user.name,
            customerEmail = user.email,
            phone = phone.ifEmpty { user.phone },
            shippingAddress = shippingAddress,
            deliveryInstructions = instructions,
            items = orderItems,
            sellerIds = orderItems.map { it.sellerId }.distinct(),
            subtotal = subtotal,
            discount = 0.0,
            deliveryFee = deliveryFee,
            totalAmount = totalAmount,
            status = Order.STATUS_CONFIRMED,
            paymentStatus = Order.PAYMENT_PAID,
            paymentMethod = paymentMethod,
            paymentId = "PAY-TXN-" + UUID.randomUUID().toString().take(8).uppercase()
        )

        // Deduct product stock & increment sold count
        _products.value = _products.value.map { prod ->
            val purchased = orderItems.find { it.productId == prod.id }
            if (purchased != null) {
                prod.copy(
                    stock = (prod.stock - purchased.quantity).coerceAtLeast(0),
                    soldCount = prod.soldCount + purchased.quantity
                )
            } else prod
        }

        // Generate seller payout records grouped by seller
        val sellerGroups = orderItems.groupBy { it.sellerId }
        val newPayouts = sellerGroups.map { (sellerId, items) ->
            val gross = items.sumOf { it.total }
            val comm = items.sumOf { it.commissionAmount }
            val net = items.sumOf { it.sellerPayable }
            val sellerObj = _sellers.value.find { it.id == sellerId }

            SellerPayout(
                id = "PAYOUT-" + UUID.randomUUID().toString().take(8).uppercase(),
                sellerId = sellerId,
                storeName = sellerObj?.storeName ?: items.first().storeName,
                orderId = orderId,
                grossAmount = gross,
                commissionAmount = comm,
                netAmount = net,
                status = SellerPayout.STATUS_PENDING,
                bankDetails = "Direct Marketplace Deposit",
                createdAt = System.currentTimeMillis()
            )
        }

        // Update seller total sales
        _sellers.value = _sellers.value.map { seller ->
            val itemsForSeller = orderItems.filter { it.sellerId == seller.id }
            if (itemsForSeller.isNotEmpty()) {
                seller.copy(
                    totalSales = seller.totalSales + itemsForSeller.sumOf { it.total },
                    totalOrders = seller.totalOrders + 1
                )
            } else seller
        }

        _orders.value = listOf(newOrder) + _orders.value
        _payouts.value = newPayouts + _payouts.value

        // Remove only checked out items from cart
        _cartItems.value = _cartItems.value.filterNot { it.isSelected }

        return Result.success(newOrder)
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Order> {
        var updated: Order? = null
        _orders.value = _orders.value.map {
            if (it.id == orderId) {
                val copy = it.copy(status = newStatus, updatedAt = System.currentTimeMillis())
                updated = copy
                copy
            } else it
        }
        return updated?.let { Result.success(it) } ?: Result.failure(NoSuchElementException("Order not found"))
    }

    override suspend fun cancelOrder(orderId: String): Result<Order> {
        var updated: Order? = null
        _orders.value = _orders.value.map {
            if (it.id == orderId) {
                if (!it.isCancellable) {
                    throw IllegalStateException("Order cannot be cancelled in status: ${it.status}")
                }
                val copy = it.copy(status = Order.STATUS_CANCELLED, updatedAt = System.currentTimeMillis())
                updated = copy
                copy
            } else it
        }
        return updated?.let { Result.success(it) } ?: Result.failure(NoSuchElementException("Order not found"))
    }

    // Commission
    override suspend fun updateCommissionSettings(globalRate: Double, categoryRates: Map<String, Double>): Result<CommissionSetting> {
        val updated = CommissionSetting(
            globalRatePercent = globalRate,
            categoryRates = categoryRates,
            updatedAt = System.currentTimeMillis()
        )
        _commissionSetting.value = updated
        return Result.success(updated)
    }

    // Payouts
    override suspend fun requestPayout(sellerId: String, amount: Double, bankDetails: String): Result<SellerPayout> {
        val seller = _sellers.value.find { it.id == sellerId }
        val payout = SellerPayout(
            id = "PAYOUT-" + UUID.randomUUID().toString().take(8).uppercase(),
            sellerId = sellerId,
            storeName = seller?.storeName.orEmpty(),
            orderId = "DIRECT-WITHDRAWAL",
            grossAmount = amount,
            commissionAmount = 0.0,
            netAmount = amount,
            status = SellerPayout.STATUS_PENDING,
            bankDetails = bankDetails,
            createdAt = System.currentTimeMillis()
        )
        _payouts.value = listOf(payout) + _payouts.value
        return Result.success(payout)
    }

    override suspend fun updatePayoutStatus(payoutId: String, status: String, paymentRef: String): Result<SellerPayout> {
        var updated: SellerPayout? = null
        _payouts.value = _payouts.value.map {
            if (it.id == payoutId) {
                val copy = it.copy(
                    status = status,
                    paymentRef = paymentRef.ifEmpty { it.paymentRef },
                    paidAt = if (status == SellerPayout.STATUS_PAID) System.currentTimeMillis() else it.paidAt
                )
                updated = copy
                copy
            } else it
        }
        return updated?.let { Result.success(it) } ?: Result.failure(NoSuchElementException("Payout not found"))
    }

    // Real Local Device / Disk Image Upload
    override suspend fun uploadLocalImage(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(IllegalArgumentException("Unable to open image file"))

            // Create dedicated local storage directory
            val imagesDir = File(context.filesDir, "buyjump_uploads")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            val fileName = "img_" + UUID.randomUUID().toString() + ".jpg"
            val targetFile = File(imagesDir, fileName)

            FileOutputStream(targetFile).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()

            // Return accessible URI string for Coil / Image display
            Result.success(targetFile.toURI().toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
