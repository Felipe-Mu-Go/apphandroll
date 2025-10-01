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
            id = "handroll_salmon",
            name = "Handroll de salmón",
            basePrice = 3500,
            baseIncludedDescription = "Alga nori, arroz sazonado y palta",
            optionalIngredients = listOf(
                Ingredient(id = "queso_crema", name = "Queso crema", extraPrice = 400),
                Ingredient(id = "sesamo", name = "Sésamo tostado", extraPrice = 200),
                Ingredient(id = "ciboulette", name = "Ciboulette", extraPrice = 0),
                Ingredient(id = "camarones", name = "Camarones", extraPrice = 700)
            )
        ),
        Product(
            id = "gohan_pollo",
            name = "Gohan crujiente",
            basePrice = 5900,
            baseIncludedDescription = "Arroz gohan, pollo teriyaki, cebollín",
            optionalIngredients = listOf(
                Ingredient(id = "palta", name = "Palta extra", extraPrice = 600),
                Ingredient(id = "tobiko", name = "Tobiko", extraPrice = 900),
                Ingredient(id = "salsa_spicy", name = "Salsa spicy", extraPrice = 300),
                Ingredient(id = "chips_camarones", name = "Chips de camarón", extraPrice = 400)
            )
        ),
        Product(
            id = "sushipleto",
            name = "Sushipleto familiar",
            basePrice = 9900,
            baseIncludedDescription = "12 rolls surtidos y salsa de soya",
            optionalIngredients = listOf(
                Ingredient(id = "wasabi_extra", name = "Wasabi extra", extraPrice = 200),
                Ingredient(id = "gengibre", name = "Jengibre encurtido", extraPrice = 200),
                Ingredient(id = "salsa_agridulce", name = "Salsa agridulce", extraPrice = 300),
                Ingredient(id = "palillos", name = "Palillos adicionales", extraPrice = 100)
            )
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
