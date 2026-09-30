package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.model.*
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

interface BuyJumpRepository {
    val currentUser: StateFlow<User?>
    val products: StateFlow<List<Product>>
    val categories: StateFlow<List<Category>>
    val sellers: StateFlow<List<Seller>>
    val orders: StateFlow<List<Order>>
    val cartItems: StateFlow<List<CartItem>>
    val wishlistItems: StateFlow<List<WishlistItem>>
    val commissionSetting: StateFlow<CommissionSetting>
    val payouts: StateFlow<List<SellerPayout>>

    // Auth
    suspend fun login(email: String, pass: String): Result<User>
    suspend fun registerCustomer(name: String, email: String, phone: String, pass: String): Result<User>
    suspend fun registerSeller(
        fullName: String,
        email: String,
        phone: String,
        pass: String,
        storeName: String,
        description: String,
        businessAddress: String,
        pickupAddress: String,
        logoUri: String
    ): Result<Seller>
    fun logout()

    // Categories
    suspend fun addCategory(name: String, description: String, imageUrl: String, commissionRate: Double): Result<Category>
    suspend fun updateCategory(category: Category): Result<Category>
    suspend fun deleteCategory(categoryId: String): Result<Unit>
    suspend fun toggleCategoryStatus(categoryId: String): Result<Category>

    // Products
    suspend fun addProduct(
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
    ): Result<Product>
    suspend fun updateProduct(product: Product): Result<Product>
    suspend fun deleteProduct(productId: String): Result<Unit>
    suspend fun approveProduct(productId: String): Result<Product>
    suspend fun rejectProduct(productId: String, reason: String): Result<Product>

    // Sellers
    suspend fun approveSeller(sellerId: String): Result<Seller>
    suspend fun rejectSeller(sellerId: String, reason: String): Result<Seller>
    suspend fun toggleSellerStatus(sellerId: String): Result<Seller>

    // Cart & Wishlist
    suspend fun addToCart(product: Product, quantity: Int = 1): Result<Unit>
    suspend fun removeFromCart(productId: String): Result<Unit>
    suspend fun updateCartQuantity(productId: String, newQuantity: Int): Result<Unit>
    suspend fun toggleCartSelection(productId: String): Result<Unit>
    suspend fun clearCart()
    suspend fun toggleWishlist(product: Product): Result<Boolean>
    fun isInWishlist(productId: String): Boolean

    // Orders
    suspend fun createOrder(
        shippingAddress: String,
        phone: String,
        instructions: String,
        paymentMethod: String
    ): Result<Order>
    suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Order>
    suspend fun cancelOrder(orderId: String): Result<Order>

    // Commission
    suspend fun updateCommissionSettings(globalRate: Double, categoryRates: Map<String, Double>): Result<CommissionSetting>

    // Payouts
    suspend fun requestPayout(sellerId: String, amount: Double, bankDetails: String): Result<SellerPayout>
    suspend fun updatePayoutStatus(payoutId: String, status: String, paymentRef: String): Result<SellerPayout>

    // Image Upload from Local Device / Disk
    suspend fun uploadLocalImage(context: Context, uri: Uri): Result<String>
}
