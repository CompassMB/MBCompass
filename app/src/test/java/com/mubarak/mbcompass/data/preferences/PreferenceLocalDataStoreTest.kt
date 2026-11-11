package com.mubarak.mbcompass.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.mubarak.mbcompass.ui.theme.ThemeConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class PreferenceLocalDataStoreTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var testScope: TestScope
    private lateinit var testFile: File
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var dataSource: PreferenceLocalDataStore

    @Before
    fun setUp() {
        testFile = File(tempFolder.root, "test.preferences_pb")
        testScope = TestScope(testDispatcher)
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )
        dataSource = PreferenceLocalDataStore(dataStore)
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
    fun trueNorth_true_roundTrip() = testScope.runTest {
        dataSource.setTrueNorthValue(UserPreferences.TRUE_NORTH, true)

        val prefs = dataSource.preferenceFlow.first()
        assertEquals(true, prefs.isTrueNorthEnabled)
    }

    @Test
    fun trueNorth_false_roundTrip() = testScope.runTest {
        dataSource.setTrueNorthValue(UserPreferences.TRUE_NORTH, false)

        val prefs = dataSource.preferenceFlow.first()
        assertEquals(false, prefs.isTrueNorthEnabled)
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

