package com.example.apphandroll

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.BufferedInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class NoBinaryResourcesTest {
    @Test
    fun `resources directory contains only text files`() {
        val resourcesDir = Paths.get("src/main/res")
        require(Files.exists(resourcesDir)) { "No se encontró el directorio de recursos: $resourcesDir" }

        val binaryFiles = mutableListOf<String>()
        Files.walk(resourcesDir).use { paths ->
            paths.filter { Files.isRegularFile(it) }
                .forEach { path ->
                    if (containsBinaryData(path)) {
                        binaryFiles += resourcesDir.relativize(path).toString()
                    }
                }
        }

        assertTrue(
            "Se detectaron archivos binarios en src/main/res: ${binaryFiles.joinToString()}",
            binaryFiles.isEmpty()
        )
    }

    private fun containsBinaryData(path: Path): Boolean {
        BufferedInputStream(Files.newInputStream(path)).use { input ->
            val buffer = ByteArray(4096)
            while (true) {
                val read = input.read(buffer)
                if (read == -1) return false
                for (i in 0 until read) {
                    if (buffer[i] == 0.toByte()) {
                        return true
                    }
                }
            }
        }
    }
}
