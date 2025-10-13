package com.example.apphandroll.whatsapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.example.apphandroll.formatPrice
import com.example.apphandroll.model.CartItem
import com.example.apphandroll.model.CustomerInfo
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// NUEVO: envío por WhatsApp - constantes para los paquetes soportados.
const val WHATSAPP_STANDARD_PACKAGE = "com.whatsapp"
const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"
private const val PLAY_STORE_BASE_URL = "https://play.google.com/store/apps/details?id="
private const val WHATSAPP_TARGET_NUMBER = "931327744"

/**
 * NUEVO: envío por WhatsApp - variantes soportadas por la app.
 */
enum class WhatsAppVariant(val packageName: String) {
    STANDARD(WHATSAPP_STANDARD_PACKAGE),
    BUSINESS(WHATSAPP_BUSINESS_PACKAGE)
}

/**
 * NUEVO: envío por WhatsApp - datos necesarios para construir el mensaje.
 */
data class WhatsAppOrderMessageData(
    val businessName: String,
    val cartItems: List<CartItem>,
    val customerInfo: CustomerInfo?,
    val deliveryMethod: String?,
    val address: String?,
    val schedule: String?,
    val notes: String?,
    val shippingCost: Int = 0,
    val discount: Int = 0,
    val targetPhoneNumber: String = WHATSAPP_TARGET_NUMBER
)

private val spanishChileLocale = Locale("es", "CL")
private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(
    "EEEE d 'de' MMMM yyyy 'a las' HH:mm",
    spanishChileLocale
)

/**
 * NUEVO: envío por WhatsApp - construye el mensaje listo para ser enviado.
 *
 * Nota: Si el minSdk fuera menor a 26, se podría usar ThreeTenABP o java.text.SimpleDateFormat
 * para reemplazar la obtención de fecha y hora.
 */
fun buildWhatsAppOrderMessage(
    data: WhatsAppOrderMessageData,
    generatedAt: LocalDateTime = LocalDateTime.now()
): String {
    require(data.cartItems.isNotEmpty()) { "Cart must not be empty" }

    val customerName = data.customerInfo?.customerName?.ifBlank { "Sin nombre" } ?: "Sin nombre"
    val customerPhone = data.customerInfo?.phone?.ifBlank { "Sin teléfono" } ?: "Sin teléfono"
    val customerEmail = data.customerInfo?.email?.takeUnless { it.isNullOrBlank() } ?: "No informado"
    val deliveryMethod = data.deliveryMethod?.ifBlank { "Por confirmar" } ?: "Por confirmar"
    val address = data.address?.ifBlank { "No aplica" } ?: "No aplica"
    val schedule = data.schedule?.ifBlank { dateFormatter.format(generatedAt) }
        ?: dateFormatter.format(generatedAt)
    val notes = data.notes?.ifBlank { "Sin notas" } ?: "Sin notas"

    val subtotalDecimal = data.cartItems.fold(BigDecimal.ZERO) { acc, item ->
        acc + item.totalPrice.toBigDecimal()
    }
    val shippingDecimal = data.shippingCost.toBigDecimal()
    val discountDecimal = data.discount.toBigDecimal()
    val totalDecimal = subtotalDecimal + shippingDecimal - discountDecimal
    val totalNonNegative = if (totalDecimal < BigDecimal.ZERO) BigDecimal.ZERO else totalDecimal

    val subtotalText = formatPrice(subtotalDecimal.toIntExactSafe())
    val shippingText = formatPrice(shippingDecimal.toIntExactSafe())
    val discountText = formatPrice(discountDecimal.toIntExactSafe())
    val totalText = formatPrice(totalNonNegative.toIntExactSafe())

    val builder = StringBuilder()
    builder.appendLine("🍣 Pedido ${data.businessName}")
    builder.appendLine()
    builder.appendLine("👤 Cliente: $customerName")
    builder.appendLine("📞 Teléfono: $customerPhone")
    builder.appendLine("📧 Correo: $customerEmail")
    builder.appendLine("🚚 Método de entrega: $deliveryMethod")
    builder.appendLine("🏠 Dirección: $address")
    builder.appendLine("🕒 Fecha/Horario: $schedule")
    builder.appendLine("📝 Notas: $notes")
    builder.appendLine()
    builder.appendLine("🛒 Detalle del pedido:")

    data.cartItems.forEachIndexed { index, item ->
        val position = index + 1
        builder.appendLine(
            "$position. x${item.quantity} ${item.product.name} - ${formatPrice(item.unitPrice)} c/u = ${formatPrice(item.totalPrice)}"
        )

        val categoryLines = item.product.ingredientCategories.mapNotNull { category ->
            val selectedIds = item.selectedCategoryOptions[category.id].orEmpty()
            if (selectedIds.isEmpty()) {
                null
            } else {
                val selectedNames = category.options.filter { option ->
                    selectedIds.contains(option.id)
                }.joinToString()
                "   • ${category.title}: $selectedNames"
            }
        }
        categoryLines.forEach { builder.appendLine(it) }

        val extras = if (item.selectedIngredients.isEmpty()) {
            "Sin adicionales"
        } else {
            item.selectedIngredients.joinToString { it.name }
        }
        builder.appendLine("   • Extras: $extras")
        builder.appendLine()
    }

    builder.appendLine("📊 Totales:")
    builder.appendLine("Subtotal: $subtotalText")
    builder.appendLine("Envío: $shippingText")
    builder.appendLine("Descuento: $discountText")
    builder.appendLine("TOTAL: $totalText")
    builder.appendLine()
    builder.append("¡Muchas gracias por tu pedido! 🙌")

    return builder.toString().trim()
}

