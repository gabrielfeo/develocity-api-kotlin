package com.gabrielfeo.gradle.enterprise.api.internal

class FakeKeychain(
    vararg entries: Pair<String, String>,
) : Keychain {

    private val entries = entries.toMap()

    override fun get(entry: String) = entries[entry]
}
