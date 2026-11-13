package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.ApiKeyManager
import com.example.ui.MainViewModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StartupErrorBannerTest {

    private lateinit var application: Application
    private lateinit var apiKeyManager: ApiKeyManager

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        apiKeyManager = ApiKeyManager(application)
        apiKeyManager.clearApiKey()
    }

    private fun awaitStartupLoaded(viewModel: MainViewModel) {
        val deadline = System.currentTimeMillis() + 5_000L
        while (!viewModel.uiState.value.isStartupSettingsLoaded && System.currentTimeMillis() < deadline) {
            Thread.sleep(20)
        }
    }

    @Test
    fun `hasApiKey is seeded synchronously when key exists in SharedPreferences`() {
        apiKeyManager.saveGeminiApiKey("AIzaSyTestKeyForStartupSeed123")
        val viewModel = MainViewModel(application)

        // Immediately upon construction (frame 0), hasApiKey must already be true
        assertTrue(viewModel.uiState.value.hasApiKey)

        awaitStartupLoaded(viewModel)
        assertTrue(viewModel.uiState.value.isStartupSettingsLoaded)
        assertTrue(viewModel.uiState.value.hasApiKey)
    }

    @Test
    fun `banner remains suppressed before startup settings complete and surfaces if truly missing`() {
        val viewModel = MainViewModel(application)

        awaitStartupLoaded(viewModel)
        assertTrue(viewModel.uiState.value.isStartupSettingsLoaded)
        assertFalse(viewModel.uiState.value.hasApiKey)
    }

    @Test
    fun `notebook custom key suppresses warning banner once startup finishes without flashing`() {
        val repo = com.example.data.notebook.NotebookRepository(application)
        val active = repo.getActiveNotebook()
        repo.saveNotebookSettings(
            active.id,
            active.settings.copy(customApiKey = "AIzaSyNotebookOnlyKey999")
        )

        val viewModel = MainViewModel(application)
        // Before background load finishes, banner is gated by isStartupSettingsLoaded == false
        val initialState = viewModel.uiState.value
        assertFalse(!initialState.hasApiKey && initialState.isStartupSettingsLoaded)

        awaitStartupLoaded(viewModel)
        assertTrue(viewModel.uiState.value.isStartupSettingsLoaded)
        assertTrue(viewModel.uiState.value.hasApiKey)
    }
}
