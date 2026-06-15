package com.example.data.repository

import com.example.data.local.CartItem
import com.example.data.local.ClothingDao
import com.example.data.local.ClothingItem
import com.example.data.local.OrderEntity
import kotlinx.coroutines.flow.Flow

class ClothingRepository(private val clothingDao: ClothingDao) {

    val allClothingItems: Flow<List<ClothingItem>> = clothingDao.getAllClothingItems()
    val cartItems: Flow<List<CartItem>> = clothingDao.getCartItems()
    val ordersList: Flow<List<OrderEntity>> = clothingDao.getOrderHistory()

    suspend fun getClothingItemById(id: Int): ClothingItem? {
        return clothingDao.getClothingItemById(id)
    }

    suspend fun seedDatabaseIfEmpty() {
        if (clothingDao.getClothingItemsCount() == 0) {
            val dummyItems = listOf(
                ClothingItem(
                    name = "Signature Leather Jacket",
                    description = "Timeless lambskin leather jacket featuring heavy-duty silver hardware, premium asymmetrical zipper closure, and quilted lining. Handcrafted with attention to stitch precision.",
                    category = "Jackets & Coats",
                    price = 249.00,
                    rating = 4.9,
                    imageUrl = "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500&auto=format&fit=crop&q=80",
                    sizeOptions = "S,M,L,XL",
                    inStock = true,
                    isFeatured = true
                ),
                ClothingItem(
                    name = "Tailored Wool Blend Blazer",
                    description = "Masterfully tailored Italian-wool blend blazer featuring classic notch lapels, structured shoulders, double vents, and a fully lined interior. Perfect for both office power dressing and night outs.",
                    category = "Jackets & Coats",
                    price = 185.00,
                    rating = 4.7,
                    imageUrl = "https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=500&auto=format&fit=crop&q=80",
                    sizeOptions = "M,L,XL",
                    inStock = true,
                    isFeatured = true
                ),
                ClothingItem(
                    name = "Heavyweight Oversized Tee",
                    description = "Heavyweight organic cotton pocket t-shirt with comfortable drop shoulders, modern relaxed boxy fit, and highly robust ribbed crewneck. Styled for ultimate minimalist aesthetic.",
                    category = "Shirts",
                    price = 32.00,
                    rating = 4.8,
                    imageUrl = "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=500&auto=format&fit=crop&q=80",
                    sizeOptions = "S,M,L,XL,XXL",
                    inStock = true,
                    isFeatured = false
                ),
                ClothingItem(
                    name = "Minimalist Linen Blend Shirt",
                    description = "Fine organic linen and cotton blend woven into a relaxed, breathable long-sleeve structure. Features a neat point collar and custom natural wooden buttons.",
                    category = "Shirts",
                    price = 55.00,
                    rating = 4.5,
                    imageUrl = "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=500&auto=format&fit=crop&q=80",
                    sizeOptions = "S,M,L,XL",
                    inStock = true,
                    isFeatured = true
                ),
                ClothingItem(
                    name = "Merino Herringbone Sweater",
                    description = "Chunky knit herringbone stitch crewneck pullover sweater made entirely of pure, ethically sourced Merino wool. Excellent insulation and naturally breathability.",
                    category = "Sweaters",
                    price = 115.00,
                    rating = 4.9,
                    imageUrl = "https://images.unsplash.com/photo-1620799140408-edc6dcb6d633?w=500&auto=format&fit=crop&q=80",
                    sizeOptions = "S,M,L",
                    inStock = true,
                    isFeatured = false
                ),
                ClothingItem(
                    name = "Brushed Fleece Core Hoodie",
                    description = "Ultra-premium, high-grammage cotton-fleece blend hoodie, double-lined drawstring hood with silver metal aglets and a robust clean kangaroo front pocket.",
                    category = "Sweaters",
                    price = 68.00,
                    rating = 4.6,
                    imageUrl = "https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=500&auto=format&fit=crop&q=80",
                    sizeOptions = "S,M,L,XL,XXL",
                    inStock = true,
                    isFeatured = false
                ),
                ClothingItem(
                    name = "Classic Pleated Dress Chinos",
                    description = "Elegant high-rise double-pleated cotton twill trousers with side slant entry pockets, rear button welt pockets, and a clean, permanently iron-creased leg line.",
                    category = "Pants & Shorts",
                    price = 78.00,
                    rating = 4.4,
                    imageUrl = "https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=500&auto=format&fit=crop&q=80",
                    sizeOptions = "M,L,XL",
                    inStock = true,
                    isFeatured = false
                ),
                ClothingItem(
                    name = "Ripstop Stretch Cargo Joggers",
                    description = "Water-resistant stretch ripstop cargo pants featuring comfortable elastic waist, adjustable drawstring, multiple security utility pockets, and snug elastic ankle cuffs.",
                    category = "Pants & Shorts",
                    price = 85.00,
                    rating = 4.6,
                    imageUrl = "https://images.unsplash.com/photo-1517423568366-8b83523034fd?w=500&auto=format&fit=crop&q=80",
                    sizeOptions = "S,M,L,XL",
                    inStock = true,
                    isFeatured = false
                )
            )
            clothingDao.insertClothingItems(dummyItems)
        }
    }

    suspend fun addToCart(item: ClothingItem, selectedSize: String, quantity: Int) {
        val existingItem = clothingDao.findCartItem(item.id, selectedSize)
        if (existingItem != null) {
            val newQty = existingItem.selectedQuantity + quantity
            clothingDao.updateCartItem(existingItem.copy(selectedQuantity = newQty))
        } else {
            val cartItem = CartItem(
                itemId = item.id,
                name = item.name,
                category = item.category,
                price = item.price,
                imageUrl = item.imageUrl,
                selectedSize = selectedSize,
                selectedQuantity = quantity
            )
            clothingDao.insertCartItem(cartItem)
        }
    }

    suspend fun removeCartItem(cartId: Int) {
        clothingDao.deleteCartItem(cartId)
    }

    suspend fun updateCartItemQuantity(cartId: Int, quantity: Int) {
        if (quantity <= 0) {
            clothingDao.deleteCartItem(cartId)
        } else {
            clothingDao.updateCartQuantity(cartId, quantity)
        }
    }

    suspend fun clearCart() {
        clothingDao.clearCart()
    }

    suspend fun createOrder(order: OrderEntity) {
        clothingDao.insertOrder(order)
    }
}
