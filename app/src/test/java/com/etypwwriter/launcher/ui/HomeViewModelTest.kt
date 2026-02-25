package com.etypwwriter.launcher.ui

import com.etypwwriter.launcher.data.FavoritesRepository
import com.etypwwriter.launcher.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initialization loads defaults when repository is empty`() = runTest {
        val repository = mockk<FavoritesRepository>(relaxed = true)
        
        coEvery { repository.favoritesFlow } returns MutableStateFlow(emptySet())
        coEvery { repository.customNamesFlow } returns MutableStateFlow(emptyMap())
        coEvery { repository.foldersFlow } returns MutableStateFlow(emptyMap())
        coEvery { repository.hiddenAppsFlow } returns MutableStateFlow(emptySet())

        val viewModel = HomeViewModel(repository)

        val favorites = viewModel.favoriteApps.value
        assertEquals(3, favorites.size)
        assert(favorites.contains("com.whatsapp"))
        assert(favorites.contains("com.google.android.deskclock"))
        
        coVerify(exactly = 1) { repository.saveFavorites(any()) }
    }

    @Test
    fun `addFavorite adds new package and saves`() = runTest {
        val repository = mockk<FavoritesRepository>(relaxed = true)
        val favoritesFlow = MutableStateFlow(setOf("app1"))
        
        coEvery { repository.favoritesFlow } returns favoritesFlow
        coEvery { repository.customNamesFlow } returns MutableStateFlow(emptyMap())
        coEvery { repository.foldersFlow } returns MutableStateFlow(emptyMap())
        coEvery { repository.hiddenAppsFlow } returns MutableStateFlow(emptySet())

        coEvery { repository.saveFavorites(any()) } answers {
            favoritesFlow.value = firstArg()
        }

        val viewModel = HomeViewModel(repository)

        viewModel.addFavorite("app2")

        coVerify { repository.saveFavorites(setOf("app1", "app2")) }
        assertEquals(setOf("app1", "app2"), viewModel.favoriteApps.value)
    }

    @Test
    fun `removeFavorite removes package and saves`() = runTest {
        val repository = mockk<FavoritesRepository>(relaxed = true)
        val favoritesFlow = MutableStateFlow(setOf("app1", "app2"))
        
        coEvery { repository.favoritesFlow } returns favoritesFlow
        coEvery { repository.customNamesFlow } returns MutableStateFlow(emptyMap())
        coEvery { repository.foldersFlow } returns MutableStateFlow(emptyMap())
        coEvery { repository.hiddenAppsFlow } returns MutableStateFlow(emptySet())

        coEvery { repository.saveFavorites(any()) } answers {
            favoritesFlow.value = firstArg()
        }

        val viewModel = HomeViewModel(repository)

        viewModel.removeFavorite("app1")

        coVerify { repository.saveFavorites(setOf("app2")) }
        assertEquals(setOf("app2"), viewModel.favoriteApps.value)
    }
}
