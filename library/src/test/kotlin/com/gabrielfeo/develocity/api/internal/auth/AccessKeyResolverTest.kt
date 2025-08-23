package com.gabrielfeo.develocity.api.internal.auth

import com.gabrielfeo.develocity.api.internal.FakeEnv
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.*

class AccessKeyResolverTest {
    private val host = "host.example.com"
    private val home = "/home/testuser".toPath()
    private lateinit var env: FakeEnv
    private lateinit var fs: FakeFileSystem

    @BeforeTest
    fun setUp() {
        env = FakeEnv()
        fs = FakeFileSystem()
    }

    private fun resolver() = AccessKeyResolver(env, home, fs)

    private fun writeKeysFile(path: String, content: String) {
        val filePath = path.toPath()
        fs.createDirectories(filePath.parent!!)
        fs.write(filePath) { writeUtf8(content) }
    }

    // region ~/.gradle
    @Test fun `~ gradle - single key with newline`() = testGradleFile("$host=foo\n", "foo")
    @Test fun `~ gradle - single key no newline`() = testGradleFile("$host=foo", "foo")
    @Test fun `~ gradle - single key with semicolon`() = testGradleFile("$host=foo;\n", "foo")
    @Test fun `~ gradle - single key no semicolon`() = testGradleFile("$host=foo;", "foo")
    @Test fun `~ gradle - multiple keys`() = testGradleFile("other=bar\n$host=foo\nnot$host=baz\n", "foo")
    @Test fun `~ gradle - no keys`() = testGradleFile("", null)
    @Test fun `~ gradle - no matching key`() = testGradleFile("other=bar\nnot$host=baz\n", null)
    private fun testGradleFile(content: String, expected: String?) {
        writeKeysFile("/home/testuser/.gradle/develocity/keys.properties", content)
        assertEquals(expected, resolver().resolve(host))
    }
    // endregion

    // region GRADLE_USER_HOME
    @Test fun `GRADLE_USER_HOME - single key with newline`() = testGradleUserHome("$host=foo\n", "foo")
    @Test fun `GRADLE_USER_HOME - single key no newline`() = testGradleUserHome("$host=foo", "foo")
    @Test fun `GRADLE_USER_HOME - single key with semicolon`() = testGradleUserHome("$host=foo;\n", "foo")
    @Test fun `GRADLE_USER_HOME - single key no semicolon`() = testGradleUserHome("$host=foo;", "foo")
    @Test fun `GRADLE_USER_HOME - multiple keys`() = testGradleUserHome("other=bar\n$host=foo\nnot$host=baz\n", "foo")
    @Test fun `GRADLE_USER_HOME - no keys`() = testGradleUserHome("", null)
    @Test fun `GRADLE_USER_HOME - no matching key`() = testGradleUserHome("other=bar\nnot$host=baz\n", null)
    private fun testGradleUserHome(content: String, expected: String?) {
        val customHome = "/custom/gradle/home".toPath()
        env["GRADLE_USER_HOME"] = customHome.toString()
        writeKeysFile("/custom/gradle/home/develocity/keys.properties", content)
        assertEquals(expected, resolver().resolve(host))
    }
    // endregion

    // region ~/.m2
    @Test fun `~ m2 - single key with newline`() = testM2File("$host=foo\n", "foo")
    @Test fun `~ m2 - single key no newline`() = testM2File("$host=foo", "foo")
    @Test fun `~ m2 - single key with semicolon`() = testM2File("$host=foo;\n", "foo")
    @Test fun `~ m2 - single key no semicolon`() = testM2File("$host=foo;", "foo")
    @Test fun `~ m2 - multiple keys`() = testM2File("other=bar\n$host=foo\nnot$host=baz\n", "foo")
    @Test fun `~ m2 - no keys`() = testM2File("", null)
    @Test fun `~ m2 - no matching key`() = testM2File("other=bar\nnot$host=baz\n", null)
    private fun testM2File(content: String, expected: String?) {
        writeKeysFile("/home/testuser/.m2/.develocity/keys.properties", content)
        assertEquals(expected, resolver().resolve(host))
    }
    // endregion

    // region DEVELOCITY_ACCESS_KEY
    @Test fun `env var DEVELOCITY_ACCESS_KEY - single key`() = testEnvVar("DEVELOCITY_ACCESS_KEY", "$host=foo", "foo")
    @Test fun `env var DEVELOCITY_ACCESS_KEY - single key with semicolon`() = testEnvVar("DEVELOCITY_ACCESS_KEY", "$host=foo;", "foo")
    @Test fun `env var DEVELOCITY_ACCESS_KEY - multiple keys`() = testEnvVar("DEVELOCITY_ACCESS_KEY", "other=bar;$host=foo;not$host=baz", "foo")
    @Test fun `env var DEVELOCITY_ACCESS_KEY - no keys`() = testEnvVar("DEVELOCITY_ACCESS_KEY", "", null)
    @Test fun `env var DEVELOCITY_ACCESS_KEY - no matching key`() = testEnvVar("DEVELOCITY_ACCESS_KEY", "other=bar;not$host=baz", null)
    // endregion

    // region GRADLE_ENTERPRISE_ACCESS_KEY
    @Test fun `env var GRADLE_ENTERPRISE_ACCESS_KEY - single key`() = testEnvVar("GRADLE_ENTERPRISE_ACCESS_KEY", "$host=foo", "foo")
    @Test fun `env var GRADLE_ENTERPRISE_ACCESS_KEY - single key with semicolon`() = testEnvVar("GRADLE_ENTERPRISE_ACCESS_KEY", "$host=foo;", "foo")
    @Test fun `env var GRADLE_ENTERPRISE_ACCESS_KEY - multiple keys`() = testEnvVar("GRADLE_ENTERPRISE_ACCESS_KEY", "other=bar;$host=foo;not$host=baz", "foo")
    @Test fun `env var GRADLE_ENTERPRISE_ACCESS_KEY - no keys`() = testEnvVar("GRADLE_ENTERPRISE_ACCESS_KEY", "", null)
    @Test fun `env var GRADLE_ENTERPRISE_ACCESS_KEY - no matching key`() = testEnvVar("GRADLE_ENTERPRISE_ACCESS_KEY", "other=bar;not$host=baz", null)
    private fun testEnvVar(varName: String, value: String?, expected: String?) {
        env[varName] = value
        assertEquals(expected, resolver().resolve(host))
    }
    // endregion
}
