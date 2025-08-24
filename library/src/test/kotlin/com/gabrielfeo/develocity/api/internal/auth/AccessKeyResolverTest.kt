package com.gabrielfeo.develocity.api.internal.auth

import com.gabrielfeo.develocity.api.internal.FakeEnv
import okio.Path
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals


private val host = "host.example.com"
private val home = "/home/testuser".toPath()

class AccessKeyResolverTest {

    data class FileCase(val path: Path, val content: String, val expected: String?)
    data class EnvVarCase(val varName: String, val value: String?, val expected: String?)

    private lateinit var env: FakeEnv
    private lateinit var fileSystem: FakeFileSystem
    private lateinit var resolver: AccessKeyResolver

    @BeforeEach
    fun setUp() {
        env = FakeEnv()
        fileSystem = FakeFileSystem()
        resolver = AccessKeyResolver(env, home, fileSystem)
    }

    private fun writeKeysFile(path: Path, content: String) {
        fileSystem.createDirectories(path.parent!!)
        fileSystem.write(path) { writeUtf8(content) }
    }

    companion object {

        @JvmStatic
        fun standardFileCaseProvider() = listOf(
            "/home/testuser/.gradle/develocity/keys.properties".toPath(),
            "/home/testuser/.m2/.develocity/keys.properties".toPath(),
        ).flatMap {
            listOf(
                FileCase(it, content = "$host=foo\n", expected = "foo"),
                FileCase(it, content = "$host=foo", expected = "foo"),
                FileCase(it, content = "$host=foo # comment", expected = "foo"),
                FileCase(it, content = "$host=foo  #  comment", expected = "foo"),
                FileCase(it, content = "other=bar\n$host=foo\nnot$host=baz\n", expected = "foo"),
                FileCase(it, content = "\n#foo\n\nother=bar\n\n$host=foo\nnot$host=baz\n", expected = "foo"),
                FileCase(it, content = "", expected = null),
                FileCase(it, content = "\n", expected = null),
                FileCase(it, content = "other=bar\nnot$host=baz\n", expected = null),
            )
        }

        @JvmStatic
        fun customGradleUserHomeCaseProvider() = standardFileCaseProvider()

        @JvmStatic
        fun envVarCaseProvider() = listOf(
            "DEVELOCITY_ACCESS_KEY",
            "GRADLE_ENTERPRISE_ACCESS_KEY",
        ).flatMap {
            listOf(
                EnvVarCase(it, "$host=foo", expected = "foo"),
                EnvVarCase(it, ";$host=foo;", expected = "foo"),
                EnvVarCase(it, "other=bar;$host=foo;not$host=baz", expected = "foo"),
                EnvVarCase(it, "", expected = null),
                EnvVarCase(it, ";", expected = null),
                EnvVarCase(it, "other=bar;not$host=baz", expected = null),
            )
        }
    }

    @ParameterizedTest(name = "example")
    @MethodSource("standardFileCaseProvider")
    fun resolveFromStandardFile(case: FileCase) {
        writeKeysFile(case.path, case.content)
        assertEquals(case.expected, resolver.resolve(host))
    }

    @ParameterizedTest
    @MethodSource("customGradleUserHomeCaseProvider")
    fun resolveFromFileOnCustomGradleUserHome(case: FileCase) {
        val customHome = "/custom/gradle/user/home".toPath()
        env["GRADLE_USER_HOME"] = customHome.toString()
        writeKeysFile(customHome / "develocity/keys.properties", case.content)
        assertEquals(case.expected, resolver.resolve(host))
    }

    @ParameterizedTest
    @MethodSource("envVarCaseProvider")
    fun resolveFromEnvVar(case: EnvVarCase) {
        env[case.varName] = case.value
        assertEquals(case.expected, resolver.resolve(host))
    }
}
