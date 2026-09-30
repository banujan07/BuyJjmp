package com.example.data.firebase

import android.util.Log
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface FirebaseFirestoreService {
    // Users
    suspend fun getUser(uid: String): Result<User>
    suspend fun getUsers(): Result<List<User>>
    suspend fun saveUser(user: User): Result<Unit>

    // Sellers
    suspend fun getSellers(): Result<List<Seller>>
    suspend fun saveSeller(seller: Seller): Result<Unit>
    suspend fun updateSellerStatus(sellerId: String, status: String, reason: String? = null): Result<Unit>

    // Categories
    suspend fun getCategories(): Result<List<Category>>
    suspend fun saveCategory(category: Category): Result<Unit>
    suspend fun deleteCategory(categoryId: String): Result<Unit>

    // Products
    suspend fun getProducts(): Result<List<Product>>
    suspend fun saveProduct(product: Product): Result<Unit>
    suspend fun updateProductApproval(productId: String, status: String, reason: String? = null): Result<Unit>
    suspend fun deleteProduct(productId: String): Result<Unit>

    // Orders
    suspend fun getOrders(): Result<List<Order>>
    suspend fun saveOrder(order: Order): Result<Unit>
    suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit>

    // Commission Settings
    suspend fun getCommissionSettings(): Result<CommissionSetting>
    suspend fun saveCommissionSettings(settings: CommissionSetting): Result<Unit>

    // Payouts
    suspend fun getPayouts(): Result<List<SellerPayout>>
    suspend fun savePayout(payout: SellerPayout): Result<Unit>
    suspend fun updatePayoutStatus(payoutId: String, status: String, paymentRef: String? = null): Result<Unit>
}

class FirebaseFirestoreServiceImpl : FirebaseFirestoreService {

    private val TAG = "BuyJumpFirestore"

    // In-memory cache synced with Firestore
    private val localUsers = mutableMapOf<String, User>()
    private val localSellers = mutableMapOf<String, Seller>()
    private val localCategories = mutableMapOf<String, Category>()
    private val localProducts = mutableMapOf<String, Product>()
    private val localOrders = mutableMapOf<String, Order>()
    private val localPayouts = mutableMapOf<String, SellerPayout>()
    private var localCommission = CommissionSetting()

