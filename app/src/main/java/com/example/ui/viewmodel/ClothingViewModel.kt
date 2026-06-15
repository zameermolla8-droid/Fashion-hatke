package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.CartItem
import com.example.data.local.ClothingItem
import com.example.data.local.OrderEntity
import com.example.data.repository.ClothingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

class ClothingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ClothingRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ClothingRepository(database.clothingDao())
        
        // Seed database with amazing clothes on init!
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    // Search and category selection state
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")

    // Categories list based on our seeded clothing items
    val categories = listOf("All", "Jackets & Coats", "Shirts", "Sweaters", "Pants & Shorts")

    // Filtered Clothing items list
    val clothingItems: StateFlow<List<ClothingItem>> = combine(
        repository.allClothingItems,
        searchQuery,
        selectedCategory
    ) { items, query, category ->
        items.filter { item ->
            val matchesSearch = item.name.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || item.category == category
            matchesSearch && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Highlighted featured items
    val featuredItems: StateFlow<List<ClothingItem>> = repository.allClothingItems
        .map { items -> items.filter { it.isFeatured } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Selected item for Detail Page
    private val _selectedItemId = MutableStateFlow<Int?>(null)
    val selectedItem: StateFlow<ClothingItem?> = _selectedItemId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repository.allClothingItems.map { list -> list.find { it.id == id } }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun selectItem(itemId: Int?) {
        _selectedItemId.value = itemId
    }

    // Cart details
    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Subtotal state
    val subtotalPrice: StateFlow<Double> = repository.cartItems
        .map { items -> items.sumOf { it.price * it.selectedQuantity } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    // Constant shipping ($10) & tax rate (8.5%)
    val shippingFee = 10.00
    val taxRate = 0.085

    // Checkout/Order State
    private val _orderPlacementSuccess = MutableSharedFlow<OrderEntity>()
    val orderPlacementSuccess = _orderPlacementSuccess.asSharedFlow()

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun selectCategory(category: String) {
        selectedCategory.value = category
    }

    // Database cart modifications
    fun addItemToCart(item: ClothingItem, selectedSize: String, quantity: Int) {
        viewModelScope.launch {
            repository.addToCart(item, selectedSize, quantity)
        }
    }

    fun removeCartItem(cartId: Int) {
        viewModelScope.launch {
            repository.removeCartItem(cartId)
        }
    }

    fun incrementCartItem(cartItem: CartItem) {
        viewModelScope.launch {
            repository.updateCartItemQuantity(cartItem.id, cartItem.selectedQuantity + 1)
        }
    }

    fun decrementCartItem(cartItem: CartItem) {
        viewModelScope.launch {
            repository.updateCartItemQuantity(cartItem.id, cartItem.selectedQuantity - 1)
        }
    }

    // Format currency helper
    fun formatCurrency(amount: Double): String {
        return NumberFormat.getCurrencyInstance(Locale.US).format(amount)
    }

    // Place order and trigger email compose
    fun checkoutAndPlaceOrder(
        context: Context,
        name: String,
        email: String,
        address: String,
        phone: String,
        promoCode: String = ""
    ) {
        val currentCart = cartItems.value
        if (currentCart.isEmpty()) {
            Toast.makeText(context, "Your cart is empty", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            // Calculate final price
            val subtotal = currentCart.sumOf { it.price * it.selectedQuantity }
            val discount = if (promoCode.uppercase() == "WELCOME10") subtotal * 0.10 else 0.0
            val taxAmount = (subtotal - discount) * taxRate
            val grandTotal = subtotal - discount + taxAmount + shippingFee

            // Generate order ID
            val orderId = "ORD-${UUID.randomUUID().toString().substring(0, 8).uppercase()}"

            // Format items list summary text
            val itemsBuilder = StringBuilder()
            currentCart.forEachIndexed { index, cartItem ->
                itemsBuilder.append("${index + 1}. ${cartItem.name} [Size: ${cartItem.selectedSize}] x ${cartItem.selectedQuantity} - ${formatCurrency(cartItem.price * cartItem.selectedQuantity)}\n")
            }
            val itemsSummary = itemsBuilder.toString()

            // Save order to history
            val order = OrderEntity(
                orderId = orderId,
                customerName = name,
                customerEmail = email,
                customerAddress = address,
                customerPhone = phone,
                itemsSummary = itemsSummary,
                totalPrice = grandTotal,
                timestamp = System.currentTimeMillis()
            )

            repository.createOrder(order)
            repository.clearCart()

            // Construct and trigger highly professional order email and launch mail client pre-populated.
            // Recipient is zameermolla8@gmail.com
            val gmailAddress = "zameermolla8@gmail.com"
            val emailSubject = "NEW ORDER PLACED - $orderId"
            
            val emailBody = """
                =========================================
                NEW CUSTOMER CLOTHING ORDER (Aura Boutique)
                =========================================
                Order ID: $orderId
                Date & Time: ${java.text.DateFormat.getDateTimeInstance().format(java.util.Date(order.timestamp))}
                
                CUSTOMER DETAILS:
                Name: $name
                Email: $email
                Phone: $phone
                Shipping Address: $address
                
                ORDER DETAILS:
                $itemsSummary
                -----------------------------------------
                Financial Breakdown:
                Subtotal: ${formatCurrency(subtotal)}
                Promo Applied: ${if (discount > 0) "WELCOME10 (-10%)" else "None"}
                Discount: -${formatCurrency(discount)}
                Estimated Tax (8.5%): ${formatCurrency(taxAmount)}
                Shipping & Handling: ${formatCurrency(shippingFee)}
                GRAND TOTAL: ${formatCurrency(grandTotal)}
                -----------------------------------------
                Status: Pending Fulfillment
                =========================================
                
                This order is ready to be processed and fulfilled. Direct this email query to zameermolla8@gmail.com for full shipping tracking updates.
            """.trimIndent()

            // Standard intent chooser that is highly compatible
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:") // only email apps should handle this
                putExtra(Intent.EXTRA_EMAIL, arrayOf(gmailAddress))
                putExtra(Intent.EXTRA_SUBJECT, emailSubject)
                putExtra(Intent.EXTRA_TEXT, emailBody)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            try {
                val chooser = Intent.createChooser(intent, "Select Gmail or Email client to process order summary")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (e: Exception) {
                // Fallback direct launch mail if chooser fails
                try {
                    val directIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "message/rfc822"
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(gmailAddress))
                        putExtra(Intent.EXTRA_SUBJECT, emailSubject)
                        putExtra(Intent.EXTRA_TEXT, emailBody)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(directIntent)
                } catch (ex: Exception) {
                    Toast.makeText(context, "Could not open mail client. Copy details to send.", Toast.LENGTH_LONG).show()
                }
            }

            _orderPlacementSuccess.emit(order)
        }
    }

    val ordersList: StateFlow<List<OrderEntity>> = repository.ordersList
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
