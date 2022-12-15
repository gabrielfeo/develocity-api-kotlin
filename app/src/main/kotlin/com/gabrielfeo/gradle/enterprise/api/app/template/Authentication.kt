package com.gabrielfeo.gradle.enterprise.api.app.template

fun getTokenFromEnv(varName: String): String {
    return checkNotNull(System.getenv(varName)) { "No such var '$varName'" }
}

fun getTokenFromKeychain(keyName: String): String {
    val process = ProcessBuilder(
        "security",
        "find-generic-password",
        "-w",
        "-a",
        System.getenv("LOGNAME"),
        "-s",
        keyName
    ).start()
    val status = process.waitFor()
    check(status == 0) { "Failed to get key from keychain (exit $status)" }
    return process.inputStream.bufferedReader().use {
        it.readText().trim()
    }
}