    // 1. Users
    override suspend fun getUser(uid: String): Result<User> = withContext(Dispatchers.IO) {
        val firestore = FirebaseManager.firestore
        if (firestore != null) {
            try {
                val doc = firestore.collection("users").document(uid).get().awaitTask()
                if (doc.exists()) {
                    val user = User(
                        id = doc.getString("id") ?: uid,
                        name = doc.getString("name").orEmpty(),
                        email = doc.getString("email").orEmpty(),
                        phone = doc.getString("phone").orEmpty(),
                        role = doc.getString("role") ?: User.ROLE_CUSTOMER,
                        status = doc.getString("status") ?: "active"
                    )
                    localUsers[uid] = user
                    return@withContext Result.success(user)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Firestore getUser error: ${e.message}")
            }
        }
        val cached = localUsers[uid]
        if (cached != null) Result.success(cached)
        else Result.failure(NoSuchElementException("User $uid not found"))
    }

    override suspend fun getUsers(): Result<List<User>> = withContext(Dispatchers.IO) {
        val firestore = FirebaseManager.firestore
        if (firestore != null) {
            try {
                val snapshot = firestore.collection("users").get().awaitTask()
                val list = snapshot.documents.map { doc ->
                    User(
                        id = doc.getString("id") ?: doc.id,
                        name = doc.getString("name").orEmpty(),
                        email = doc.getString("email").orEmpty(),
                        phone = doc.getString("phone").orEmpty(),
                        role = doc.getString("role") ?: User.ROLE_CUSTOMER,
                        status = doc.getString("status") ?: "active"
                    )
                }
                list.forEach { localUsers[it.id] = it }
                if (list.isNotEmpty()) return@withContext Result.success(list)
            } catch (e: Exception) {
                Log.w(TAG, "Firestore getUsers error: ${e.message}")
            }
        }
        Result.success(localUsers.values.toList())
    }

    override suspend fun saveUser(user: User): Result<Unit> = withContext(Dispatchers.IO) {
        localUsers[user.id] = user
        val firestore = FirebaseManager.firestore
        if (firestore != null) {
            try {
                val data = hashMapOf(
                    "id" to user.id,
                    "name" to user.name,
                    "email" to user.email,
                    "phone" to user.phone,
                    "role" to user.role,
                    "status" to user.status,
                    "createdAt" to user.createdAt
                )
                firestore.collection("users").document(user.id).set(data).awaitTask()
                Log.d(TAG, "User saved to Firestore users/${user.id}")
            } catch (e: Exception) {
                Log.w(TAG, "Firestore saveUser error: ${e.message}")
            }
        }
        Result.success(Unit)
    }

    // 2. Sellers
    override suspend fun getSellers(): Result<List<Seller>> = withContext(Dispatchers.IO) {
        val firestore = FirebaseManager.firestore
        if (firestore != null) {
            try {
                val snapshot = firestore.collection("sellers").get().awaitTask()
                val list = snapshot.documents.map { doc ->
                    Seller(
                        id = doc.getString("id") ?: doc.id,
                        userId = doc.getString("userId").orEmpty(),
                        fullName = doc.getString("fullName").orEmpty(),
                        email = doc.getString("email").orEmpty(),
                        phone = doc.getString("phone").orEmpty(),
                        storeName = doc.getString("storeName").orEmpty(),
                        storeDescription = doc.getString("storeDescription").orEmpty(),
                        businessAddress = doc.getString("businessAddress").orEmpty(),
                        pickupAddress = doc.getString("pickupAddress").orEmpty(),
                        logoUrl = doc.getString("logoUrl").orEmpty(),
                        status = doc.getString("status") ?: Seller.STATUS_PENDING,
                        rejectionReason = doc.getString("rejectionReason"),
                        rating = doc.getDouble("rating") ?: 4.8,
                        totalSales = doc.getDouble("totalSales") ?: 0.0,
                        totalOrders = doc.getLong("totalOrders")?.toInt() ?: 0
                    )
                }
                list.forEach { localSellers[it.id] = it }
                if (list.isNotEmpty()) return@withContext Result.success(list)
            } catch (e: Exception) {
                Log.w(TAG, "Firestore getSellers error: ${e.message}")
            }
        }
        Result.success(localSellers.values.toList())
    }

    override suspend fun saveSeller(seller: Seller): Result<Unit> = withContext(Dispatchers.IO) {
        localSellers[seller.id] = seller
        val firestore = FirebaseManager.firestore
        if (firestore != null) {
            try {
                val data = hashMapOf(
                    "id" to seller.id,
                    "userId" to seller.userId,
                    "fullName" to seller.fullName,
                    "email" to seller.email,
                    "phone" to seller.phone,
                    "storeName" to seller.storeName,
                    "storeDescription" to seller.storeDescription,
                    "businessAddress" to seller.businessAddress,
                    "pickupAddress" to seller.pickupAddress,
                    "logoUrl" to seller.logoUrl,
                    "status" to seller.status,
                    "rejectionReason" to seller.rejectionReason,
                    "rating" to seller.rating,
                    "totalSales" to seller.totalSales,
                    "totalOrders" to seller.totalOrders,
                    "createdAt" to seller.createdAt
                )
                firestore.collection("sellers").document(seller.id).set(data).awaitTask()
                Log.d(TAG, "Seller saved to Firestore sellers/${seller.id}")
            } catch (e: Exception) {
                Log.w(TAG, "Firestore saveSeller error: ${e.message}")
            }
        }
        Result.success(Unit)
    }

    override suspend fun updateSellerStatus(sellerId: String, status: String, reason: String?): Result<Unit> = withContext(Dispatchers.IO) {
        val existing = localSellers[sellerId]
        if (existing != null) {
            localSellers[sellerId] = existing.copy(status = status, rejectionReason = reason)
        }
        val firestore = FirebaseManager.firestore
        if (firestore != null) {
            try {
                val updates = mutableMapOf<String, Any>("status" to status)
                if (reason != null) updates["rejectionReason"] = reason
                firestore.collection("sellers").document(sellerId).update(updates).awaitTask()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore updateSellerStatus error: ${e.message}")
            }
        }
        Result.success(Unit)
    }

    // 3. Categories
    override suspend fun getCategories(): Result<List<Category>> = withContext(Dispatchers.IO) {
        val firestore = FirebaseManager.firestore
        if (firestore != null) {
            try {
                val snapshot = firestore.collection("categories").get().awaitTask()
                val list = snapshot.documents.map { doc ->
                    Category(
                        id = doc.getString("id") ?: doc.id,
                        name = doc.getString("name").orEmpty(),
                        description = doc.getString("description").orEmpty(),
                        imageUrl = doc.getString("imageUrl").orEmpty(),
                        iconName = doc.getString("iconName") ?: "category",
                        status = doc.getString("status") ?: Category.STATUS_ACTIVE,
                        commissionRate = doc.getDouble("commissionRate") ?: 10.0
                    )
                }
                list.forEach { localCategories[it.id] = it }
                if (list.isNotEmpty()) return@withContext Result.success(list)
            } catch (e: Exception) {
                Log.w(TAG, "Firestore getCategories error: ${e.message}")
            }
        }
        Result.success(localCategories.values.toList())
    }

    override suspend fun saveCategory(category: Category): Result<Unit> = withContext(Dispatchers.IO) {
        localCategories[category.id] = category
        val firestore = FirebaseManager.firestore
        if (firestore != null) {
            try {
                val data = hashMapOf(
                    "id" to category.id,
                    "name" to category.name,
                    "description" to category.description,
                    "imageUrl" to category.imageUrl,
                    "iconName" to category.iconName,
                    "status" to category.status,
                    "commissionRate" to category.commissionRate,
                    "createdAt" to category.createdAt,
                    "updatedAt" to category.updatedAt
                )
                firestore.collection("categories").document(category.id).set(data).awaitTask()
                Log.d(TAG, "Category saved to Firestore categories/${category.id}")
            } catch (e: Exception) {
                Log.w(TAG, "Firestore saveCategory error: ${e.message}")
            }
        }
        Result.success(Unit)
    }

    override suspend fun deleteCategory(categoryId: String): Result<Unit> = withContext(Dispatchers.IO) {
        localCategories.remove(categoryId)
        val firestore = FirebaseManager.firestore
        if (firestore != null) {
            try {
                firestore.collection("categories").document(categoryId).delete().awaitTask()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore deleteCategory error: ${e.message}")
            }
        }
        Result.success(Unit)
    }

    // 4. Products
    override suspend fun getProducts(): Result<List<Product>> = withContext(Dispatchers.IO) {
        val firestore = FirebaseManager.firestore
        if (firestore != null) {
            try {
                val snapshot = firestore.collection("products").get().awaitTask()
                val list = snapshot.documents.map { doc ->
                    @Suppress("UNCHECKED_CAST")
                    val imagesList = (doc.get("images") as? List<String>) ?: emptyList()
                    @Suppress("UNCHECKED_CAST")
                    val specsMap = (doc.get("specifications") as? Map<String, String>) ?: emptyMap()

                    Product(
                        id = doc.getString("id") ?: doc.id,
                        sellerId = doc.getString("sellerId").orEmpty(),
                        storeName = doc.getString("storeName").orEmpty(),
                        title = doc.getString("title").orEmpty(),
                        description = doc.getString("description").orEmpty(),
                        categoryId = doc.getString("categoryId").orEmpty(),
                        categoryName = doc.getString("categoryName").orEmpty(),
                        price = doc.getDouble("price") ?: 0.0,
                        originalPrice = doc.getDouble("originalPrice") ?: 0.0,
                        discountPercent = doc.getLong("discountPercent")?.toInt() ?: 0,
                        stock = doc.getLong("stock")?.toInt() ?: 0,
                        images = imagesList,
                        approvalStatus = doc.getString("approvalStatus") ?: Product.APPROVAL_PENDING,
                        rejectionReason = doc.getString("rejectionReason"),
                        status = doc.getString("status") ?: Product.STATUS_ACTIVE,
                        rating = doc.getDouble("rating") ?: 4.5,
                        reviewCount = doc.getLong("reviewCount")?.toInt() ?: 0,
                        soldCount = doc.getLong("soldCount")?.toInt() ?: 0,
                        specifications = specsMap,
                        isFeatured = doc.getBoolean("isFeatured") ?: false,
                        isFlashDeal = doc.getBoolean("isFlashDeal") ?: false
                    )
                }
                list.forEach { localProducts[it.id] = it }
                if (list.isNotEmpty()) return@withContext Result.success(list)
            } catch (e: Exception) {
                Log.w(TAG, "Firestore getProducts error: ${e.message}")
            }
        }
        Result.success(localProducts.values.toList())
    }

    override suspend fun saveProduct(product: Product): Result<Unit> = withContext(Dispatchers.IO) {
        localProducts[product.id] = product
        val firestore = FirebaseManager.firestore
        if (firestore != null) {
            try {
                val data = hashMapOf(
                    "id" to product.id,
                    "sellerId" to product.sellerId,
                    "storeName" to product.storeName,
                    "title" to product.title,
                    "description" to product.description,
                    "categoryId" to product.categoryId,
                    "categoryName" to product.categoryName,
                    "price" to product.price,
                    "originalPrice" to product.originalPrice,
                    "discountPercent" to product.discountPercent,
                    "stock" to product.stock,
                    "images" to product.images,
                    "approvalStatus" to product.approvalStatus,
                    "rejectionReason" to product.rejectionReason,
                    "status" to product.status,
                    "rating" to product.rating,
                    "reviewCount" to product.reviewCount,
                    "soldCount" to product.soldCount,
                    "specifications" to product.specifications,
                    "isFeatured" to product.isFeatured,
                    "isFlashDeal" to product.isFlashDeal,
                    "createdAt" to product.createdAt,
                    "updatedAt" to product.updatedAt
                )
                firestore.collection("products").document(product.id).set(data).awaitTask()
                Log.d(TAG, "Product saved to Firestore products/${product.id}")
            } catch (e: Exception) {
                Log.w(TAG, "Firestore saveProduct error: ${e.message}")
            }
        }
        Result.success(Unit)
    }

    override suspend fun updateProductApproval(productId: String, status: String, reason: String?): Result<Unit> = withContext(Dispatchers.IO) {
        val existing = localProducts[productId]
        if (existing != null) {
            localProducts[productId] = existing.copy(approvalStatus = status, rejectionReason = reason)
        }
        val firestore = FirebaseManager.firestore
        if (firestore != null) {
            try {
                val updates = mutableMapOf<String, Any>("approvalStatus" to status)
                if (reason != null) updates["rejectionReason"] = reason
                firestore.collection("products").document(productId).update(updates).awaitTask()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore updateProductApproval error: ${e.message}")
            }
        }
        Result.success(Unit)
    }

    override suspend fun deleteProduct(productId: String): Result<Unit> = withContext(Dispatchers.IO) {
        localProducts.remove(productId)
        val firestore = FirebaseManager.firestore
        if (firestore != null) {
            try {
                firestore.collection("products").document(productId).delete().awaitTask()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore deleteProduct error: ${e.message}")
            }
        }
        Result.success(Unit)
    }

    // 5. Orders
    override suspend fun getOrders(): Result<List<Order>> = withContext(Dispatchers.IO) {
        Result.success(localOrders.values.toList())
    }

    override suspend fun saveOrder(order: Order): Result<Unit> = withContext(Dispatchers.IO) {
        localOrders[order.id] = order
        val firestore = FirebaseManager.firestore
        if (firestore != null) {
            try {
                val data = hashMapOf(
                    "id" to order.id,
                    "userId" to order.userId,
                    "customerName" to order.customerName,
                    "customerEmail" to order.customerEmail,
                    "phone" to order.phone,
                    "shippingAddress" to order.shippingAddress,
                    "deliveryInstructions" to order.deliveryInstructions,
                    "sellerIds" to order.sellerIds,
                    "subtotal" to order.subtotal,
                    "discount" to order.discount,
                    "deliveryFee" to order.deliveryFee,
                    "totalAmount" to order.totalAmount,
                    "status" to order.status,
                    "paymentStatus" to order.paymentStatus,
                    "paymentMethod" to order.paymentMethod,
                    "paymentId" to order.paymentId,
                    "createdAt" to order.createdAt
                )
                firestore.collection("orders").document(order.id).set(data).awaitTask()
                Log.d(TAG, "Order saved to Firestore orders/${order.id}")
            } catch (e: Exception) {
                Log.w(TAG, "Firestore saveOrder error: ${e.message}")
            }
        }
        Result.success(Unit)
    }

    override suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit> = withContext(Dispatchers.IO) {
        val existing = localOrders[orderId]
        if (existing != null) {
            localOrders[orderId] = existing.copy(status = status)
        }
        val firestore = FirebaseManager.firestore
        if (firestore != null) {
            try {
                firestore.collection("orders").document(orderId).update("status", status).awaitTask()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore updateOrderStatus error: ${e.message}")
            }
        }
        Result.success(Unit)
    }

    // 6. Commission Settings
    override suspend fun getCommissionSettings(): Result<CommissionSetting> = withContext(Dispatchers.IO) {
        val firestore = FirebaseManager.firestore
        if (firestore != null) {
            try {
                val doc = firestore.collection("commissionSettings").document("default_settings").get().awaitTask()
                if (doc.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val catRates = (doc.get("categoryRates") as? Map<String, Double>) ?: localCommission.categoryRates
                    val setting = CommissionSetting(
                        id = doc.id,
                        globalRatePercent = doc.getDouble("globalRatePercent") ?: 10.0,
                        categoryRates = catRates
                    )
                    localCommission = setting
                    return@withContext Result.success(setting)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Firestore getCommissionSettings error: ${e.message}")
            }
        }
        Result.success(localCommission)
    }

    override suspend fun saveCommissionSettings(settings: CommissionSetting): Result<Unit> = withContext(Dispatchers.IO) {
        localCommission = settings
        val firestore = FirebaseManager.firestore
        if (firestore != null) {
            try {
                val data = hashMapOf(
                    "id" to settings.id,
                    "globalRatePercent" to settings.globalRatePercent,
                    "categoryRates" to settings.categoryRates,
                    "updatedAt" to settings.updatedAt
                )
                firestore.collection("commissionSettings").document("default_settings").set(data).awaitTask()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore saveCommissionSettings error: ${e.message}")
            }
        }
        Result.success(Unit)
    }

    // 7. Payouts
    override suspend fun getPayouts(): Result<List<SellerPayout>> = withContext(Dispatchers.IO) {
        Result.success(localPayouts.values.toList())
    }

    override suspend fun savePayout(payout: SellerPayout): Result<Unit> = withContext(Dispatchers.IO) {
        localPayouts[payout.id] = payout
        val firestore = FirebaseManager.firestore
        if (firestore != null) {
            try {
                val data = hashMapOf(
                    "id" to payout.id,
                    "sellerId" to payout.sellerId,
                    "storeName" to payout.storeName,
                    "orderId" to payout.orderId,
                    "grossAmount" to payout.grossAmount,
                    "commissionAmount" to payout.commissionAmount,
                    "netAmount" to payout.netAmount,
                    "status" to payout.status,
                    "paymentRef" to payout.paymentRef,
                    "bankDetails" to payout.bankDetails,
                    "createdAt" to payout.createdAt,
                    "paidAt" to payout.paidAt
                )
                firestore.collection("sellerPayouts").document(payout.id).set(data).awaitTask()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore savePayout error: ${e.message}")
            }
        }
        Result.success(Unit)
    }

    override suspend fun updatePayoutStatus(payoutId: String, status: String, paymentRef: String?): Result<Unit> = withContext(Dispatchers.IO) {
        val existing = localPayouts[payoutId]
        if (existing != null) {
            localPayouts[payoutId] = existing.copy(
                status = status,
                paymentRef = paymentRef ?: existing.paymentRef,
                paidAt = if (status == SellerPayout.STATUS_PAID) System.currentTimeMillis() else existing.paidAt
            )
        }
        val firestore = FirebaseManager.firestore
        if (firestore != null) {
            try {
                val updates = mutableMapOf<String, Any>("status" to status)
                if (paymentRef != null) updates["paymentRef"] = paymentRef
                if (status == SellerPayout.STATUS_PAID) updates["paidAt"] = System.currentTimeMillis()
                firestore.collection("sellerPayouts").document(payoutId).update(updates).awaitTask()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore updatePayoutStatus error: ${e.message}")
            }
        }
        Result.success(Unit)
    }
}
