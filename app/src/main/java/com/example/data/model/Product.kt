package com.example.data.model

data class Product(
    val id: String = "",
    val sellerId: String = "",
    val storeName: String = "",
    val title: String = "",
    val description: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val price: Double = 0.0,
    val originalPrice: Double = 0.0,
    val discountPercent: Int = 0,
    val stock: Int = 0,
    val images: List<String> = emptyList(),
    val approvalStatus: String = APPROVAL_PENDING, // pending, approved, rejected
    val rejectionReason: String? = null,
    val status: String = STATUS_ACTIVE,           // active, inactive
    val rating: Double = 4.5,
    val reviewCount: Int = 0,
    val soldCount: Int = 0,
    val specifications: Map<String, String> = emptyMap(),
    val isFeatured: Boolean = false,
    val isFlashDeal: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val APPROVAL_PENDING = "pending"
        const val APPROVAL_APPROVED = "approved"
        const val APPROVAL_REJECTED = "rejected"

        const val STATUS_ACTIVE = "active"
        const val STATUS_INACTIVE = "inactive"
    }

    val primaryImageUrl: String
        get() = images.firstOrNull().orEmpty()

    val isCustomerVisible: Boolean
        get() = approvalStatus == APPROVAL_APPROVED && status == STATUS_ACTIVE && stock > 0

    val isInStock: Boolean
        get() = stock > 0
}
