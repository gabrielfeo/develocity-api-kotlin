package com.gabrielfeo.gradle.enterprise.api.extension

import com.gabrielfeo.gradle.enterprise.api.model.BuildAttributesValue

operator fun List<BuildAttributesValue>.get(name: String): String? {
    return find { it.name == name }?.value
}

operator fun List<BuildAttributesValue>.contains(name: String): Boolean {
    return any { it.name == name }
}
