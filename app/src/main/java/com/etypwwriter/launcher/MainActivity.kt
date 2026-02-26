package com.etypwwriter.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
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
import com.etypwwriter.launcher.ui.theme.ETypewriterLauncherTheme
import kotlinx.coroutines.delay
import androidx.activity.viewModels
import com.etypwwriter.launcher.data.FavoritesRepository
import com.etypwwriter.launcher.ui.HomeViewModel
import com.etypwwriter.launcher.ui.HomeViewModelFactory
import android.widget.Toast
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.pm.PackageManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.etypwwriter.launcher.ui.AppPickerScreen
import com.etypwwriter.launcher.ui.AppDrawerScreen
import com.etypwwriter.launcher.ui.AppOptionsDialog
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.etypwwriter.launcher.ui.SingleAppPickerScreen

import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.Coil
import coil.compose.AsyncImage
import com.etypwwriter.launcher.ui.icons.IconPackManager

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

import com.etypwwriter.launcher.utils.uninstallApp

class MainActivity : ComponentActivity() {

    private val favoritesRepository by lazy { FavoritesRepository(applicationContext) }
    private val homeViewModel: HomeViewModel by viewModels { HomeViewModelFactory(favoritesRepository) }

    private val roleRequestLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "e-typewriter configurado como launcher por defecto", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No se configuró como launcher por defecto", Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize Coil globally to support SVGs
        val imageLoader = ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
            }
            .logger(coil.util.DebugLogger())
            .build()
        Coil.setImageLoader(imageLoader)

        setContent {
            ETypewriterLauncherTheme {
                val windowSizeClass = calculateWindowSizeClass(this)
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Black
                ) { innerPadding ->
                    LauncherScreen(
                        homeViewModel = homeViewModel,
                        windowWidthSizeClass = windowSizeClass.widthSizeClass,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkAndRequestDefaultLauncher()
    }

    private fun checkAndRequestDefaultLauncher() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME) && !roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                roleRequestLauncher.launch(intent)
            }
        } else {
            // For older Android versions, we could show a dialog directing them to settings,
            // but for simplicity and since minimum SDK is 26, we'll try launching the settings directly.
            val intent = Intent(Settings.ACTION_HOME_SETTINGS)
            try {
                startActivity(intent)
            } catch (e: Exception) {
                // Ignore if the intent is not resolvable
            }
        }
    }
}

@Composable
fun LauncherScreen(
    homeViewModel: HomeViewModel,
    windowWidthSizeClass: WindowWidthSizeClass,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }
    val favoriteApps by homeViewModel.favoriteApps.collectAsStateWithLifecycle()
    val customNames by homeViewModel.customNames.collectAsStateWithLifecycle()
    val folders by homeViewModel.folders.collectAsStateWithLifecycle()
    val hiddenApps by homeViewModel.hiddenApps.collectAsStateWithLifecycle()
    val bottomShortcuts by homeViewModel.bottomShortcuts.collectAsStateWithLifecycle()
    var appPickerSlot by remember { mutableStateOf<Int?>(null) }
    var isEditingFavorites by remember { mutableStateOf(false) }
    var isDrawerOpen by remember { mutableStateOf(false) }
    var appToRename by remember { mutableStateOf<String?>(null) }

    val iconPackManager = remember { IconPackManager(context) }
    LaunchedEffect(Unit) {
        iconPackManager.init()
        while (true) {
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }

    appToRename?.let { packageName ->
        AppOptionsDialog(
            initialName = getAppName(context, packageName, customNames) ?: "",
            isHidden = hiddenApps.contains(packageName),
            onDismiss = { appToRename = null },
            onSave = { newName ->
                homeViewModel.saveCustomName(packageName, newName)
                appToRename = null
            },
            onToggleHide = {
                homeViewModel.toggleHiddenApp(packageName)
                appToRename = null
            },
            onUninstall = {
                uninstallApp(context, packageName)
                appToRename = null
                isDrawerOpen = false
            }
        )
    }

    if (isEditingFavorites) {
        AppPickerScreen(
            currentFavorites = favoriteApps,
            onSave = { newFavorites ->
                homeViewModel.saveFavorites(newFavorites)
                isEditingFavorites = false
            },
            onCancel = { isEditingFavorites = false },
            modifier = modifier
        )
    } else if (isDrawerOpen) {
        AppDrawerScreen(
            customNames = customNames,
            folders = folders,
            hiddenApps = hiddenApps,
            windowWidthSizeClass = windowWidthSizeClass,
            iconPackManager = iconPackManager,
            onClose = { isDrawerOpen = false },
            onAppSelected = { packageName ->
                launchApp(context, packageName)
                isDrawerOpen = false
            },
            onAppLongPressed = { packageName ->
                appToRename = packageName
            },
            onSaveFolder = { name, apps ->
                homeViewModel.saveFolder(name, apps)
            },
            onDeleteFolder = { name ->
                homeViewModel.deleteFolder(name)
            },
            modifier = modifier
        )
    } else if (appPickerSlot != null) {
        SingleAppPickerScreen(
            title = "Elegir aplicación",
            onAppSelected = { packageName ->
                homeViewModel.saveBottomShortcut(appPickerSlot!!, packageName)
                appPickerSlot = null
            },
            onCancel = { appPickerSlot = null },
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount < -20) { // Swipe up
                            isDrawerOpen = true
                        }
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp, vertical = 60.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
            // Reloj
            Text(
                text = currentTime,
                color = Color.White,
                fontSize = 80.sp,
                fontWeight = FontWeight.Light
            )

            Spacer(modifier = Modifier.height(40.dp))
            
            HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
            
            Spacer(modifier = Modifier.height(20.dp))

            // Botón editar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { isEditingFavorites = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar Favoritos",
                        tint = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))

            // Accesos directos dinámicos
            favoriteApps.forEach { packageName ->
                val appName = getAppName(context, packageName, customNames)
                if (appName != null) {
                    AppShortcut(
                        packageName = packageName,
                        name = appName,
                        iconPackManager = iconPackManager,
                        onClick = { launchApp(context, packageName) },
                        onLongClick = { appToRename = packageName }
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0..2) {
                BottomShortcutItem(
                    packageName = bottomShortcuts[i],
                    index = i,
                    iconPackManager = iconPackManager,
                    onClick = { pkg ->
                        if (pkg != null) {
                            val launchResult = launchApp(context, pkg)
                            if (launchResult == null) {
                                appPickerSlot = i
                            }
                        } else {
                            appPickerSlot = i
                        }
                    },
                    onLongClick = {
                        appPickerSlot = i
                    }
                )
            }
        }
    }
}
}


