plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "cocktailcraft.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
    }
}
