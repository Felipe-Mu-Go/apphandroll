package com.example.apphandroll

import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class NoBinaryFilesInRepoTest {

    @Test
    fun `el repositorio no contiene archivos binarios no admitidos`() {
        val repoRoot = locateRepositoryRoot()
        val binaryFiles = mutableListOf<String>()

        Files.walk(repoRoot).use { paths ->
            paths.filter { Files.isRegularFile(it) && !shouldIgnore(it, repoRoot) }
                .forEach { path ->
                    if (BinaryFileDetector.containsBinaryData(path)) {
                        binaryFiles += repoRoot.relativize(path).toString()
                    }
                }
        }

        assertTrue(
            "Se detectaron archivos binarios en el repositorio: ${binaryFiles.joinToString()}",
            binaryFiles.isEmpty()
        )
    }

    private fun locateRepositoryRoot(): Path {
        var current = Paths.get("").toAbsolutePath()

        while (true) {
            if (Files.exists(current.resolve(".git"))) {
                return current
            }

            current = current.parent ?: break
        }

        error("No se encontró el directorio .git partiendo desde ${Paths.get("").toAbsolutePath()}")
    }

    private fun shouldIgnore(path: Path, repoRoot: Path): Boolean {
        val relative = repoRoot.relativize(path).toString().replace('\\', '/')
        if (relative.isEmpty()) return true

        val segments = relative.split('/')
        if (segments.any { it == ".git" || it == "build" || it == ".gradle" || it == ".idea" }) {
            return true
        }

        if (relative == "gradle/wrapper/gradle-wrapper.jar") {
            return true
        }

        return false
    }
}
