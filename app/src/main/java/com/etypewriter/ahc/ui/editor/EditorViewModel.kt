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
        val textChanged = newValue.text != textFieldValue.text
        if (textChanged) {
            textFieldValue = newValue
            hasUnsavedChanges = true
        } else if (newValue.selection != textFieldValue.selection) {
            val cursorPos = newValue.selection.start
            val lineEnd = findEndOfLine(newValue.text, cursorPos)
            textFieldValue = newValue.copy(selection = TextRange(lineEnd))
        }
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
