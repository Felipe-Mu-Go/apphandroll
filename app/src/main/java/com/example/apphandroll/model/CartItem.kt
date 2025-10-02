package com.example.apphandroll.model

import java.util.UUID

data class CartItem(
    val id: String = UUID.randomUUID().toString(),
    val product: Product,
    val selectedIngredients: List<Ingredient>,
    val selectedCategoryOptions: Map<String, List<String>> = emptyMap(),
    val quantity: Int
) {
    val unitPrice: Int
        get() = product.basePrice + selectedIngredients.sumOf { it.extraPrice }

    val totalPrice: Int
        get() = unitPrice * quantity
}
