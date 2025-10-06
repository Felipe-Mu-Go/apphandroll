package com.example.apphandroll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.apphandroll.R
import com.example.apphandroll.model.CartItem
import com.example.apphandroll.model.Ingredient
import com.example.apphandroll.model.IngredientCategory
import com.example.apphandroll.model.IngredientOption
import com.example.apphandroll.model.Product
import com.example.apphandroll.formatPrice

private const val SUSHIPLETO_VEGETARIANO_ID = "sushipleto_vegetariano"
private const val SUSHIPLETO_VEGETARIANO_BASE_CATEGORY_ID = "sushipleto_vegetariano_base"
private const val SUSHIPLETO_VEGETARIANO_CREAMY_CATEGORY_ID = "sushipleto_vegetariano_a_eleccion_cremoso"
private const val SUSHIPLETO_VEGETARIANO_VEGETAL_CATEGORY_ID = "sushipleto_vegetariano_a_eleccion_vegetal"

private val FLEXIBLE_INGREDIENT_PRODUCT_IDS = setOf(
    "handroll",
    "sushiburger",
    "sushipleto",
    SUSHIPLETO_VEGETARIANO_ID
)

private const val DEFAULT_FLEXIBLE_INGREDIENT_SUBTITLE = "Puedes escoger libremente entre Proteínas, Bases y Vegetales.\nIncluye hasta 1 por categoría; extras: proteína/base +$1.000, vegetal +$500."

private val FLEXIBLE_INGREDIENT_SUBTITLES = mapOf(
    SUSHIPLETO_VEGETARIANO_ID to "Una base incluida: Champiñón o Palmito.\nPuedes agregar más: base extra +$1.000.\nA elección: Queso/Palta (+$1.000 c/u), Cebollín/Ciboulette (+$500 c/u)."
)

private fun getFlexibleIngredientSubtitle(productId: String): String {
    return FLEXIBLE_INGREDIENT_SUBTITLES[productId] ?: DEFAULT_FLEXIBLE_INGREDIENT_SUBTITLE
}

private sealed interface CatalogItem

private data class SingleProductItem(val product: Product) : CatalogItem

private data class SushipletoCombinedItem(
    val classic: Product,
    val vegetarian: Product
) : CatalogItem

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppHandrollTheme {
                ShopApp()
            }
        }
    }
}

