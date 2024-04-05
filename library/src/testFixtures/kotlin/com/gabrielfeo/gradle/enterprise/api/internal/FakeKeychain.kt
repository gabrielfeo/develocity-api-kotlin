package com.gabrielfeo.develocity.api.internal

internal class FakeKeychain(
    vararg entries: Pair<String, String>,
) : Keychain {

    private val entries = entries.toMap()

    override fun get(entry: String) =
        entries[entry]?.let { KeychainResult.Success(it) }
            ?: KeychainResult.Error("entry $entry not mocked")
}
