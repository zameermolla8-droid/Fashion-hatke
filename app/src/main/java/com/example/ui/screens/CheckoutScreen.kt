package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LightGreyBg
import com.example.ui.theme.SandAccent
import com.example.ui.theme.SoftBone
import com.example.ui.viewmodel.ClothingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: ClothingViewModel,
    initialPromoCode: String,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToOrders: () -> Unit
) {
    val context = LocalContext.current
    val cartItems by viewModel.cartItems.collectAsState()
    val subtotal by viewModel.subtotalPrice.collectAsState()

    // Customer form inputs
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var promoCode by remember { mutableStateOf(initialPromoCode) }

    // Error states
    var nameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var addressError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }

    // Success state Overlay
    var showReceiptOverlay by remember { mutableStateOf(false) }
    var generatedOrderId by remember { mutableStateOf("") }
    var finalAmountPaid by remember { mutableStateOf(0.0) }

    // Observe order placement success to display details in confirmation
    LaunchedEffect(viewModel.orderPlacementSuccess) {
        viewModel.orderPlacementSuccess.collect { order ->
            generatedOrderId = order.orderId
            finalAmountPaid = order.totalPrice
            showReceiptOverlay = true
        }
    }

    // Calculations
    val isPromoApplied = promoCode.uppercase() == "WELCOME10"
    val discount = if (isPromoApplied) subtotal * 0.10 else 0.0
    val taxableAmount = subtotal - discount
    val tax = taxableAmount * viewModel.taxRate
    val isFreeShipping = subtotal >= 150.00
    val shipping = if (isFreeShipping) 0.00 else viewModel.shippingFee
    val grandTotal = taxableAmount + tax + shipping

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "SECURE CHECKOUT", 
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        fontSize = 18.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("checkout_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(LightGreyBg)
                    .verticalScroll(rememberScrollState())
            ) {
                // Form Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Customer Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Full Name Input
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                nameError = false
                            },
                            label = { Text("Full Name *") },
                            isError = nameError,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_name_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        if (nameError) {
                            Text(
                                text = "Name is required",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Email Input
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                emailError = false
                            },
                            label = { Text("Email Address *") },
                            isError = emailError,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_email_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        if (emailError) {
                            Text(
                                text = "Enter a valid email address",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Phone Input
                        OutlinedTextField(
                            value = phone,
                            onValueChange = {
                                phone = it
                                phoneError = false
                            },
                            label = { Text("Phone Number *") },
                            isError = phoneError,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_phone_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        if (phoneError) {
                            Text(
                                text = "Enter a valid phone number (min 6 digits)",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Shipping Address Input
                        OutlinedTextField(
                            value = address,
                            onValueChange = {
                                address = it
                                addressError = false
                            },
                            label = { Text("Shipping Address *") },
                            isError = addressError,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .testTag("checkout_address_input"),
                            maxLines = 3,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        if (addressError) {
                            Text(
                                text = "Shipping address is required",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }
                    }
                }

                // Summary Cost Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Order Details Review",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        SummaryLine(label = "Subtotal", value = viewModel.formatCurrency(subtotal))
                        if (isPromoApplied) {
                            SummaryLine(
                                label = "Promo Discount (WELCOME10)",
                                value = "-${viewModel.formatCurrency(discount)}",
                                valueColor = SandAccent
                            )
                        }
                        SummaryLine(label = "Taxes (8.5%)", value = viewModel.formatCurrency(tax))
                        val shipText = if (isFreeShipping) "FREE" else viewModel.formatCurrency(shipping)
                        SummaryLine(
                            label = "Shipping",
                            value = shipText,
                            valueColor = if (isFreeShipping) SandAccent else Color.Black
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = Color(0xFFF5F5F5))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GRAND TOTAL",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                            Text(
                                text = viewModel.formatCurrency(grandTotal),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Place order major action
                        Button(
                            onClick = {
                                // Form Validation
                                var hasError = false
                                if (name.trim().isEmpty()) {
                                    nameError = true
                                    hasError = true
                                }
                                if (email.trim().isEmpty() || !email.contains("@")) {
                                    emailError = true
                                    hasError = true
                                }
                                if (phone.trim().length < 6) {
                                    phoneError = true
                                    hasError = true
                                }
                                if (address.trim().isEmpty()) {
                                    addressError = true
                                    hasError = true
                                }

                                if (!hasError) {
                                    viewModel.checkoutAndPlaceOrder(
                                        context = context,
                                        name = name.trim(),
                                        email = email.trim(),
                                        address = address.trim(),
                                        phone = phone.trim(),
                                        promoCode = promoCode.trim()
                                    )
                                } else {
                                    Toast.makeText(context, "Please correct the errors in the form", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("place_order_button"),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.Done, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Place Order & Open Gmail",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Note: Placing authorization immediately triggers Gmail client pre-filled to zameermolla8@gmail.com for instant fulfillment processing.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // High Fidelity Checkout Ticket overlay
            AnimatedVisibility(
                visible = showReceiptOverlay,
                enter = fadeIn() + expandIn(),
                exit = fadeOut() + shrinkOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .testTag("receipt_card"),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(SandAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Order Placed Successfully!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "Email generated to shop owner",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Ticket breakdown box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SoftBone, RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Order Receipt:", fontSize = 13.sp, color = Color.Gray)
                                        Text(text = generatedOrderId, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Processed for:", fontSize = 13.sp, color = Color.Gray)
                                        Text(text = name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Total Charged:", fontSize = 13.sp, color = Color.Gray)
                                        Text(text = viewModel.formatCurrency(finalAmountPaid), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "A copy of this invoice has been loaded into your Email client composed automatically. Please hit 'Send' in the email app to deliver the details instantly to our center.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Action Row
                            Button(
                                onClick = {
                                    showReceiptOverlay = false
                                    onNavigateToHome()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("receipt_home_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text("Continue Shopping", fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    showReceiptOverlay = false
                                    onNavigateToOrders()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("receipt_orders_btn"),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text("View Order History Logs", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