@Composable
fun ShopApp(viewModel: ShopViewModel = viewModel()) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectionProduct by remember { mutableStateOf<Product?>(null) }
    var selectionItemId by remember { mutableStateOf<String?>(null) }
    val selectedIngredientIds = remember { mutableStateListOf<String>() }
    val selectedCategoryOptions = remember { mutableStateMapOf<String, List<String>>() }
    var selectionQuantity by remember { mutableStateOf(1) }
    var showSelector by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showOrderDialog by remember { mutableStateOf(false) }
    var pendingCatalogSnackbar by remember { mutableStateOf(false) }

    fun resetSelection() {
        selectionProduct = null
        selectionItemId = null
        selectedIngredientIds.clear()
        selectedCategoryOptions.clear()
        selectionQuantity = 1
        showSelector = false
        showConfirmDialog = false
    }

    LaunchedEffect(pendingCatalogSnackbar) {
        if (pendingCatalogSnackbar) {
            snackbarHostState.showSnackbar("Pedido confirmado")
            pendingCatalogSnackbar = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "catalog",
            modifier = Modifier.padding(padding)
        ) {
            composable("catalog") {
                ProductListScreen(
                    products = viewModel.products,
                    cartCount = viewModel.cart.sumOf { it.quantity },
                    onProductClick = { product ->
                        selectionProduct = product
                        selectionItemId = null
                        selectedIngredientIds.clear()
                        selectedCategoryOptions.clear()
                        selectionQuantity = 1
                        showSelector = true
                    },
                    onCartClick = {
                        navController.navigate("cart")
                    }
                )
            }
            composable("cart") {
                CartScreen(
                    cartItems = viewModel.cart,
                    total = viewModel.total(),
                    onBack = { navController.popBackStack() },
                    onEditItem = { item ->
                        selectionProduct = item.product
                        selectionItemId = item.id
                        selectedIngredientIds.clear()
                        selectedIngredientIds.addAll(item.selectedIngredients.map { it.id })
                        selectedCategoryOptions.clear()
                        item.selectedCategoryOptions.forEach { entry: Map.Entry<String, List<String>> ->
                            val categoryId = entry.key
                            val optionIds = entry.value
                            selectedCategoryOptions[categoryId] = optionIds.toList()
                        }
                        selectionQuantity = item.quantity
                        showSelector = true
                    },
                    onRemoveItem = { viewModel.removeCartItem(it) },
                    onConfirmOrder = {
                        if (viewModel.cart.isNotEmpty()) {
                            showOrderDialog = true
                        }
                    }
                )
            }
        }
    }

    val currentProduct = selectionProduct
    val currentCategorySelections = if (currentProduct != null) {
        currentProduct.ingredientCategories.associate { category: IngredientCategory ->
            category.id to selectedCategoryOptions[category.id].orEmpty()
        }
    } else {
        emptyMap()
    }

    if (showSelector && currentProduct != null) {
        val totalSelections = currentCategorySelections.values.sumOf { it.size }
        val canContinue = if (FLEXIBLE_INGREDIENT_PRODUCT_IDS.contains(currentProduct.id)) {
            totalSelections >= 1
        } else {
            currentProduct.ingredientCategories.all { category: IngredientCategory ->
                currentCategorySelections[category.id].orEmpty().size >= category.includedCount
            }
        }
        IngredientSelectorDialog(
            product = currentProduct,
            selectedIngredientIds = selectedIngredientIds,
            categorySelections = currentCategorySelections,
            quantity = selectionQuantity,
            onQuantityChange = { newQuantity ->
                selectionQuantity = newQuantity.coerceAtLeast(1)
            },
            onToggleOptionalIngredient = { ingredientId ->
                if (selectedIngredientIds.contains(ingredientId)) {
                    selectedIngredientIds.remove(ingredientId)
                } else {
                    selectedIngredientIds.add(ingredientId)
                }
            },
            onToggleCategoryOption = { categoryId, optionId ->
                val currentSelections = selectedCategoryOptions[categoryId].orEmpty()
                selectedCategoryOptions[categoryId] = if (currentSelections.contains(optionId)) {
                    currentSelections.filterNot { it == optionId }
                } else {
                    currentSelections + listOf(optionId)
                }
            },
            onDismiss = {
                resetSelection()
            },
            onContinue = {
                if (canContinue) {
                    showSelector = false
                    showConfirmDialog = true
                }
            },
            canContinue = canContinue
        )
    }

    if (showConfirmDialog && currentProduct != null) {
        val optionalExtras = currentProduct.optionalIngredients.filter { ingredient ->
            selectedIngredientIds.contains(ingredient.id)
        }
        val categoryExtras = currentProduct.ingredientCategories.flatMap { category: IngredientCategory ->
            val selectedIds = currentCategorySelections[category.id].orEmpty()
            selectedIds.drop(category.includedCount).mapNotNull { optionId ->
                val optionName = category.options.firstOrNull { option -> option.id == optionId }?.name
                    ?: return@mapNotNull null
                Ingredient(
                    id = "${category.id}_extra_$optionId",
                    name = "${category.title} extra: $optionName",
                    extraPrice = category.extraPrice
                )
            }
        }
        val selectedIngredients = optionalExtras + categoryExtras
        ConfirmAddDialog(
            product = currentProduct,
            categorySelections = currentCategorySelections,
            ingredients = selectedIngredients,
            quantity = selectionQuantity,
            onDismiss = { resetSelection() },
            onConfirm = {
                val selectionsForCart = currentCategorySelections.mapValues { entry: Map.Entry<String, List<String>> ->
                    entry.value.toList()
                }
                if (selectionItemId == null) {
                    viewModel.addToCart(currentProduct, selectedIngredients, selectionsForCart, selectionQuantity)
                } else {
                    viewModel.updateCartItem(selectionItemId!!, selectedIngredients, selectionsForCart, selectionQuantity)
                }
                resetSelection()
            }
        )
    }

    if (showOrderDialog) {
        AlertDialog(
            onDismissRequest = { showOrderDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showOrderDialog = false
                        viewModel.clearCart()
                        navController.navigate("catalog") {
                            popUpTo("catalog") { inclusive = false }
                            launchSingleTop = true
                        }
                        pendingCatalogSnackbar = true
                    }
                ) {
                    Text(text = "Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOrderDialog = false }) {
                    Text(text = "Cancelar")
                }
            },
            title = { Text(text = "¿Desea confirmar?") },
            text = { Text(text = "Se enviará el pedido con ${viewModel.cart.sumOf { it.quantity }} ítems.") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    products: List<Product>,
    cartCount: Int,
    onProductClick: (Product) -> Unit,
    onCartClick: () -> Unit
) {
    val sushipleto = products.firstOrNull { it.id == "sushipleto" }
    val sushipletoVegetariano = products.firstOrNull { it.id == "sushipleto_vegetariano" }
    val hasCombinedSushipleto = sushipleto != null && sushipletoVegetariano != null
    val catalogItems = buildList<CatalogItem> {
        var combinedAdded = false
        products.forEach { product: Product ->
            when (product.id) {
                "sushipleto" -> {
                    if (hasCombinedSushipleto && !combinedAdded) {
                        add(
                            SushipletoCombinedItem(
                                classic = sushipleto!!,
                                vegetarian = sushipletoVegetariano!!
                            )
                        )
                        combinedAdded = true
                    } else {
                        add(SingleProductItem(product))
                    }
                }
                "sushipleto_vegetariano" -> {
                    if (!hasCombinedSushipleto) {
                        add(SingleProductItem(product))
                    }
                }
                else -> add(SingleProductItem(product))
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "ArmaTuHandroll",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                },
                actions = {
                    IconButton(onClick = onCartClick) {
                        BadgedBox(
                            badge = {
                                if (cartCount > 0) {
                                    Badge { Text(text = cartCount.toString()) }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ShoppingCart,
                                contentDescription = "Ver carrito"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                contentScale = ContentScale.Fit,
                alpha = 0.18f
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            items(
                items = catalogItems,
                key = { item ->
                    when (item) {
                        is SingleProductItem -> item.product.id
                        is SushipletoCombinedItem -> "sushipleto_combined"
                    }
                }
            ) { item ->
                when (item) {
                    is SingleProductItem -> {
                        ProductCard(
                            product = item.product,
                            onSelect = { onProductClick(item.product) }
                        )
                    }
                    is SushipletoCombinedItem -> {
                        SushipletoCombinedCard(
                            sushipleto = item.classic,
                            vegetarian = item.vegetarian,
                            onSelectSushipleto = { onProductClick(item.classic) },
                            onSelectVegetarian = { onProductClick(item.vegetarian) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SushipletoCombinedCard(
    sushipleto: Product,
    vegetarian: Product,
    onSelectSushipleto: () -> Unit,
    onSelectVegetarian: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SushipletoSection(
                product = sushipleto,
                onSelect = onSelectSushipleto
            )
            Divider()
            SushipletoSection(
                product = vegetarian,
                onSelect = onSelectVegetarian
            )
        }
    }
}

@Composable
private fun SushipletoSection(
    product: Product,
    onSelect: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = product.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Precio: ${formatPrice(product.basePrice)}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Incluye: ${product.baseIncludedDescription}",
            style = MaterialTheme.typography.bodySmall
        )
        Button(onClick = onSelect, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Elegir ingredientes")
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = "Precio: ${formatPrice(product.basePrice)}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Incluye: ${product.baseIncludedDescription}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onSelect, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Elegir ingredientes")
            }
        }
    }
}

@Composable
fun IngredientSelectorDialog(
    product: Product,
    selectedIngredientIds: List<String>,
    categorySelections: Map<String, List<String>>,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    onToggleOptionalIngredient: (String) -> Unit,
    onToggleCategoryOption: (String, String) -> Unit,
    onDismiss: () -> Unit,
    onContinue: () -> Unit,
    canContinue: Boolean
) {
    val usesFlexibleSelection = FLEXIBLE_INGREDIENT_PRODUCT_IDS.contains(product.id)
    val isSushipletoVegetariano = product.id == SUSHIPLETO_VEGETARIANO_ID
    val selectedOptionalIngredients = product.optionalIngredients.filter { ingredient ->
        selectedIngredientIds.contains(ingredient.id)
    }
    val extrasFromCategories = product.ingredientCategories.sumOf { category: IngredientCategory ->
        val selections = categorySelections[category.id].orEmpty()
        (selections.size - category.includedCount).coerceAtLeast(0) * category.extraPrice
    }
    val extrasFromOptional = selectedOptionalIngredients.sumOf { it.extraPrice }
    val totalExtrasPrice = extrasFromCategories + extrasFromOptional
    val totalSelectionsAcrossCategories = categorySelections.values.sumOf { it.size }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onContinue, enabled = canContinue) {
                Text(text = "Continuar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar")
            }
        },
        title = {
            Text(
                text = if (usesFlexibleSelection) {
                    "Elige tus ingredientes (mínimo 1)"
                } else {
                    "Selecciona ingredientes"
                }
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (usesFlexibleSelection) {
                    Text(
                        text = getFlexibleIngredientSubtitle(product.id),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "Incluye: ${product.baseIncludedDescription}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (product.ingredientCategories.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (isSushipletoVegetariano) {
                            SushipletoVegetarianoIngredientContent(
                                categories = product.ingredientCategories,
                                categorySelections = categorySelections,
                                onToggleCategoryOption = onToggleCategoryOption
                            )
                        } else {
                            product.ingredientCategories.forEach { category: IngredientCategory ->
                                val selections = categorySelections[category.id].orEmpty()
                                val extraCount = (selections.size - category.includedCount).coerceAtLeast(0)
                                val missing = (category.includedCount - selections.size).coerceAtLeast(0)
                                val includedSelected = selections.size.coerceAtMost(category.includedCount)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = category.title, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = category.description,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    if (usesFlexibleSelection) {
                                        Text(
                                            text = "${includedSelected}/${category.includedCount} incluido${if (category.includedCount != 1) "s" else ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    category.options.forEach { option: IngredientOption ->
                                        val isSelected = selections.contains(option.id)
                                        val selectionIndex = selections.indexOf(option.id)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = { onToggleCategoryOption(category.id, option.id) }
                                            )
                                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                                Text(text = option.name)
                                                if (isSelected) {
                                                    val isIncluded = selectionIndex in 0 until category.includedCount
                                                    val labelText = if (isIncluded) {
                                                        "Incluido"
                                                    } else {
                                                        "Extra +${formatPrice(category.extraPrice)}"
                                                    }
                                                    Text(
                                                        text = labelText,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = if (isIncluded) {
                                                            MaterialTheme.colorScheme.primary
                                                        } else {
                                                            MaterialTheme.colorScheme.secondary
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (!usesFlexibleSelection && missing > 0) {
                                        Text(
                                            text = "Selecciona ${missing} opción(es) más para continuar.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                    if (extraCount > 0) {
                                        Text(
                                            text = "Extras seleccionados: ${formatPrice(extraCount * category.extraPrice)}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                if (usesFlexibleSelection && totalSelectionsAcrossCategories < 1) {
                    Text(
                        text = "Selecciona al menos 1 ingrediente para continuar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (!usesFlexibleSelection && product.optionalIngredients.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val showHeading = product.ingredientCategories.isNotEmpty()
                        if (showHeading) {
                            Text(text = "Agrega extras opcionales", fontWeight = FontWeight.SemiBold)
                        }
                        product.optionalIngredients.forEach { ingredient: Ingredient ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = selectedIngredientIds.contains(ingredient.id),
                                    onCheckedChange = { onToggleOptionalIngredient(ingredient.id) }
                                )
                                Column(modifier = Modifier.padding(start = 8.dp)) {
                                    Text(text = ingredient.name)
                                    Text(
                                        text = "Extra: ${formatPrice(ingredient.extraPrice)}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
                if (usesFlexibleSelection) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Precio base: ${formatPrice(product.basePrice)}")
                        Text(text = "Extras: ${formatPrice(totalExtrasPrice)}")
                        Text(
                            text = "Subtotal: ${formatPrice(product.basePrice + totalExtrasPrice)}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Cantidad", fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onQuantityChange((quantity - 1).coerceAtLeast(1)) }) {
                            Icon(imageVector = Icons.Filled.Remove, contentDescription = "Disminuir")
                        }
                        Text(text = quantity.toString(), modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { onQuantityChange(quantity + 1) }) {
                            Icon(imageVector = Icons.Filled.Add, contentDescription = "Aumentar")
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun SushipletoVegetarianoIngredientContent(
    categories: List<com.example.apphandroll.model.IngredientCategory>,
    categorySelections: Map<String, List<String>>,
    onToggleCategoryOption: (String, String) -> Unit
) {
    val baseCategory = categories.firstOrNull { it.id == SUSHIPLETO_VEGETARIANO_BASE_CATEGORY_ID }
    val creamyCategory = categories.firstOrNull { it.id == SUSHIPLETO_VEGETARIANO_CREAMY_CATEGORY_ID }
    val vegetalCategory = categories.firstOrNull { it.id == SUSHIPLETO_VEGETARIANO_VEGETAL_CATEGORY_ID }

    baseCategory?.let { category: IngredientCategory ->
        val selections = categorySelections[category.id].orEmpty()
        val includedSelected = selections.size.coerceAtMost(category.includedCount)
        val extraCount = (selections.size - category.includedCount).coerceAtLeast(0)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "Base", fontWeight = FontWeight.SemiBold)
            Text(text = category.description, style = MaterialTheme.typography.bodySmall)
            Text(
                text = "${includedSelected}/${category.includedCount} incluida${if (category.includedCount != 1) "s" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            category.options.forEach { option: IngredientOption ->
                val isSelected = selections.contains(option.id)
                val selectionIndex = selections.indexOf(option.id)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleCategoryOption(category.id, option.id) }
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(text = option.name)
                        if (isSelected) {
                            val isIncluded = selectionIndex in 0 until category.includedCount
                            val labelText = if (isIncluded) {
                                "Incluido"
                            } else {
                                "Extra +${formatPrice(category.extraPrice)}"
                            }
                            Text(
                                text = labelText,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isIncluded) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                }
                            )
                        }
                    }
                }
            }
            if (extraCount > 0) {
                Text(
                    text = "Base extra: ${formatPrice(extraCount * category.extraPrice)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    if (baseCategory != null && (creamyCategory != null || vegetalCategory != null)) {
        Spacer(modifier = Modifier.height(12.dp))
    }

    if (creamyCategory != null || vegetalCategory != null) {
        val creamySelections = creamyCategory?.let { categorySelections[it.id].orEmpty() } ?: emptyList()
        val vegetalSelections = vegetalCategory?.let { categorySelections[it.id].orEmpty() } ?: emptyList()
        val extraItems = buildList {
            creamyCategory?.let { category: IngredientCategory ->
                addAll(category.options.map { option: IngredientOption ->
                    Triple(category.id, option, category.extraPrice)
                })
            }
            vegetalCategory?.let { category: IngredientCategory ->
                addAll(category.options.map { option: IngredientOption ->
                    Triple(category.id, option, category.extraPrice)
                })
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "A elección", fontWeight = FontWeight.SemiBold)
            Text(
                text = "Queso/Palta extra +${formatPrice(creamyCategory?.extraPrice ?: 1000)}. Cebollín/Ciboulette extra +${formatPrice(vegetalCategory?.extraPrice ?: 500)}.",
                style = MaterialTheme.typography.bodySmall
            )
            val totalSelected = creamySelections.size + vegetalSelections.size
            Text(
                text = "Seleccionados: $totalSelected",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            extraItems.forEach { triple: Triple<String, IngredientOption, Int> ->
                val (categoryId, option, price) = triple
                val selectionsForCategory = categorySelections[categoryId].orEmpty()
                val isSelected = selectionsForCategory.contains(option.id)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleCategoryOption(categoryId, option.id) }
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(text = option.name)
                        Text(
                            text = "Extra +${formatPrice(price)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
            if (creamyCategory != null && creamySelections.isNotEmpty()) {
                Text(
                    text = "Cremoso extra: ${formatPrice(creamySelections.size * creamyCategory.extraPrice)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (vegetalCategory != null && vegetalSelections.isNotEmpty()) {
                Text(
                    text = "Vegetal extra: ${formatPrice(vegetalSelections.size * vegetalCategory.extraPrice)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ConfirmAddDialog(
    product: Product,
    categorySelections: Map<String, List<String>>,
    ingredients: List<Ingredient>,
    quantity: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val unitPrice = product.basePrice + ingredients.sumOf { it.extraPrice }
    val total = unitPrice * quantity
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar")
            }
        },
        title = { Text(text = "¿Está seguro?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = product.name, fontWeight = FontWeight.Bold)
                if (product.ingredientCategories.isNotEmpty()) {
                    product.ingredientCategories.forEach { category: IngredientCategory ->
                        val selectedIds = categorySelections[category.id].orEmpty()
                        if (selectedIds.isNotEmpty()) {
                            val selectedNames = category.options.filter { option ->
                                selectedIds.contains(option.id)
                            }.map { it.name }
                            Text(
                                text = "${category.title}: ${selectedNames.joinToString()}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                if (ingredients.isEmpty()) {
                    Text(text = "Sin ingredientes adicionales")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ingredients.forEach { ingredient: Ingredient ->
                            Text(text = "• ${ingredient.name} (${formatPrice(ingredient.extraPrice)})")
                        }
                    }
                }
                Text(text = "Cantidad: $quantity")
                Text(text = "Subtotal: ${formatPrice(total)}", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartItems: List<CartItem>,
    total: Int,
    onBack: () -> Unit,
    onEditItem: (CartItem) -> Unit,
    onRemoveItem: (String) -> Unit,
    onConfirmOrder: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Carrito") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (cartItems.isEmpty()) {
                Text(text = "Carrito vacío", style = MaterialTheme.typography.bodyLarge)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cartItems, key = { it.id }) { item: CartItem ->
                        CartItemRow(
                            item = item,
                            onEdit = { onEditItem(item) },
                            onDelete = { onRemoveItem(item.id) }
                        )
                    }
                }
            }

            if (cartItems.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Total: ${formatPrice(total)}", fontWeight = FontWeight.Bold)
                    Button(
                        onClick = onConfirmOrder,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = cartItems.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors()
                    ) {
                        Text(text = "Confirmar pedido")
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = item.product.name, fontWeight = FontWeight.Bold)
                Text(text = "x${item.quantity}")
            }
            if (item.product.ingredientCategories.isNotEmpty()) {
                item.product.ingredientCategories.forEach { category: IngredientCategory ->
                    val selectedIds = item.selectedCategoryOptions[category.id].orEmpty()
                    if (selectedIds.isNotEmpty()) {
                        val selectedNames = category.options.filter { option ->
                            selectedIds.contains(option.id)
                        }.map { it.name }
                        Text(
                            text = "${category.title}: ${selectedNames.joinToString()}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            if (item.selectedIngredients.isNotEmpty()) {
                Text(
                    text = "Extras: ${item.selectedIngredients.joinToString { it.name }}",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text(text = "Sin ingredientes adicionales", style = MaterialTheme.typography.bodySmall)
            }
            Text(text = "Precio unitario: ${formatPrice(item.unitPrice)}")
            Text(text = "Subtotal: ${formatPrice(item.totalPrice)}", fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Editar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Eliminar")
                }
            }
        }
    }
}
