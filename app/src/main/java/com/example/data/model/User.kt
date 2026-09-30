package com.example.data.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = ROLE_CUSTOMER, // customer, seller, admin
    val status: String = "active",    // active, disabled
    val addresses: List<ShippingAddress> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val ROLE_CUSTOMER = "customer"
        const val ROLE_SELLER = "seller"
        const val ROLE_ADMIN = "admin"
    }

    val isAdmin: Boolean get() = role == ROLE_ADMIN
    val isSeller: Boolean get() = role == ROLE_SELLER
    val isCustomer: Boolean get() = role == ROLE_CUSTOMER
}

data class ShippingAddress(
    val id: String = "",
    val label: String = "Home",
    val recipientName: String = "",
    val phone: String = "",
    val street: String = "",
    val city: String = "",
    val postalCode: String = "",
    val isDefault: Boolean = false
)
