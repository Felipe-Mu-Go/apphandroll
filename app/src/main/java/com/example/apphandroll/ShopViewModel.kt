package com.example.apphandroll

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.example.apphandroll.model.CartItem
import com.example.apphandroll.model.Ingredient
import com.example.apphandroll.model.IngredientCategory
import com.example.apphandroll.model.IngredientOption
import com.example.apphandroll.model.Product
import com.example.apphandroll.model.OrderCustomerDetails
import java.util.LinkedHashMap

private fun createSharedIngredientCategories(prefix: String): List<IngredientCategory> {
    return listOf(
        IngredientCategory(
            id = "${prefix}_proteina",
            title = "Proteínas",
            description = "Incluye hasta 1 proteína sin costo. Cada proteína adicional suma $1.000.",
            options = listOf(
                IngredientOption(id = "${prefix}_proteina_pollo", name = "Pollo"),
                IngredientOption(id = "${prefix}_proteina_camaron", name = "Camarón"),
                IngredientOption(id = "${prefix}_proteina_carne", name = "Carne"),
                IngredientOption(id = "${prefix}_proteina_kanikama", name = "Kanikama"),
                IngredientOption(id = "${prefix}_proteina_palmito", name = "Palmito"),
                IngredientOption(id = "${prefix}_proteina_champinon", name = "Champiñón")
            ),
            includedCount = 1,
            extraPrice = 1000
        ),
        IngredientCategory(
            id = "${prefix}_base",
            title = "Bases",
            description = "Incluye hasta 1 base sin costo. Cada base adicional suma $1.000.",
            options = listOf(
                IngredientOption(id = "${prefix}_base_queso", name = "Queso"),
                IngredientOption(id = "${prefix}_base_palta", name = "Palta")
            ),
            includedCount = 1,
            extraPrice = 1000
        ),
        IngredientCategory(
            id = "${prefix}_vegetal",
            title = "Vegetales",
            description = "Incluye hasta 1 vegetal sin costo. Cada vegetal adicional suma $500.",
            options = listOf(
                IngredientOption(id = "${prefix}_vegetal_cebollin", name = "Cebollín"),
                IngredientOption(id = "${prefix}_vegetal_ciboulette", name = "Ciboulette"),
                IngredientOption(id = "${prefix}_vegetal_choclo", name = "Choclo")
            ),
            includedCount = 1,
            extraPrice = 500
        )
    )
}

