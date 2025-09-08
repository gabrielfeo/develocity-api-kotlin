package com.gabrielfeo.develocity.api.internal

data class FakeSystemProperties(
    override var userHome: String? = System.getProperty("java.io.tmpdir"),
) : SystemProperties
