package com.mubarak.mbcompass.data.preferences

import com.mubarak.mbcompass.ui.theme.ThemeConfig
import junit.framework.TestCase.assertEquals
import org.junit.Assert
import org.junit.Test

class ThemeConfigTest {

    @Test
    fun followSystemMapsToCrtPreferenceValue() {
        Assert.assertEquals("default", ThemeConfig.FOLLOW_SYSTEM.prefName)
    }

    @Test
    fun lightMapsToCrtPreferenceValue() {
        Assert.assertEquals("light", ThemeConfig.LIGHT.prefName)
    }

    @Test
    fun darkMapsToCrtPreferenceValue() {
        Assert.assertEquals("dark", ThemeConfig.DARK.prefName)
    }

    @Test
    fun fromReturnsFollowSystem() {
        assertEquals(ThemeConfig.FOLLOW_SYSTEM, ThemeConfig.fromPref("system"))
    }

    @Test
    fun fromReturnsLight() {
        assertEquals(ThemeConfig.LIGHT, ThemeConfig.fromPref("light"))
    }

    @Test
    fun fromReturnsDark() {
        assertEquals(ThemeConfig.DARK, ThemeConfig.fromPref("dark"))
    }

    @Test
    fun fromFallBackToFollowSystemForUnknownValue() {
        assertEquals(ThemeConfig.FOLLOW_SYSTEM, ThemeConfig.fromPref("unknown"))
    }

    @Test
    fun fromFallBackToFollowSystemForNull() {
        assertEquals(ThemeConfig.FOLLOW_SYSTEM, ThemeConfig.fromPref(null))
    }

    @Test
    fun fromFallBackToFollowSystemForEmptyValue() {
        assertEquals(ThemeConfig.FOLLOW_SYSTEM, ThemeConfig.fromPref(""))
    }
}