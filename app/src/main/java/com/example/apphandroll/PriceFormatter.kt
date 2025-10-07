package com.example.apphandroll

import java.text.NumberFormat
import java.util.Locale

private val chileanLocale = Locale("es", "CL")

private val priceFormatter: ThreadLocal<NumberFormat> = ThreadLocal.withInitial {
    NumberFormat.getIntegerInstance(chileanLocale).apply {
        isGroupingUsed = true
        maximumFractionDigits = 0
    }
}

fun formatPrice(value: Int): String {
    require(value >= 0) { "Price cannot be negative" }
    val formatter = priceFormatter.get()
    return "${'$'}${formatter.format(value)}"
}
