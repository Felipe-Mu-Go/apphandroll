package com.example.apphandroll.model

data class IngredientCategory(
    val id: String,
    val title: String,
    val description: String,
    val options: List<IngredientOption>,
    val includedCount: Int,
    val extraPrice: Int
)

data class IngredientOption(
    val id: String,
    val name: String
)
