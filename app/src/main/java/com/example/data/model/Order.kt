package com.example.data.model

data class Order(
    val id: String = "",
    val userId: String = "",
    val customerName: String = "",
    val customerEmail: String = "",
    val phone: String = "",
    val shippingAddress: String = "",
    val deliveryInstructions: String = "",
    val items: List<OrderItem> = emptyList(),
    val sellerIds: List<String> = emptyList(),
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val totalAmount: Double = 0.0,
    val status: String = STATUS_PENDING, // pending, confirmed, processing, shipped, delivered, cancelled
    val paymentStatus: String = PAYMENT_PAID, // pending, paid, failed
    val paymentMethod: String = "Credit / Debit Card", // card, cod, etc.
    val paymentId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_CONFIRMED = "confirmed"
        const val STATUS_PROCESSING = "processing"
        const val STATUS_SHIPPED = "shipped"
        const val STATUS_DELIVERED = "delivered"
        const val STATUS_CANCELLED = "cancelled"

        const val PAYMENT_PENDING = "pending"
        const val PAYMENT_PAID = "paid"
        const val PAYMENT_FAILED = "failed"
    }

    val isCancellable: Boolean
        get() = status == STATUS_PENDING || status == STATUS_CONFIRMED

    val progressStepIndex: Int
        get() = when (status) {
            STATUS_PENDING -> 0
            STATUS_CONFIRMED -> 1
            STATUS_PROCESSING -> 2
            STATUS_SHIPPED -> 3
            STATUS_DELIVERED -> 4
            STATUS_CANCELLED -> -1
            else -> 0
        }
}

data class OrderItem(
    val productId: String = "",
    val productTitle: String = "",
    val productImage: String = "",
    val sellerId: String = "",
    val storeName: String = "",
    val categoryId: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val commissionRate: Double = 10.0,
    val commissionAmount: Double = 0.0,
    val sellerPayable: Double = 0.0
) {
    val total: Double get() = price * quantity
}
