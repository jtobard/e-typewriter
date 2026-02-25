package com.etypwwriter.launcher.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppItem(
    val packageName: String,
    val label: String
)

suspend fun getInstalledApps(context: Context): List<AppItem> = withContext(Dispatchers.IO) {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    
    val resolveInfoList: List<ResolveInfo> = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
    
    resolveInfoList.mapNotNull {
        val packageName = it.activityInfo.packageName
        if (packageName == context.packageName) return@mapNotNull null // Exclude the launcher itself
        AppItem(
            packageName = packageName,
            label = it.loadLabel(pm).toString()
        )
    }.sortedBy { it.label.lowercase() }
}
