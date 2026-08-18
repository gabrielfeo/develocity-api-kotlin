package com.gabrielfeo.develocity.api.internal.auth

import com.gabrielfeo.develocity.api.internal.Env
import com.gabrielfeo.develocity.api.internal.env
import com.gabrielfeo.develocity.api.internal.systemProperties
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

internal var accessKeyResolver = AccessKeyResolver(
    env,
    homeDirectory = checkNotNull(systemProperties.userHome).toPath(),
    fileSystem = FileSystem.SYSTEM,
)

internal class AccessKeyResolver(
    private val env: Env,
    private val homeDirectory: Path,
    private val fileSystem: FileSystem,
) {

    private val gradleUserHome: Path
        get() = env["GRADLE_USER_HOME"]?.toPath() ?: (homeDirectory / ".gradle")

    fun resolve(host: String): String? {
        val keyEntry = fromEnvVar("DEVELOCITY_ACCESS_KEY", host)
            ?: fromEnvVar("GRADLE_ENTERPRISE_ACCESS_KEY", host)
            ?: fromFile(gradleUserHome / "develocity/keys.properties", host)
            ?: fromFile(homeDirectory / ".m2/.develocity/keys.properties", host)
        return keyEntry?.accessKey
    }

    private fun fromEnvVar(varName: String, host: String): HostAccessKeyEntry? =
        env[varName]?.split(';')
            ?.filterNot { it.isBlank() }
            ?.map(::HostAccessKeyEntry)
            ?.lastOrNull { it.host == host }

    private fun fromFile(path: Path, host: String): HostAccessKeyEntry? {
        if (!fileSystem.exists(path)) return null
        return fileSystem.read(path) {
            generateSequence { readUtf8Line()?.trim(' ') }
                .filterNot { it.isBlank() || it.isComment() }
                .map(::HostAccessKeyEntry)
                .lastOrNull { it.host == host }
        }
    }

    private fun String.isComment() = startsWith('#')
}
