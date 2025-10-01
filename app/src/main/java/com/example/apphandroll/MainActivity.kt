package com.example.apphandroll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.weight
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
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.apphandroll.model.CartItem
import com.example.apphandroll.model.Ingredient
import com.example.apphandroll.model.Product

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
    var selectionQuantity by remember { mutableStateOf(1) }
    var showSelector by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showOrderDialog by remember { mutableStateOf(false) }
    var pendingCatalogSnackbar by remember { mutableStateOf(false) }

    fun resetSelection() {
        selectionProduct = null
        selectionItemId = null
        selectedIngredientIds.clear()
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
    if (showSelector && currentProduct != null) {
        IngredientSelectorDialog(
            product = currentProduct,
            selectedIngredientIds = selectedIngredientIds,
            quantity = selectionQuantity,
            onQuantityChange = { newQuantity ->
                selectionQuantity = newQuantity.coerceAtLeast(1)
            },
            onToggleIngredient = { ingredientId ->
                if (selectedIngredientIds.contains(ingredientId)) {
                    selectedIngredientIds.remove(ingredientId)
                } else {
                    selectedIngredientIds.add(ingredientId)
                }
            },
            onDismiss = {
                resetSelection()
            },
            onContinue = {
                showSelector = false
                showConfirmDialog = true
            }
        )
    }

    if (showConfirmDialog && currentProduct != null) {
        val selectedIngredients = currentProduct.optionalIngredients.filter { selectedIngredientIds.contains(it.id) }
        ConfirmAddDialog(
            product = currentProduct,
            ingredients = selectedIngredients,
            quantity = selectionQuantity,
            onDismiss = { resetSelection() },
            onConfirm = {
                if (selectionItemId == null) {
                    viewModel.addToCart(currentProduct, selectedIngredients, selectionQuantity)
                } else {
                    viewModel.updateCartItem(selectionItemId!!, selectedIngredients, selectionQuantity)
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Catálogo") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(products) { product ->
                ProductCard(
                    product = product,
                    onSelect = { onProductClick(product) }
                )
            }
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
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    onToggleIngredient: (String) -> Unit,
    onDismiss: () -> Unit,
    onContinue: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onContinue) {
                Text(text = "Continuar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar")
            }
        },
        title = { Text(text = "Selecciona ingredientes") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Incluye: ${product.baseIncludedDescription}", style = MaterialTheme.typography.bodyMedium)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    product.optionalIngredients.forEach { ingredient ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = selectedIngredientIds.contains(ingredient.id),
                                onCheckedChange = { onToggleIngredient(ingredient.id) }
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
fun ConfirmAddDialog(
    product: Product,
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
                if (ingredients.isEmpty()) {
                    Text(text = "Sin ingredientes adicionales")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ingredients.forEach { ingredient ->
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
                    items(cartItems, key = { it.id }) { item ->
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
