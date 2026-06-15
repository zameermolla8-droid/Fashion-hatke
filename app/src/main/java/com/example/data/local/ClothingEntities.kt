package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clothing_items")
data class ClothingItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val category: String,
    val price: Double,
    val rating: Double,
    val imageUrl: String,
    val sizeOptions: String, // Comma-separated: "S,M,L,XL"
    val inStock: Boolean = true,
    val isFeatured: Boolean = false
) {
    val sizesList: List<String>
        get() = sizeOptions.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemId: Int,
    val name: String,
    val category: String,
    val price: Double,
    val imageUrl: String,
    val selectedSize: String,
    val selectedQuantity: Int
)

@Entity(tableName = "order_history")
data class OrderEntity(
    @PrimaryKey val orderId: String, // String UUID or generated receipt ID
    val customerName: String,
    val customerEmail: String,
    val customerAddress: String,
    val customerPhone: String,
    val itemsSummary: String, // Detailed formatted text summary of items
    val totalPrice: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val isFulfilled: Boolean = false
)
