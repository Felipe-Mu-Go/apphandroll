package com.example.apphandroll

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.example.apphandroll.model.CartItem
import com.example.apphandroll.model.Ingredient
import com.example.apphandroll.model.IngredientCategory
import com.example.apphandroll.model.IngredientOption
import com.example.apphandroll.model.Product

class ShopViewModel : ViewModel() {
    val products: List<Product> = listOf(
        Product(
            id = "handroll",
            name = "Handroll",
            basePrice = 3500,
            baseIncludedDescription = "Elige tu proteína favorita, acompáñala con una base cremosa y un vegetal fresco. Si quieres más, puedes agregar proteínas o bases extra por $1.000 y vegetales extra por $500.",
            optionalIngredients = emptyList(),
            ingredientCategories = listOf(
                IngredientCategory(
                    id = "handroll_proteina",
                    title = "Proteína",
                    description = "Selecciona al menos 1 proteína para tu handroll. Cada proteína adicional suma $1.000.",
                    options = listOf(
                        IngredientOption(id = "handroll_proteina_pollo", name = "Pollo"),
                        IngredientOption(id = "handroll_proteina_camaron", name = "Camarón"),
                        IngredientOption(id = "handroll_proteina_carne", name = "Carne"),
                        IngredientOption(id = "handroll_proteina_kanikama", name = "Kanikama"),
                        IngredientOption(id = "handroll_proteina_palmito", name = "Palmito"),
                        IngredientOption(id = "handroll_proteina_champinon", name = "Champiñón")
                    ),
                    includedCount = 1,
                    extraPrice = 1000
                ),
                IngredientCategory(
                    id = "handroll_base",
                    title = "Base cremosa",
                    description = "Incluye 1 base cremosa. Cada base extra suma $1.000.",
                    options = listOf(
                        IngredientOption(id = "handroll_base_queso", name = "Queso"),
                        IngredientOption(id = "handroll_base_palta", name = "Palta")
                    ),
                    includedCount = 1,
                    extraPrice = 1000
                ),
                IngredientCategory(
                    id = "handroll_vegetal",
                    title = "Vegetal fresco",
                    description = "Escoge 1 vegetal para dar frescura. Cada vegetal adicional suma $500.",
                    options = listOf(
                        IngredientOption(id = "handroll_vegetal_cebollin", name = "Cebollín"),
                        IngredientOption(id = "handroll_vegetal_ciboulette", name = "Ciboulette"),
                        IngredientOption(id = "handroll_vegetal_choclo", name = "Choclo")
                    ),
                    includedCount = 1,
                    extraPrice = 500
                )
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

    fun addToCart(
        product: Product,
        ingredients: List<Ingredient>,
        categorySelections: Map<String, List<String>>,
        quantity: Int
    ) {
        _cart.add(
            CartItem(
                product = product,
                selectedIngredients = ingredients,
                selectedCategoryOptions = categorySelections,
                quantity = quantity
            )
        )
    }

    fun updateCartItem(
        itemId: String,
        newIngredients: List<Ingredient>?,
        newCategorySelections: Map<String, List<String>>?,
        newQuantity: Int?
    ) {
        val index = _cart.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val current = _cart[index]
            _cart[index] = current.copy(
                selectedIngredients = newIngredients ?: current.selectedIngredients,
                selectedCategoryOptions = newCategorySelections ?: current.selectedCategoryOptions,
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
