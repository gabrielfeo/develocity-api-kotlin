package build.logic

import com.gabrielfeo.develocity.api.Config
import com.gabrielfeo.develocity.api.DevelocityApi
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

abstract class DevelocityApiService
    : DevelocityApi by DevelocityApi.newInstance(),
      BuildService<BuildServiceParameters.None>,
      AutoCloseable {

    override fun close() {
        shutdown()
    }
}
