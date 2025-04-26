plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.multiplatorm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.maven.publish) apply false

    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.atomicfu)
    alias(libs.plugins.ktlint)
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint") // Version should be inherited from parent

    // Optionally configure plugin
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.0.1")
        filter {
            exclude("**/generated/**")
            include("**/kotlin/**")
        }
    }
}

tasks.register<Copy>("setUpGitHooks") {
    group = "help"
    from("$rootDir/.hooks")
    into("$rootDir/.git/hooks")
}
