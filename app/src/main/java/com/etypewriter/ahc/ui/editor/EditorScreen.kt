package com.etypewriter.ahc.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onNewFile: () -> Unit,
    onOpenFile: () -> Unit,
    onSaveFile: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        TopBar(
            fileName = viewModel.fileName,
            hasUnsavedChanges = viewModel.hasUnsavedChanges,
            onNew = onNewFile,
            onOpen = onOpenFile,
            onSave = onSaveFile,
            typography = typography.titleMedium,
        )

        HorizontalDivider(color = colors.outline.copy(alpha = 0.3f), thickness = 0.5.dp)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            if (viewModel.text.isEmpty()) {
                Text(
                    text = "Start typing...",
                    style = typography.bodyLarge.copy(
                        color = colors.outline.copy(alpha = 0.5f)
                    ),
                )
            }

            BasicTextField(
                value = viewModel.text,
                onValueChange = viewModel::onTextChange,
                modifier = Modifier.fillMaxSize(),
                textStyle = typography.bodyLarge.copy(color = colors.onBackground),
                cursorBrush = SolidColor(colors.onBackground),
            )
        }

        HorizontalDivider(color = colors.outline.copy(alpha = 0.3f), thickness = 0.5.dp)

        StatusBar(
            text = viewModel.text,
            typography = typography.labelSmall,
        )
    }
}

@Composable
private fun TopBar(
    fileName: String,
    hasUnsavedChanges: Boolean,
    onNew: () -> Unit,
    onOpen: () -> Unit,
    onSave: () -> Unit,
    typography: TextStyle,
) {
    val colors = MaterialTheme.colorScheme
    val displayName = if (hasUnsavedChanges) "$fileName *" else fileName

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = displayName,
            style = typography,
            modifier = Modifier.weight(1f).padding(start = 8.dp),
        )

        IconButton(onClick = onNew) {
            Icon(
                imageVector = Icons.Outlined.CreateNewFolder,
                contentDescription = "New file",
                tint = colors.onBackground,
            )
        }
        IconButton(onClick = onOpen) {
            Icon(
                imageVector = Icons.Outlined.FolderOpen,
                contentDescription = "Open file",
                tint = colors.onBackground,
            )
        }
        IconButton(onClick = onSave) {
            Icon(
                imageVector = Icons.Outlined.Save,
                contentDescription = "Save file",
                tint = colors.onBackground,
            )
        }
    }
}

@Composable
private fun StatusBar(
    text: String,
    typography: TextStyle,
) {
    val wordCount = if (text.isBlank()) 0 else text.trim().split("\\s+".toRegex()).size
    val charCount = text.length
    val lineCount = if (text.isEmpty()) 1 else text.lines().size

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "$lineCount lines", style = typography)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = "$wordCount words", style = typography)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = "$charCount chars", style = typography)
    }
}
