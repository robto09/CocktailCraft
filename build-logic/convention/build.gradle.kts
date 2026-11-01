plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    // The root build does not apply detekt, so build-logic brings the plugin
    implementation(libs.detekt.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "cocktailcraft.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("detekt") {
            id = "cocktailcraft.detekt"
            implementationClass = "DetektConventionPlugin"
        }
    }
}
