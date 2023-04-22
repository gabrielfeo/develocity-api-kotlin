package com.gabrielfeo.gradle.enterprise.api.example

class App {
    val greeting: String
        get() {
            return "Hello World!"
        }
}

fun main() {
    println(App().greeting)
}
