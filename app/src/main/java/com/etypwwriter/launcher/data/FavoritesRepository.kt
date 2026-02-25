package com.etypwwriter.launcher.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import org.json.JSONObject
import androidx.datastore.preferences.core.stringPreferencesKey

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "launcher_prefs")

class FavoritesRepository(private val context: Context) {

    private val favoritesKey = stringSetPreferencesKey("favorite_apps")
    private val customNamesKey = stringPreferencesKey("custom_app_names")
    private val hiddenAppsKey = stringSetPreferencesKey("hidden_apps")

    val favoritesFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[favoritesKey] ?: emptySet()
    }

    suspend fun saveFavorites(favorites: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[favoritesKey] = favorites
        }
    }

    val hiddenAppsFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[hiddenAppsKey] ?: emptySet()
    }

    suspend fun saveHiddenApps(hiddenApps: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[hiddenAppsKey] = hiddenApps
        }
    }

    val customNamesFlow: Flow<Map<String, String>> = context.dataStore.data.map { preferences ->
        val jsonString = preferences[customNamesKey] ?: "{}"
        try {
            val jsonObject = JSONObject(jsonString)
            val map = mutableMapOf<String, String>()
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = jsonObject.getString(key)
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun saveCustomName(packageName: String, customName: String) {
        context.dataStore.edit { preferences ->
            val jsonString = preferences[customNamesKey] ?: "{}"
            val jsonObject = try { JSONObject(jsonString) } catch (e: Exception) { JSONObject() }
            if (customName.isBlank()) {
                jsonObject.remove(packageName)
            } else {
                jsonObject.put(packageName, customName)
            }
            preferences[customNamesKey] = jsonObject.toString()
        }
    }

    private val foldersKey = stringPreferencesKey("app_folders")

    val foldersFlow: Flow<Map<String, Set<String>>> = context.dataStore.data.map { preferences ->
        val jsonString = preferences[foldersKey] ?: "{}"
        try {
            val jsonObject = JSONObject(jsonString)
            val map = mutableMapOf<String, Set<String>>()
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val jsonArray = jsonObject.getJSONArray(key)
                val set = mutableSetOf<String>()
                for (i in 0 until jsonArray.length()) {
                    set.add(jsonArray.getString(i))
                }
                map[key] = set
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun saveFolders(folders: Map<String, Set<String>>) {
        context.dataStore.edit { preferences ->
            val jsonObject = JSONObject()
            folders.forEach { (folderName, apps) ->
                val jsonArray = org.json.JSONArray()
                apps.forEach { jsonArray.put(it) }
                jsonObject.put(folderName, jsonArray)
            }
            preferences[foldersKey] = jsonObject.toString()
        }
    }
}
