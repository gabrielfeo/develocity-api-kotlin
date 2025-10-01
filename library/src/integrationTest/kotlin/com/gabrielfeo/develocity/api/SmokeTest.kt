package com.gabrielfeo.develocity.api

import com.gabrielfeo.develocity.api.internal.RealEnv
import com.gabrielfeo.develocity.api.internal.asMap
import kotlin.test.Test
import kotlin.test.assertTrue

class SmokeTest {

    @Test
    fun testSuiteEnvironmentIsEmpty() {
        val vars = RealEnv.asMap().toMutableMap().entries.apply {
            // Added by either Gradle or JUnit that but irrelevant to library functionality
            removeAll { (k, _) -> k.endsWith("CF_USER_TEXT_ENCODING") }
            removeAll { (k, _) -> k.startsWith("JAVA_MAIN_CLASS") }
        }
        assertTrue(vars.isEmpty(), "Expected empty environment, found $vars")
    }
}
