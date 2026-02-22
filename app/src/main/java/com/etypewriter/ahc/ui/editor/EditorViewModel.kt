package com.etypewriter.ahc.ui.editor

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import com.etypewriter.ahc.data.FileManager

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    var textFieldValue by mutableStateOf(TextFieldValue(""))
        private set

    val text: String get() = textFieldValue.text

    var fileName by mutableStateOf("untitled.md")
        private set

    var currentUri: Uri? by mutableStateOf(null)
        private set

    var hasUnsavedChanges by mutableStateOf(false)
        private set

    fun onTextFieldValueChange(newValue: TextFieldValue) {
        val wrapped = wrapLinesToMaxChars(newValue.text, CHARS_PER_LINE)
        val textChanged = wrapped != textFieldValue.text
        if (textChanged) {
            val newCursor = wrappedCursorFromOriginal(newValue.text, newValue.selection.start, CHARS_PER_LINE)
            textFieldValue = TextFieldValue(wrapped, TextRange(newCursor.coerceIn(0, wrapped.length)))
            hasUnsavedChanges = true
        } else if (newValue.selection != textFieldValue.selection) {
            val cursorPos = newValue.selection.start
            val lineEnd = findEndOfLine(newValue.text, cursorPos)
            textFieldValue = newValue.copy(selection = TextRange(lineEnd))
        }
    }

    private fun wrapLinesToMaxChars(text: String, maxPerLine: Int): String {
        if (maxPerLine <= 0) return text
        return text.split("\n").flatMap { line ->
            line.chunked(maxPerLine)
        }.joinToString("\n")
    }

    /** Cursor position in wrapped text corresponding to original position in unwrapped text. */
    private fun wrappedCursorFromOriginal(text: String, originalCursor: Int, maxPerLine: Int): Int {
        if (maxPerLine <= 0) return originalCursor
        val prefix = text.take(originalCursor)
        val wrappedPrefix = prefix.split("\n").flatMap { it.chunked(maxPerLine) }.joinToString("\n")
        return wrappedPrefix.length
    }

    companion object {
        const val CHARS_PER_LINE = 70
    }

    private fun findEndOfLine(text: String, position: Int): Int {
        val nextNewline = text.indexOf('\n', position)
        return if (nextNewline == -1) text.length else nextNewline
    }

    fun openFile(uri: Uri) {
        val ctx = getApplication<Application>()
        val content = FileManager.readFile(ctx, uri)
        if (content != null) {
            textFieldValue = TextFieldValue(content, TextRange(content.length))
            fileName = FileManager.fileNameFromUri(ctx, uri)
            currentUri = uri
            hasUnsavedChanges = false
        }
    }

    fun saveFile(uri: Uri) {
        val ctx = getApplication<Application>()
        val success = FileManager.writeFile(ctx, uri, text)
        if (success) {
            currentUri = uri
            fileName = FileManager.fileNameFromUri(ctx, uri)
            hasUnsavedChanges = false
        }
    }

    fun newFile() {
        textFieldValue = TextFieldValue("")
        fileName = "untitled.md"
        currentUri = null
        hasUnsavedChanges = false
    }
}