class ShopViewModel : ViewModel() {
    val products: List<Product> = listOf(
        Product(
            id = "handroll",
            name = "Handroll",
            basePrice = 3500,
            baseIncludedDescription = "Incluye hasta 1 proteína, 1 base y 1 vegetal sin costo extra. Proteína o base extra +$1.000, vegetal extra +$500.",
            optionalIngredients = emptyList(),
            ingredientCategories = createSharedIngredientCategories("handroll")
        ),
        Product(
            id = "sushiburger",
            name = "Sushiburger",
            basePrice = 5500,
            baseIncludedDescription = "Arroz y nori, elige tu proteína favorita, una base cremosa y vegetales frescos.",
            optionalIngredients = listOf(
                Ingredient(id = "sushiburger_extra_proteina", name = "Extra proteína", extraPrice = 1000),
                Ingredient(id = "sushiburger_extra_vegetal", name = "Vegetal extra", extraPrice = 500)
            ),
            ingredientCategories = createSharedIngredientCategories("sushiburger")
        ),
        Product(
            id = "sushipleto",
            name = "Sushipleto",
            basePrice = 5000,
            baseIncludedDescription = "Base de arroz y nori, relleno con una proteína, una base cremosa y un vegetal fresco a tu elección.",
            optionalIngredients = emptyList(),
            ingredientCategories = createSharedIngredientCategories("sushipleto")
        ),
        Product(
            id = "sushipleto_vegetariano",
            name = "Sushipleto Vegetariano",
            basePrice = 4500,
            baseIncludedDescription = "Deliciosa combinación de champiñón o palmito, queso, palta y un toque de cebollín o ciboulette.",
            optionalIngredients = emptyList(),
            ingredientCategories = listOf(
                IngredientCategory(
                    id = "sushipleto_vegetariano_base",
                    title = "Base",
                    description = "Incluye 1 base sin costo. Agregar otra base suma $1.000.",
                    options = listOf(
                        IngredientOption(id = "sushipleto_vegetariano_base_champinon", name = "Champiñón"),
                        IngredientOption(id = "sushipleto_vegetariano_base_palmito", name = "Palmito")
                    ),
                    includedCount = 1,
                    extraPrice = 1000
                ),
                IngredientCategory(
                    id = "sushipleto_vegetariano_a_eleccion_cremoso",
                    title = "A elección (Queso/Palta)",
                    description = "Cada opción agrega un extra de $1.000.",
                    options = listOf(
                        IngredientOption(id = "sushipleto_vegetariano_eleccion_queso", name = "Queso"),
                        IngredientOption(id = "sushipleto_vegetariano_eleccion_palta", name = "Palta")
                    ),
                    includedCount = 0,
                    extraPrice = 1000
                ),
                IngredientCategory(
                    id = "sushipleto_vegetariano_a_eleccion_vegetal",
                    title = "A elección (Cebollín/Ciboulette)",
                    description = "Cada opción agrega un extra de $500.",
                    options = listOf(
                        IngredientOption(id = "sushipleto_vegetariano_eleccion_cebollin", name = "Cebollín"),
                        IngredientOption(id = "sushipleto_vegetariano_eleccion_ciboulette", name = "Ciboulette")
                    ),
                    includedCount = 0,
                    extraPrice = 500
                )
            )
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

    var lastOrderCustomerDetails: OrderCustomerDetails? = null
        private set

    fun addToCart(
        product: Product,
        ingredients: List<Ingredient>,
        categorySelections: Map<String, List<String>>,
        quantity: Int
    ) {
        require(quantity > 0) { "Quantity must be greater than 0" }
        val sanitizedIngredients = sanitizeIngredients(ingredients)
        val sanitizedSelections = sanitizeCategorySelections(categorySelections)
        _cart.add(
            CartItem(
                product = product,
                selectedIngredients = sanitizedIngredients,
                selectedCategoryOptions = sanitizedSelections,
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
            val sanitizedIngredients = newIngredients?.let(::sanitizeIngredients)
            val sanitizedSelections = newCategorySelections?.let(::sanitizeCategorySelections)
            val sanitizedQuantity = newQuantity?.also {
                require(it > 0) { "Quantity must be greater than 0" }
            }
            _cart[index] = current.copy(
                selectedIngredients = sanitizedIngredients ?: current.selectedIngredients,
                selectedCategoryOptions = sanitizedSelections ?: current.selectedCategoryOptions,
                quantity = sanitizedQuantity ?: current.quantity
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

    fun recordOrderCustomer(details: OrderCustomerDetails) {
        lastOrderCustomerDetails = details
    }

    private fun sanitizeIngredients(ingredients: List<Ingredient>): List<Ingredient> {
        return buildList(ingredients.size) {
            ingredients.forEach { ingredient ->
                require(ingredient.id.isNotBlank()) { "Ingredient id cannot be blank" }
                require(ingredient.name.isNotBlank()) { "Ingredient name cannot be blank" }
                require(ingredient.extraPrice >= 0) { "Ingredient extra price cannot be negative" }
                add(ingredient)
            }
        }
    }

    private fun sanitizeCategorySelections(
        categorySelections: Map<String, List<String>>
    ): Map<String, List<String>> {
        if (categorySelections.isEmpty()) return emptyMap()
        val sanitized = LinkedHashMap<String, List<String>>(categorySelections.size)
        categorySelections.forEach { (categoryId, optionIds) ->
            require(categoryId.isNotBlank()) { "Category id cannot be blank" }
            val cleanedOptionIds = optionIds.map { optionId ->
                require(optionId.isNotBlank()) { "Option id cannot be blank for category $categoryId" }
                optionId
            }.distinct()
            sanitized[categoryId] = cleanedOptionIds
        }
        return sanitized
    }
}
