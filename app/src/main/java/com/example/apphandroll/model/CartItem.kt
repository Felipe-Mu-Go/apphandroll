package com.example.apphandroll.model

import java.util.UUID

data class CartItem(
    val id: String = UUID.randomUUID().toString(),
    val product: Product,
    val selectedIngredients: List<Ingredient>,
    val selectedCategoryOptions: Map<String, List<String>> = emptyMap(),
    val quantity: Int
) {

    init {
        require(quantity > 0) { "Quantity must be greater than 0" }
        require(product.basePrice >= 0) { "Product base price cannot be negative" }
        require(selectedIngredients.all { it.extraPrice >= 0 }) {
            "Ingredient extra prices cannot be negative"
        }
        selectedCategoryOptions.forEach { (categoryId, optionIds) ->
            require(categoryId.isNotBlank()) { "Category id cannot be blank" }
            require(optionIds.all { it.isNotBlank() }) { "Option ids cannot be blank" }
        }
    }

    val unitPrice: Int
        get() = product.basePrice + selectedIngredients.sumOf { it.extraPrice }

    val totalPrice: Int
        get() = unitPrice * quantity
}
