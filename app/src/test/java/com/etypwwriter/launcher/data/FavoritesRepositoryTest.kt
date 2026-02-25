package com.etypwwriter.launcher.data

import android.content.Context
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File

@RunWith(RobolectricTestRunner::class)
class FavoritesRepositoryTest {

    private lateinit var context: Context
    private lateinit var repository: FavoritesRepository

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        // Clean up DataStore files between test runs if needed
        File(context.filesDir, "datastore").deleteRecursively()
        repository = FavoritesRepository(context)
    }

    @Test
    fun `save and get favorites`() = runTest {
        val favorites = setOf("app1", "app2")
        repository.saveFavorites(favorites)
        
        val result = repository.favoritesFlow.first()
        assertEquals(favorites, result)
    }

    @Test
    fun `save and get hidden apps`() = runTest {
        val hidden = setOf("hidden1", "hidden2")
        repository.saveHiddenApps(hidden)
        
        val result = repository.hiddenAppsFlow.first()
        assertEquals(hidden, result)
    }

    @Test
    fun `save and get custom names`() = runTest {
        repository.saveCustomName("com.example.app1", "My Custom App")
        
        val result = repository.customNamesFlow.first()
        assertEquals("My Custom App", result["com.example.app1"])

        // Test removing name
        repository.saveCustomName("com.example.app1", "")
        val resultAfterRemove = repository.customNamesFlow.first()
        assertEquals(null, resultAfterRemove["com.example.app1"])
    }

    @Test
    fun `save and get folders`() = runTest {
        val folderData = mapOf("Games" to setOf("app1", "app2"))
        repository.saveFolders(folderData)
        
        val result = repository.foldersFlow.first()
        assertEquals(setOf("app1", "app2"), result["Games"])
    }
}
