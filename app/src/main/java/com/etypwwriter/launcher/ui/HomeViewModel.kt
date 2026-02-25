package com.etypwwriter.launcher.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.etypwwriter.launcher.data.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: FavoritesRepository) : ViewModel() {

    private val _favoriteApps = MutableStateFlow<Set<String>>(emptySet())
    val favoriteApps: StateFlow<Set<String>> = _favoriteApps.asStateFlow()

    private val _customNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val customNames: StateFlow<Map<String, String>> = _customNames.asStateFlow()

    private val _folders = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val folders: StateFlow<Map<String, Set<String>>> = _folders.asStateFlow()

    private val _hiddenApps = MutableStateFlow<Set<String>>(emptySet())
    val hiddenApps: StateFlow<Set<String>> = _hiddenApps.asStateFlow()

    init {
        viewModelScope.launch {
            repository.favoritesFlow.collect { favorites ->
                // If it's the first run, let's put some defaults
                if (favorites.isEmpty()) {
                    val defaults = setOf(
                        "com.android.dialer", // This might not exist, but we handle it in launchApp
                        "com.whatsapp",
                        "com.google.android.deskclock"
                    )
                    repository.saveFavorites(defaults)
                    _favoriteApps.value = defaults
                } else {
                    _favoriteApps.value = favorites
                }
            }
        }
        viewModelScope.launch {
            repository.customNamesFlow.collect { names ->
                _customNames.value = names
            }
        }
        viewModelScope.launch {
            repository.foldersFlow.collect { f ->
                _folders.value = f
            }
        }
        viewModelScope.launch {
            repository.hiddenAppsFlow.collect { h ->
                _hiddenApps.value = h
            }
        }
    }

    fun addFavorite(packageName: String) {
        if (_favoriteApps.value.size < 8) {
            val newFavorites = _favoriteApps.value.toMutableSet().apply { add(packageName) }
            viewModelScope.launch { repository.saveFavorites(newFavorites) }
        }
    }

    fun removeFavorite(packageName: String) {
        val newFavorites = _favoriteApps.value.toMutableSet().apply { remove(packageName) }
        viewModelScope.launch { repository.saveFavorites(newFavorites) }
    }

    fun saveFavorites(packages: Set<String>) {
        if (packages.size <= 8) {
            viewModelScope.launch { repository.saveFavorites(packages) }
        }
    }

    fun saveCustomName(packageName: String, customName: String) {
        viewModelScope.launch { repository.saveCustomName(packageName, customName) }
    }

    fun saveFolder(folderName: String, packages: Set<String>) {
        val currentFolders = _folders.value.toMutableMap()
        currentFolders[folderName] = packages
        viewModelScope.launch { repository.saveFolders(currentFolders) }
    }

    fun deleteFolder(folderName: String) {
        val currentFolders = _folders.value.toMutableMap()
        currentFolders.remove(folderName)
        viewModelScope.launch { repository.saveFolders(currentFolders) }
    }

    fun toggleHiddenApp(packageName: String) {
        val newHiddenApps = _hiddenApps.value.toMutableSet()
        if (newHiddenApps.contains(packageName)) {
            newHiddenApps.remove(packageName)
        } else {
            newHiddenApps.add(packageName)
        }
        viewModelScope.launch { repository.saveHiddenApps(newHiddenApps) }
    }
}

class HomeViewModelFactory(private val repository: FavoritesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
