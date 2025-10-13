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
import java.time.LocalTime
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
private val scheduleDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(
    "dd/MM/yyyy HH:mm",
    spanishChileLocale
)
private val scheduleTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(
    "HH:mm",
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
    _generatedAt: LocalDateTime = LocalDateTime.now()
): String {
    require(data.cartItems.isNotEmpty()) { "Cart must not be empty" }

    val customerName = data.customerInfo?.customerName?.trim()?.takeUnless { it.isEmpty() }
    val customerPhone = data.customerInfo?.phone?.trim()?.takeUnless { it.isEmpty() }
    val scheduleText = data.schedule?.trim()?.takeUnless { it.isEmpty() }?.let { raw ->
        val normalized = raw.trim()
        val parsedDateTime = runCatching { LocalDateTime.parse(normalized) }
            .getOrNull()
            ?: runCatching {
                LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            }.getOrNull()
        when {
            parsedDateTime != null -> parsedDateTime.format(scheduleDateTimeFormatter)
            else -> {
                val parsedTime = runCatching { LocalTime.parse(normalized) }.getOrNull()
                    ?: runCatching {
                        LocalTime.parse(normalized, DateTimeFormatter.ofPattern("HH:mm"))
                    }.getOrNull()
                parsedTime?.format(scheduleTimeFormatter) ?: normalized
            }
        }
    }

    val notesText = data.notes?.trim()?.takeUnless { it.isEmpty() }

    val subtotalDecimal = data.cartItems.fold(BigDecimal.ZERO) { acc, item ->
        acc + item.totalPrice.toBigDecimal()
    }
    val shippingDecimal = data.shippingCost.toBigDecimal()
    val discountDecimal = data.discount.toBigDecimal()
    val totalDecimal = subtotalDecimal + shippingDecimal - discountDecimal
    val totalNonNegative = if (totalDecimal < BigDecimal.ZERO) BigDecimal.ZERO else totalDecimal

    val totalText = formatPrice(totalNonNegative.toIntExactSafe())

    val builder = StringBuilder()
    builder.appendLine("🍣 **Pedido ${data.businessName}**")
    builder.appendLine()

    val contactLines = buildList {
        customerName?.let { add("👤 **Cliente:** $it") }
        customerPhone?.let { add("📞 **Teléfono:** $it") }
        scheduleText?.let { add("🕒 **Hora de Retiro:** $it") }
        notesText?.let { add("📝 **Notas:** $it") }
    }
    contactLines.forEach { builder.appendLine(it) }
    if (contactLines.isNotEmpty()) {
        builder.appendLine()
    }

    builder.appendLine("🧾 **Detalle de pedido:**")
    data.cartItems.forEach { item ->
        builder.appendLine(
            "- x${item.quantity} ${item.product.name} — ${formatPrice(item.unitPrice)} c/u → ${formatPrice(item.totalPrice)}"
        )
    }

    builder.appendLine()
    builder.append("💵 **Total:** $totalText")

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
