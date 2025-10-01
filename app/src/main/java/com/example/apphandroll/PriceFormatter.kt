package com.example.apphandroll

import java.text.NumberFormat
import java.util.Locale

fun formatPrice(value: Int): String {
    val formatter = NumberFormat.getIntegerInstance(Locale.getDefault())
    return "${'$'}${formatter.format(value)}"
}
