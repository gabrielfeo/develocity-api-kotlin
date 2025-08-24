beforeSettings{
    pluginManagement {
        repositories {
            mavenCentral()
            gradlePluginPortal()
            exclusiveContent {
                forRepository { mavenLocal() }
                filter { includeGroup("com.gabrielfeo") }
            }
        }
    }
}

beforeProject {
    repositories {
        exclusiveContent {
            forRepository { mavenLocal() }
            filter { includeGroup("com.gabrielfeo") }
        }
    }
}

afterProject {
    configurations.all {
        resolutionStrategy {
            force("com.gabrielfeo:develocity-api-kotlin:SNAPSHOT")
        }
    }
}
