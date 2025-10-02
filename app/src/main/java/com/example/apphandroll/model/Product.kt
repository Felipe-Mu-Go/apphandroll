package com.example.apphandroll.model

data class Product(
    val id: String,
    val name: String,
    val basePrice: Int,
    val baseIncludedDescription: String,
    val optionalIngredients: List<Ingredient>,
    val ingredientCategories: List<IngredientCategory> = emptyList()
)
