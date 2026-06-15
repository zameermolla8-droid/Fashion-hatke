package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClothingDao {
    // Clothing items
    @Query("SELECT * FROM clothing_items")
    fun getAllClothingItems(): Flow<List<ClothingItem>>

    @Query("SELECT * FROM clothing_items WHERE id = :id LIMIT 1")
    suspend fun getClothingItemById(id: Int): ClothingItem?

    @Query("SELECT COUNT(*) FROM clothing_items")
    suspend fun getClothingItemsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClothingItem(item: ClothingItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClothingItems(items: List<ClothingItem>)

    // Cart items
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItem)

    @Query("SELECT * FROM cart_items WHERE itemId = :itemId AND selectedSize = :size LIMIT 1")
    suspend fun findCartItem(itemId: Int, size: String): CartItem?

    @Update
    suspend fun updateCartItem(item: CartItem)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItem(id: Int)

    @Query("UPDATE cart_items SET selectedQuantity = :quantity WHERE id = :id")
    suspend fun updateCartQuantity(id: Int, quantity: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    // Order history
    @Query("SELECT * FROM order_history ORDER BY timestamp DESC")
    fun getOrderHistory(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)
}
