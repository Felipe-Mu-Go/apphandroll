package com.example.apphandroll

import java.io.BufferedInputStream
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

object BinaryFileDetector {
    private const val BUFFER_SIZE = 4096

    fun containsBinaryData(path: Path): Boolean {
        BufferedInputStream(Files.newInputStream(path)).use { input ->
            val decoder = StandardCharsets.UTF_8.newDecoder().apply {
                onMalformedInput(CodingErrorAction.REPORT)
                onUnmappableCharacter(CodingErrorAction.REPORT)
            }
            val byteBuffer = ByteBuffer.allocate(BUFFER_SIZE)
            val charBuffer = CharBuffer.allocate(BUFFER_SIZE)
            val buffer = ByteArray(BUFFER_SIZE)

            while (true) {
                val read = input.read(buffer)
                if (read == -1) break

                for (index in 0 until read) {
                    if (buffer[index] == 0.toByte()) {
                        return true
                    }
                }

                byteBuffer.clear()
                byteBuffer.put(buffer, 0, read)
                byteBuffer.flip()

                try {
                    decoder.decode(byteBuffer, charBuffer, false)
                } catch (error: CharacterCodingException) {
                    return true
                } finally {
                    charBuffer.clear()
                }
            }

            return try {
                decoder.flush(charBuffer)
                false
            } catch (error: CharacterCodingException) {
                true
            }
        }
    }
}
