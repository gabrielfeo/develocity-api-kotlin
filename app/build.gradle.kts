plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.10"
    application
    id("org.openapi.generator") version "6.2.1"
}

application {
    mainClass.set("com.gabrielfeo.gradle.enterprise.api.app.template.AppKt")
}

openApiGenerate {
    generatorName.set("kotlin")
    val apiSpec = providers.gradleProperty("gradle.enterprise.version")
        .map { version ->
            val configFile = "gradle-enterprise-$version-api.yaml"
            resources.text
                .fromUri("https://docs.gradle.com/enterprise/api-manual/ref/$configFile")
                .asFile().absolutePath
        }
    inputSpec.set(apiSpec)
    val generateDir = project.layout.buildDirectory.file("generated/$name")
    outputDir.set(generateDir.map { it.asFile.absolutePath })
    val ignoreFile = project.layout.projectDirectory.file(".openapi-generator-ignore")
    ignoreFileOverride.set(ignoreFile.asFile.absolutePath)
    apiPackage.set("com.gradle.enterprise.api")
    modelPackage.set(apiPackage.map { "$it.model" })
    packageName.set(apiPackage.map { "$it.client" })
}

sourceSets {
    main {
        java {
            srcDir(tasks.openApiGenerate)
        }
    }
}

dependencies {
    implementation("com.squareup.moshi:moshi:1.13.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.13.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
}
