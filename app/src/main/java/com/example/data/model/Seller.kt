package com.example.data.model

data class Seller(
    val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val storeName: String = "",
    val storeDescription: String = "",
    val businessAddress: String = "",
    val pickupAddress: String = "",
    val logoUrl: String = "",
    val status: String = STATUS_PENDING, // pending, active, rejected, disabled
    val rejectionReason: String? = null,
    val rating: Double = 4.8,
    val totalSales: Double = 0.0,
    val totalOrders: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_ACTIVE = "active"
        const val STATUS_REJECTED = "rejected"
        const val STATUS_DISABLED = "disabled"
    }

    val isApproved: Boolean get() = status == STATUS_ACTIVE
    val isPending: Boolean get() = status == STATUS_PENDING
}
