package com.example.data.model

data class Category(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val iconName: String = "category",
    val status: String = STATUS_ACTIVE, // active, inactive
    val commissionRate: Double = 10.0, // Specific category commission percentage e.g. 5.0, 10.0, 12.0
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_ACTIVE = "active"
        const val STATUS_INACTIVE = "inactive"
    }

    val isActive: Boolean get() = status == STATUS_ACTIVE
}
