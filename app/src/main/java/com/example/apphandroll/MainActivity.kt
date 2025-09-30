package com.example.apphandroll

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppHandrollTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppHandrollScreen(onConfirm = { name ->
                        Toast.makeText(
                            this,
                            "Pedido confirmado para $name",
                            Toast.LENGTH_LONG
                        ).show()
                    })
                }
            }
        }
    }
}

data class Product(
    val name: String,
    val description: String,
    val basePrice: Int,
    val includedByCategory: Map<String, Int>
)

data class IngredientCategory(
    val name: String,
    val included: Int,
    val extraPrice: Int,
    val options: List<String>
)

data class CartItem(
    val product: Product,
    val ingredients: Map<String, List<String>>,
    val totalPrice: Int
)

private val ingredientCategories = listOf(
    IngredientCategory(
        name = "Proteína",
        included = 1,
        extraPrice = 1000,
        options = listOf("Salmón", "Atún", "Camarón", "Tofu")
    ),
    IngredientCategory(
        name = "Base",
        included = 1,
        extraPrice = 1000,
        options = listOf("Arroz", "Quinoa", "Lechuga", "Mix de algas")
    ),
    IngredientCategory(
        name = "Vegetales",
        included = 2,
        extraPrice = 500,
        options = listOf("Palta", "Pepino", "Zanahoria", "Cebollín", "Brotes")
    )
)

private val products = listOf(
    Product(
        name = "Handroll Clásico",
        description = "Un clásico con ingredientes seleccionados",
        basePrice = 4500,
        includedByCategory = mapOf(
            "Proteína" to 1,
            "Base" to 1,
            "Vegetales" to 1
        )
    ),
    Product(
        name = "Gohan Personalizado",
        description = "Arma tu gohan a tu gusto",
        basePrice = 5500,
        includedByCategory = mapOf(
            "Proteína" to 1,
            "Base" to 1,
            "Vegetales" to 2
        )
    ),
    Product(
        name = "Sushi Burger Crujiente",
        description = "Pan de arroz sellado con relleno generoso",
        basePrice = 6200,
        includedByCategory = mapOf(
            "Proteína" to 2,
            "Base" to 1,
            "Vegetales" to 2
        )
    ),
    Product(
        name = "Sushi Pleto Tradicional",
        description = "Bandeja variada para compartir",
        basePrice = 7800,
        includedByCategory = mapOf(
            "Proteína" to 2,
            "Base" to 2,
            "Vegetales" to 3
        )
    )
)

