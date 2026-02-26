package com.etypwwriter.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.etypwwriter.launcher.utils.AppItem
import com.etypwwriter.launcher.utils.getInstalledApps

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults

import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.Add

import androidx.compose.foundation.lazy.grid.GridItemSpan

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AppDrawerScreen(
    customNames: Map<String, String>,
    folders: Map<String, Set<String>>,
    hiddenApps: Set<String>,
    windowWidthSizeClass: WindowWidthSizeClass,
    onClose: () -> Unit,
    onAppSelected: (String) -> Unit,
    onAppLongPressed: (String) -> Unit,
    onSaveFolder: (String, Set<String>) -> Unit,
    onDeleteFolder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var installedApps by remember { mutableStateOf<List<AppItem>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    
    var showFolderEditDialog by remember { mutableStateOf(false) }
    var folderToEdit by remember { mutableStateOf<String?>(null) }
    var expandedFolders by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showHiddenApps by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        installedApps = getInstalledApps(context)
    }
    
    val appsInFolders = remember(folders) { folders.values.flatten().toSet() }

    val filteredApps = if (searchQuery.isBlank()) {
        val baseApps = if (showHiddenApps) installedApps else installedApps.filter { !hiddenApps.contains(it.packageName) }
        baseApps.filter { !appsInFolders.contains(it.packageName) }
    } else {
        val baseApps = if (showHiddenApps) installedApps else installedApps.filter { !hiddenApps.contains(it.packageName) }
        baseApps.filter { 
            val label = customNames[it.packageName] ?: it.label
            label.contains(searchQuery, ignoreCase = true) 
        }
    }

    if (showFolderEditDialog) {
        val initialName = folderToEdit ?: ""
        val initialApps = folderToEdit?.let { folders[it] } ?: emptySet()
        FolderEditDialog(
            initialName = initialName,
            initialApps = initialApps,
            installedApps = installedApps,
            customNames = customNames,
            onDismiss = { showFolderEditDialog = false },
            onSave = { name, apps ->
                val oldName = folderToEdit
                if (apps.isEmpty()) {
                    if (oldName != null) onDeleteFolder(oldName)
                } else {
                    if (oldName != null && oldName != name) {
                        onDeleteFolder(oldName)
                    }
                    onSaveFolder(name, apps)
                }
                showFolderEditDialog = false
            }
        )
    }

    if (showAboutDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text("Acerca de e-typewriter", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Versión 1.0", color = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Este proyecto es Software Libre y está licenciado bajo la GNU General Public License v3.0 (GPLv3).",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Código fuente disponible en GitHub.",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showAboutDialog = false }) {
                    Text("Cerrar", color = Color.White)
                }
            },
            containerColor = Color.DarkGray
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(top = 40.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
            }
            Text(
                text = "Todas las Apps",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { 
                folderToEdit = null
                showFolderEditDialog = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Carpeta", tint = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar...", color = Color.Gray) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.DarkGray,
                unfocusedContainerColor = Color.DarkGray,
                cursorColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        val columns = if (windowWidthSizeClass == WindowWidthSizeClass.Compact) {
            GridCells.Fixed(1)
        } else {
            GridCells.Fixed(2)
        }

        LazyVerticalGrid(columns = columns) {
            // Folders
            if (searchQuery.isBlank()) {
                folders.entries.sortedBy { it.key.lowercase() }.forEach { (folderName, apps) ->
                    item {
                        val isExpanded = expandedFolders.contains(folderName)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        expandedFolders = if (isExpanded) expandedFolders - folderName else expandedFolders + folderName
                                    },
                                    onLongClick = {
                                        folderToEdit = folderName
                                        showFolderEditDialog = true
                                    }
                                )
                                .padding(vertical = 12.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📁 $folderName",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (expandedFolders.contains(folderName)) {
                        val folderApps = installedApps.filter { apps.contains(it.packageName) }.sortedBy { (customNames[it.packageName] ?: it.label).lowercase() }
                        items(folderApps) { app ->
                            Text(
                                text = "  ↳ ${customNames[app.packageName] ?: app.label}",
                                color = Color.LightGray,
                                fontSize = 18.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { onAppSelected(app.packageName) },
                                        onLongClick = { onAppLongPressed(app.packageName) }
                                    )
                                    .padding(vertical = 8.dp, horizontal = 32.dp)
                            )
                        }
                    }
                }
            }

            items(filteredApps) { app ->
                Text(
                    text = customNames[app.packageName] ?: app.label,
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onAppSelected(app.packageName) },
                            onLongClick = { onAppLongPressed(app.packageName) }
                        )
                        .padding(vertical = 12.dp, horizontal = 12.dp)
                )
            }

            if (searchQuery.isBlank() && hiddenApps.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    androidx.compose.material3.Button(
                        onClick = { showHiddenApps = !showHiddenApps },
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Text(if (showHiddenApps) "Ocultar apps" else "Ver más...", color = Color.White)
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAboutDialog = true }
                        .padding(vertical = 16.dp, horizontal = 16.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    Text("Acerca de / Licencia...", color = Color.Gray, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
