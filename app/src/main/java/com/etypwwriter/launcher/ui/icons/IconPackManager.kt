package com.etypwwriter.launcher.ui.icons

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile

class IconPackManager(private val context: Context) {

    private val packageMap = mutableMapOf<String, String>()
    private var isInitialized = false
    private var zipFile: java.util.zip.ZipFile? = null
    private var cacheZip: File? = null

    suspend fun init() {
        if (isInitialized) return
        withContext(Dispatchers.IO) {
            android.util.Log.d("IconPackManager", "Starting IconPackManager init")
            cacheZip = File(context.cacheDir, "arcticons.zip")
            
            val extractFromAssets = {
                val tempFile = File(context.cacheDir, "arcticons.zip.tmp")
                context.assets.open("arcticons.zip").use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                if (cacheZip!!.exists()) cacheZip!!.delete()
                tempFile.renameTo(cacheZip)
                android.util.Log.d("IconPackManager", "Extracted zip to cache")
            }

            if (!cacheZip!!.exists()) {
                extractFromAssets()
            }

            try {
                zipFile = java.util.zip.ZipFile(cacheZip)
            } catch (e: Exception) {
                // Si está corrupto, re-extraemos
                android.util.Log.e("IconPackManager", "Error reading zip: ${e.message}, retrying extraction")
                extractFromAssets()
                zipFile = java.util.zip.ZipFile(cacheZip)
            }
            
            val entry = zipFile?.getEntry("appfilter.xml")
            if (entry != null) {
                zipFile?.getInputStream(entry)?.use { inputStream ->
                    parseAppFilter(inputStream)
                }
            } else {
                android.util.Log.e("IconPackManager", "appfilter.xml not found in zip!")
            }
            
            isInitialized = true
            android.util.Log.d("IconPackManager", "IconPackManager initialized successfully")
        }
    }

    private fun parseAppFilter(inputStream: java.io.InputStream) {
        try {
            val factory = org.xmlpull.v1.XmlPullParserFactory.newInstance()
            // Some XML files might have namespaces or formatting issues, making them not completely valid
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(inputStream, "UTF-8")
            
            var eventType = parser.eventType
            while (eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                if (eventType == org.xmlpull.v1.XmlPullParser.START_TAG && parser.name == "item") {
                    val component = parser.getAttributeValue(null, "component")
                    val drawable = parser.getAttributeValue(null, "drawable")
                    
                    if (component != null && drawable != null) {
                        if (component.startsWith("ComponentInfo{")) {
                            val slashIndex = component.indexOf('/')
                            if (slashIndex != -1) {
                                val packageName = component.substring(14, slashIndex)
                                if (!packageMap.containsKey(packageName)) {
                                    packageMap[packageName] = drawable
                                }
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            android.util.Log.e("IconPackManager", "Error parsing xml", e)
        }
    }

    suspend fun getIconBytes(packageName: String): ByteArray? {
        if (!isInitialized) init()
        
        return withContext(Dispatchers.IO) {
            val drawableName = packageMap[packageName]
            if (drawableName == null) {
                android.util.Log.d("IconPackManager", "No mapped icon for $packageName")
                return@withContext null
            }

            // Clean the path in case there are other subdirectories
            val cleanDrawableName = drawableName.substringAfterLast("/")
            val svgEntry = zipFile?.getEntry("white/$cleanDrawableName.svg")
            
            if (svgEntry != null) {
                val bytes = zipFile?.getInputStream(svgEntry)?.use { it.readBytes() }
                android.util.Log.d("IconPackManager", "Loaded icon for $packageName: ${bytes?.size} bytes")
                bytes
            } else {
                android.util.Log.d("IconPackManager", "SVG file not found for $packageName at white/$cleanDrawableName.svg")
                null
            }
        }
    }
}
