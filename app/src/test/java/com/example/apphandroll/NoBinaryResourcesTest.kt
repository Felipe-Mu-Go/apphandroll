package com.example.apphandroll

import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files
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
                    if (BinaryFileDetector.containsBinaryData(path)) {
                        binaryFiles += resourcesDir.relativize(path).toString()
                    }
                }
        }

        assertTrue(
            "Se detectaron archivos binarios en src/main/res: ${binaryFiles.joinToString()}",
            binaryFiles.isEmpty()
        )
    }
}