private fun BigDecimal.toIntExactSafe(): Int {
    return try {
        this.setScale(0, RoundingMode.HALF_UP).intValueExact()
    } catch (error: ArithmeticException) {
        this.setScale(0, RoundingMode.HALF_UP).toInt()
    }
}

/**
 * NUEVO: envío por WhatsApp - devuelve la variante instalada, priorizando la versión estándar.
 */
fun resolveInstalledWhatsAppVariant(context: Context): WhatsAppVariant? {
    val packageManager = context.packageManager
    return listOf(WhatsAppVariant.STANDARD, WhatsAppVariant.BUSINESS).firstOrNull { variant ->
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    variant.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(variant.packageName, 0)
            }
            true
        } catch (notFound: PackageManager.NameNotFoundException) {
            false
        }
    }
}

/**
 * NUEVO: envío por WhatsApp - crea el intent para abrir la conversación.
 */
fun createWhatsAppIntent(
    variant: WhatsAppVariant,
    phoneNumber: String,
    message: String
): Intent {
    val digitsOnly = phoneNumber.filter { it.isDigit() }
    val phoneParam = if (digitsOnly.isNotEmpty()) {
        digitsOnly
    } else {
        phoneNumber.replace(" ", "")
    }
    val encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
    val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phoneParam&text=$encodedMessage")
    return Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage(variant.packageName)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}

/**
 * NUEVO: envío por WhatsApp - crea el intent para abrir la ficha en Play Store.
 */
fun createPlayStoreIntent(variant: WhatsAppVariant): Intent {
    val uri = Uri.parse("market://details?id=${variant.packageName}")
    return Intent(Intent.ACTION_VIEW, uri)
}

/**
 * NUEVO: envío por WhatsApp - crea un intent web como respaldo si Play Store no está disponible.
 */
fun createPlayStoreWebIntent(variant: WhatsAppVariant): Intent {
    val uri = Uri.parse("$PLAY_STORE_BASE_URL${variant.packageName}")
    return Intent(Intent.ACTION_VIEW, uri)
}

/**
 * NUEVO: envío por WhatsApp - obtiene el número destino configurado.
 */
fun getWhatsAppTargetNumber(): String = WHATSAPP_TARGET_NUMBER
