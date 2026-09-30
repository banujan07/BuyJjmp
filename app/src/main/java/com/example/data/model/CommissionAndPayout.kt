package com.example.data.model

data class CommissionSetting(
    val id: String = "default_settings",
    val globalRatePercent: Double = 10.0,
    val categoryRates: Map<String, Double> = mapOf(
        "cat_electronics" to 5.0,
        "cat_fashion" to 10.0,
        "cat_shoes" to 12.0,
        "cat_beauty" to 10.0,
        "cat_home" to 8.0,
        "cat_groceries" to 6.0
    ),
    val updatedAt: Long = System.currentTimeMillis()
)

data class SellerPayout(
    val id: String = "",
    val sellerId: String = "",
    val storeName: String = "",
    val orderId: String = "",
    val grossAmount: Double = 0.0,
    val commissionAmount: Double = 0.0,
    val netAmount: Double = 0.0,
    val status: String = STATUS_PENDING, // pending, processing, paid, failed, cancelled
    val paymentRef: String? = null,
    val bankDetails: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val paidAt: Long? = null
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_PROCESSING = "processing"
        const val STATUS_PAID = "paid"
        const val STATUS_FAILED = "failed"
        const val STATUS_CANCELLED = "cancelled"
    }
}
