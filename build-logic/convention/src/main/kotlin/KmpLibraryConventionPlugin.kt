import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Shared setup for every KMP library module: the Android + iOS
 * targets, the JVM toolchain, SDK levels from the version catalog, the
 * project-wide opt-ins and the Android host-test compilation. A module's own
 * build file only adds its dependencies and module-specific plugins.
 *
 * The Android namespace defaults to `com.cocktailcraft.` + the project path
 * (`:core:domain` -> `com.cocktailcraft.core.domain`); a module can override it.
 */
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("com.android.kotlin.multiplatform.library")
        // detekt + ktlint + Compose rules (DetektConventionPlugin)
        pluginManager.apply("cocktailcraft.detekt")

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        fun version(alias: String): String = libs.findVersion(alias).get().requiredVersion

        extensions.configure<KotlinMultiplatformExtension> {
            jvmToolchain(version("jvmTarget").toInt())

            sourceSets.all {
                // For @HiddenFromObjC on Kotlin-side helpers
                languageSettings.optIn("kotlin.experimental.ExperimentalObjCRefinement")
                // kotlin.time.Clock/Instant (kotlinx-datetime 0.7+) — stable in Kotlin 2.3
                languageSettings.optIn("kotlin.time.ExperimentalTime")
                // kotlinx-coroutines-test's virtual-time and dispatcher APIs
                // (StandardTestDispatcher, advanceUntilIdle, ...) are still
                // experimental; tests use them throughout
                if (name.endsWith("Test")) {
                    languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                }
            }

            extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("android") {
                namespace = "com.cocktailcraft" + path.replace(':', '.')
                compileSdk = version("compileSdk").toInt()
                // Same floor as androidApp (baselineprofile alone needs 28)
                minSdk = 24

                // Host-side (JVM) unit tests, equivalent of the old androidUnitTest
                withHostTestBuilder {}.configure {
                    // moko-resources strings resolve through the generated
                    // Android R class, so host tests need the merged resources
                    isIncludeAndroidResources = true
                }
            }

            // The deployment target lives in iosApp/project.yml; only the
            // :shared umbrella turns these into a framework.
            // No iosX64(): Apple-silicon simulators and devices only, so an
            // Intel Mac cannot build the app for its (x86_64) simulator.
            iosArm64()
            iosSimulatorArm64()
        }
    }
}
