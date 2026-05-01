package com.mubarak.mbcompass.data.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.mubarak.mbcompass.ui.theme.ThemeConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class PreferenceLocalDataStoreTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val dataStore by lazy {
        PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = {
                tempFolder.newFile("test.preferences_pb")
            }
        )
    }

    private val dataSource by lazy {
        PreferenceLocalDataStore(dataStore)
    }

    @Test
    fun defaultValues_areCorrect() = testScope.runTest {
        val prefs = dataSource.preferenceFlow.first()

        assertEquals(ThemeConfig.FOLLOW_SYSTEM.prefName, prefs.theme)
        assertEquals(false, prefs.isTrueDarkThemeEnabled)
        assertEquals(false, prefs.isTrueNorthEnabled)
        assertEquals(false, prefs.highAccuracy)
        assertEquals(48.8583, prefs.lastLatitude, 0.0)
        assertEquals(2.2944, prefs.lastLongitude, 0.0)
        assertEquals(16.0, prefs.lastZoomLevel, 0.0)
    }

    @Test
    fun theme_roundTrip() = testScope.runTest {
        dataSource.setTheme(UserPreferences.KEY_THEME, "dark")

        val prefs = dataSource.preferenceFlow.first()

        assertEquals("dark", prefs.theme)
    }

    @Test
    fun trueNorth_roundTrip() = testScope.runTest {
        dataSource.setTrueNorthValue(UserPreferences.TRUE_NORTH, true)

        val prefs = dataSource.preferenceFlow.first()
        assertEquals(true, prefs.isTrueNorthEnabled)

        dataSource.setTrueNorthValue(UserPreferences.TRUE_NORTH, false)

        val updated = dataSource.preferenceFlow.first()
        assertEquals(false, updated.isTrueNorthEnabled)
    }

    @Test
    fun trueDark_roundTrip() = testScope.runTest {
        dataSource.setTrueDarkValue(UserPreferences.TRUE_DARK, true)

        val prefs = dataSource.preferenceFlow.first()
        assertEquals(true, prefs.isTrueDarkThemeEnabled)
    }

    @Test
    fun highAccuracy_roundTrip() = testScope.runTest {
        dataSource.setHighAccuracy(true)

        val prefs = dataSource.preferenceFlow.first()
        assertEquals(true, prefs.highAccuracy)
    }

    @Test
    fun mapState_roundTrip() = testScope.runTest {
        dataSource.saveMapState(10.0, 20.0, 12.5)

        val prefs = dataSource.preferenceFlow.first()

        assertEquals(10.0, prefs.lastLatitude, 0.0)
        assertEquals(20.0, prefs.lastLongitude, 0.0)
        assertEquals(12.5, prefs.lastZoomLevel, 0.0)
    }
}

