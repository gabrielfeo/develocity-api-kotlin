package com.gabrielfeo.develocity.api.extension

import com.gabrielfeo.develocity.api.model.BuildAttributesValue

operator fun List<BuildAttributesValue>.get(name: String): String? {
    return find { it.name == name }?.value
}

operator fun List<BuildAttributesValue>.contains(name: String): Boolean {
    return any { it.name == name }
}