@Composable
fun AppHandrollScreen(onConfirm: (String) -> Unit) {
    var selectedProductIndex by remember { mutableStateOf(0) }
    val selectedProduct = products[selectedProductIndex]

    val ingredientSelections = remember(selectedProduct) {
        ingredientCategories.associate { category ->
            category.name to mutableStateListOf<String>()
        }
    }

    var customerName by remember { mutableStateOf("") }
    var customerLastName by remember { mutableStateOf("") }
    val cartItems = remember { mutableStateListOf<CartItem>() }

    val scrollStateProducts = rememberScrollState()
    val scrollStateIngredients = rememberScrollState()
    val scrollStateSummary = rememberScrollState()

    val productTotal = calculateTotal(selectedProduct, ingredientSelections)

    val cartTotal = cartItems.sumOf { it.totalPrice }

    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
                    .verticalScroll(scrollStateProducts),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Productos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                products.forEachIndexed { index, product ->
                    ProductCard(
                        product = product,
                        isSelected = index == selectedProductIndex,
                        onSelect = {
                            selectedProductIndex = index
                            ingredientSelections.values.forEach { it.clear() }
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
                    .verticalScroll(scrollStateIngredients),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Selección de ingredientes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                ingredientCategories.forEach { category ->
                    val selections = ingredientSelections[category.name] ?: mutableStateListOf()
                    IngredientCategorySection(
                        category = category,
                        includedLimit = selectedProduct.includedByCategory[category.name] ?: category.included,
                        selections = selections,
                        onSelectionChange = { option, checked ->
                            if (checked) {
                                if (!selections.contains(option)) selections.add(option)
                            } else {
                                selections.remove(option)
                            }
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
                    .verticalScroll(scrollStateSummary),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Resumen del pedido",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = customerLastName,
                    onValueChange = { customerLastName = it },
                    label = { Text("Apellido") },
                    modifier = Modifier.fillMaxWidth()
                )

                SummarySection(
                    product = selectedProduct,
                    ingredientSelections = ingredientSelections,
                    total = productTotal
                )

                Button(
                    onClick = {
                        val selectionsCopy = ingredientSelections.mapValues { it.value.toList() }
                        cartItems.add(
                            CartItem(
                                product = selectedProduct,
                                ingredients = selectionsCopy,
                                totalPrice = productTotal
                            )
                        )
                        ingredientSelections.values.forEach { it.clear() }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text(text = "Agregar al carrito")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Carrito",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (cartItems.isEmpty()) {
                    Text(
                        text = "Tu carrito está vacío",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    cartItems.forEach { item ->
                        CartItemCard(item = item)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Total carrito: ${formatPrice(cartTotal)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = {
                        val nameDisplay = listOf(customerName, customerLastName)
                            .filter { it.isNotBlank() }
                            .joinToString(" ")
                        if (nameDisplay.isNotBlank()) {
                            onConfirm(nameDisplay)
                            cartItems.clear()
                        }
                    },
                    enabled = cartItems.isNotEmpty() && customerName.isNotBlank() && customerLastName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text("Confirmar pedido")
                }
            }
        }
    }
}

@Composable
private fun ProductCard(product: Product, isSelected: Boolean, onSelect: () -> Unit) {
    val cardColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = product.description, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Precio base: ${formatPrice(product.basePrice)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = buildString {
                    append("Incluye: ")
                    append(product.includedByCategory.entries.joinToString { "${it.value} ${it.key}" })
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun IngredientCategorySection(
    category: IngredientCategory,
    includedLimit: Int,
    selections: MutableList<String>,
    onSelectionChange: (String, Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "${category.name} (incluye $includedLimit, extra ${formatPrice(category.extraPrice)})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        category.options.forEach { option ->
            val checked = selections.contains(option)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = checked, onCheckedChange = { onSelectionChange(option, it) })
                Text(text = option, style = MaterialTheme.typography.bodyMedium)
            }
        }
        val extraCount = (selections.size - includedLimit).coerceAtLeast(0)
        if (extraCount > 0) {
            Text(
                text = "Extras: $extraCount x ${formatPrice(category.extraPrice)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
        Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    }
}

@Composable
private fun SummarySection(
    product: Product,
    ingredientSelections: Map<String, MutableList<String>>,
    total: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        ingredientSelections.forEach { (category, selections) ->
            Text(
                text = "$category: ${if (selections.isEmpty()) "Sin seleccionar" else selections.joinToString()}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = "Total producto: ${formatPrice(total)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CartItemCard(item: CartItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = item.product.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            item.ingredients.forEach { (category, options) ->
                if (options.isNotEmpty()) {
                    Text(
                        text = "$category: ${options.joinToString()}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Text(
                text = "Precio: ${formatPrice(item.totalPrice)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun calculateTotal(product: Product, ingredientSelections: Map<String, MutableList<String>>): Int {
    var total = product.basePrice
    ingredientSelections.forEach { (category, selections) ->
        val included = product.includedByCategory[category] ?: 0
        val categoryConfig = ingredientCategories.firstOrNull { it.name == category }
        val extraPrice = categoryConfig?.extraPrice ?: 0
        val extras = (selections.size - included).coerceAtLeast(0)
        total += extras * extraPrice
    }
    return total
}

private fun formatPrice(price: Int): String = "$" + String.format("%,d", price).replace(',', '.')
