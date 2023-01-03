package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.model.*

operator fun List<BuildAttributesValue>.get(name: String): String? {
    return find { it.name == name }?.value
}

operator fun List<BuildAttributesValue>.contains(name: String): Boolean {
    return any { it.name == name }
}
