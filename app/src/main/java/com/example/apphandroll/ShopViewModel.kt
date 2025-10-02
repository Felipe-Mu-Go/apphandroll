package com.example.apphandroll

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.example.apphandroll.model.CartItem
import com.example.apphandroll.model.Ingredient
import com.example.apphandroll.model.Product

class ShopViewModel : ViewModel() {
    val products: List<Product> = listOf(
        Product(
            id = "handroll",
            name = "Handroll",
            basePrice = 3500,
            baseIncludedDescription = "Delicioso cono de arroz y nori, relleno con la proteína que elijas, más una mezcla cremosa y vegetales frescos que se derriten en cada mordisco.",
            optionalIngredients = listOf(
                Ingredient(id = "handroll_extra_proteina_base", name = "Proteína o base extra", extraPrice = 1000),
                Ingredient(id = "handroll_extra_vegetal", name = "Vegetal extra", extraPrice = 500)
            )
        ),
        Product(
            id = "sushiburger",
            name = "Sushiburger",
            basePrice = 5500,
            baseIncludedDescription = "Arroz y nori, elige tu proteína favorita, una base cremosa y vegetales frescos.",
            optionalIngredients = listOf(
                Ingredient(id = "sushiburger_extra_proteina", name = "Extra proteína", extraPrice = 1000),
                Ingredient(id = "sushiburger_extra_vegetal", name = "Vegetal extra", extraPrice = 500)
            )
        ),
        Product(
            id = "sushipleto",
            name = "Sushipleto",
            basePrice = 5000,
            baseIncludedDescription = "Base de arroz y nori, relleno con una proteína, una base cremosa y un vegetal fresco a tu elección.",
            optionalIngredients = emptyList()
        ),
        Product(
            id = "sushipleto_vegetariano",
            name = "Sushipleto Vegetariano",
            basePrice = 4500,
            baseIncludedDescription = "Deliciosa combinación de champiñón o palmito, queso, palta y un toque de cebollín o ciboulette.",
            optionalIngredients = emptyList()
        ),
        Product(
            id = "gohan",
            name = "Gohan",
            basePrice = 6500,
            baseIncludedDescription = "Incluye base de arroz más cebollín, más 4 ingredientes a elección para crear tu combinación perfecta.",
            optionalIngredients = emptyList()
        )
    )

    private val _cart: SnapshotStateList<CartItem> = mutableStateListOf()
    val cart: SnapshotStateList<CartItem> = _cart

    fun addToCart(product: Product, ingredients: List<Ingredient>, quantity: Int) {
        _cart.add(
            CartItem(
                product = product,
                selectedIngredients = ingredients,
                quantity = quantity
            )
        )
    }

    fun updateCartItem(itemId: String, newIngredients: List<Ingredient>?, newQuantity: Int?) {
        val index = _cart.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val current = _cart[index]
            _cart[index] = current.copy(
                selectedIngredients = newIngredients ?: current.selectedIngredients,
                quantity = newQuantity ?: current.quantity
            )
        }
    }

    fun removeCartItem(itemId: String) {
        val index = _cart.indexOfFirst { it.id == itemId }
        if (index != -1) {
            _cart.removeAt(index)
        }
    }

    fun total(): Int = _cart.sumOf { it.totalPrice }

    fun clearCart() {
        _cart.clear()
    }
}
