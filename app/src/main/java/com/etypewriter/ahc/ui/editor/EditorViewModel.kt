package com.etypewriter.ahc.ui.editor

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.etypewriter.ahc.data.FileManager

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    var text by mutableStateOf("")
        private set

    var fileName by mutableStateOf("untitled.md")
        private set

    var currentUri: Uri? by mutableStateOf(null)
        private set

    var hasUnsavedChanges by mutableStateOf(false)
        private set

    fun onTextChange(newText: String) {
        text = newText
        hasUnsavedChanges = true
    }

    fun openFile(uri: Uri) {
        val ctx = getApplication<Application>()
        val content = FileManager.readFile(ctx, uri)
        if (content != null) {
            text = content
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
        text = ""
        fileName = "untitled.md"
        currentUri = null
        hasUnsavedChanges = false
    }
}
