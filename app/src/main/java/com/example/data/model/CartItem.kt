package com.example.data.model

data class CartItem(
    val productId: String = "",
    val productTitle: String = "",
    val productImage: String = "",
    val sellerId: String = "",
    val storeName: String = "",
    val price: Double = 0.0,
    val discountPercent: Int = 0,
    val quantity: Int = 1,
    val maxStock: Int = 1,
    val isSelected: Boolean = true
) {
    val itemTotal: Double get() = price * quantity
}

data class WishlistItem(
    val productId: String = "",
    val productTitle: String = "",
    val productImage: String = "",
    val price: Double = 0.0,
    val rating: Double = 4.5,
    val sellerId: String = "",
    val storeName: String = "",
    val inStock: Boolean = true,
    val addedAt: Long = System.currentTimeMillis()
)
