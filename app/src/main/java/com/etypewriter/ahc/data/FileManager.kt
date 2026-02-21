package com.etypewriter.ahc.data

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader

object FileManager {

    fun readFile(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream)).use { reader ->
                    reader.readText()
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    fun writeFile(context: Context, uri: Uri, content: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri, "wt")?.use { stream ->
                stream.write(content.toByteArray())
                stream.flush()
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun fileNameFromUri(context: Context, uri: Uri): String {
        var name = "untitled.md"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) {
                name = cursor.getString(idx) ?: name
            }
        }
        return name
    }
}
