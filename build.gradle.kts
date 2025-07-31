plugins {
    kotlin("multiplatform") version "2.2.0" apply false
    kotlin("plugin.serialization") version "2.2.0" apply false
    id("com.android.library") version "8.3.0" apply false
    id("com.squareup.sqldelight") version "1.5.5" apply false
    id("org.jetbrains.kotlin.android") version "2.2.0" apply false
    id("com.android.application") version "8.3.0" apply false
    id("org.jetbrains.kotlin.jvm") version "2.2.0" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
