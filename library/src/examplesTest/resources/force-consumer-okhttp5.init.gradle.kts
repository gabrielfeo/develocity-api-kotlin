// Simulates a consumer that has independently upgraded to okhttp 5.x, while this
// library still declares (and is tested against) okhttp 4.x. "okhttp" and "okhttp-jvm"
// share the same Gradle module identity (okhttp:5.x redirects to okhttp-jvm:5.x via
// Gradle Module Metadata's available-at mechanism), so a plain version force is enough
// for Gradle's normal conflict resolution to pick the consumer's higher version.
afterProject {
    configurations.all {
        resolutionStrategy {
            force("com.squareup.okhttp3:okhttp:5.5.0")
        }
    }
}
