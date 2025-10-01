package com.example.apphandroll.model

import java.util.UUID

data class CartItem(
    val id: String = UUID.randomUUID().toString(),
    val product: Product,
    val selectedIngredients: List<Ingredient>,
    val quantity: Int
) {
    val unitPrice: Int
        get() = product.basePrice + selectedIngredients.sumOf { it.extraPrice }

    val totalPrice: Int
        get() = unitPrice * quantity
}