@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AppShortcut(
    packageName: String,
    name: String,
    iconPackManager: IconPackManager,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    var iconBytes by remember(packageName) { mutableStateOf<ByteArray?>(null) }

    LaunchedEffect(packageName) {
        val bytes = iconPackManager.getIconBytes(packageName)
        if (bytes != null) {
            iconBytes = bytes
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconBytes != null) {
            AsyncImage(
                model = iconBytes,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp)
            )
        }
        Text(
            text = name,
            color = Color.Gray,
            fontSize = 20.sp,
        )
    }
}

fun getAppName(context: Context, packageName: String, customNames: Map<String, String> = emptyMap()): String? {
    if (customNames.containsKey(packageName)) {
        return customNames[packageName]
    }
    val pm = context.packageManager
    return try {
        val applicationInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        pm.getApplicationLabel(applicationInfo).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        // App might have been uninstalled
        null
    }
}

fun launchApp(context: android.content.Context, packageName: String): Unit? {
    val pm = context.packageManager
    return try {
        val intent = pm.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            context.startActivity(intent)
            Unit
        } else {
            Toast.makeText(context, "App no encontrada", Toast.LENGTH_SHORT).show()
            null
        }
    } catch (e: Exception) {
        null
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun BottomShortcutItem(
    packageName: String?,
    index: Int,
    iconPackManager: IconPackManager,
    onClick: (String?) -> Unit,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current
    var iconBytes by remember(packageName) { mutableStateOf<ByteArray?>(null) }
    var androidIconBitmap by remember(packageName) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    
    LaunchedEffect(packageName) {
        android.util.Log.d("IconPackManager", "LaunchedEffect triggered for bottom shortcut: $packageName")
        if (packageName != null) {
            // Priority 1: Arcticons
            val bytes = iconPackManager.getIconBytes(packageName)
            if (bytes != null) {
                iconBytes = bytes
            } else {
                iconBytes = null
                // Priority 2: System App Icon fallback
                try {
                    val pm = context.packageManager
                    val drawable = pm.getApplicationIcon(packageName)
                    androidIconBitmap = drawable.toBitmap().asImageBitmap()
                } catch (e: Exception) {
                    androidIconBitmap = null
                }
            }
        } else {
            iconBytes = null
            androidIconBitmap = null
        }
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .combinedClickable(
                onClick = { onClick(packageName) },
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (iconBytes != null) {
            AsyncImage(
                model = iconBytes,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        } else if (androidIconBitmap != null) {
            Image(
                bitmap = androidIconBitmap!!,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val emoji = when (index) {
                0 -> "📞"
                1 -> "💬"
                2 -> "📷"
                else -> "❓"
            }
            Text(
                text = emoji,
                fontSize = 32.sp
            )
        }
    }
}

