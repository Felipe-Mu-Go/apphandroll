package com.example.apphandroll

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PriceFormatterTest {
    @Test
    fun `formats values using Chilean thousands separator`() {
        val formatted = formatPrice(3_500)

        assertEquals("$3.500", formatted)
    }

    @Test
    fun `throws when formatting negative values`() {
        assertThrows(IllegalArgumentException::class.java) {
            formatPrice(-1)
        }
    }
}
