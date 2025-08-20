package build.logic

import com.gabrielfeo.develocity.api.Config
import com.gabrielfeo.develocity.api.DevelocityApi
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import okhttp3.OkHttpClient

abstract class DevelocityApiService
    : DevelocityApi by DevelocityApi.newInstance(config()),
      BuildService<BuildServiceParameters.None>,
      AutoCloseable {

    override fun close() {
        shutdown()
    }
}

private fun config() = Config(
    // Necessary to accomodate Gradle's build service lifecycle because library
    // uses a singleton OkHttpClient.Builder unless one is provided.
    // See https://github.com/gabrielfeo/develocity-api-kotlin/issues/451
    clientBuilder = OkHttpClient.Builder(),
)
