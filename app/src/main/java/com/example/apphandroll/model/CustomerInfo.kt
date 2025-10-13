package com.example.apphandroll.model

/**
 * Holds customer information collected before confirming an order.
 */
data class CustomerInfo(
    val name: String,
    val lastName: String,
    val email: String?,
    val phone: String,
    val notes: String? = null
) {
    val customerName: String
        get() = listOf(name.trim(), lastName.trim())
            .filter { it.isNotEmpty() }
            .joinToString(" ")
}

/**
 * Aggregated customer details ready to be passed to the order confirmation flow.
 */
data class OrderCustomerDetails(
    val customerName: String,
    val email: String?,
    val phone: String
)
