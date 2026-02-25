package com.etypwwriter.launcher.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.etypwwriter.launcher.utils.AppItem
import com.etypwwriter.launcher.utils.getInstalledApps

@Composable
fun AppPickerScreen(
    currentFavorites: Set<String>,
    onSave: (Set<String>) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var installedApps by remember { mutableStateOf<List<AppItem>>(emptyList()) }
    var selectedApps by remember { mutableStateOf(currentFavorites) }

    LaunchedEffect(Unit) {
        val apps = getInstalledApps(context)
        installedApps = apps
        val validPackages = apps.map { it.packageName }.toSet()
        selectedApps = selectedApps.intersect(validPackages)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Seleccionar favoritas (${selectedApps.size}/8)",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Row {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = Color.White)
                }
                IconButton(onClick = { onSave(selectedApps) }) {
                    Icon(Icons.Default.Check, contentDescription = "Guardar", tint = Color.Green)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(installedApps) { app ->
                val isSelected = selectedApps.contains(app.packageName)
                AppSelectionRow(
                    app = app,
                    isSelected = isSelected,
                    onToggle = { 
                        if (isSelected) {
                            selectedApps = selectedApps - app.packageName
                        } else if (selectedApps.size < 8) {
                            selectedApps = selectedApps + app.packageName
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AppSelectionRow(app: AppItem, isSelected: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = app.label,
            color = Color.White,
            fontSize = 18.sp
        )
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = Color.White,
                uncheckedColor = Color.Gray,
                checkmarkColor = Color.Black
            )
        )
    }
}
