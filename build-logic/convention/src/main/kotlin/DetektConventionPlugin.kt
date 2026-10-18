import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

/**
 * Static analysis for every Gradle module with Kotlin sources: detekt with
 * the shared config in config/detekt/detekt.yml, plus ktlint (through
 * detekt-formatting) and the Compose rules as detekt rule plugins.
 *
 * Check-only: autoCorrect stays off, so `./gradlew detekt` never rewrites
 * sources. Findings that predate the setup live in each module's
 * detekt-baseline.xml; regenerate with `./gradlew detektBaseline`.
 */
class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("io.gitlab.arturbosch.detekt")

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        val jvmTarget = libs.findVersion("jvmTarget").get().requiredVersion

        extensions.configure<DetektExtension> {
            toolVersion = libs.findVersion("detekt").get().requiredVersion
            config.setFrom(isolated.rootProject.projectDirectory.file("config/detekt/detekt.yml"))
            buildUponDefaultConfig = true
            baseline = file("detekt-baseline.xml")
            // Every source set (commonMain, androidMain, iosMain, test sets,
            // androidApp's src/main/java, ...), not just the JVM defaults
            source.setFrom(layout.projectDirectory.dir("src"))
            parallel = true
            autoCorrect = false
        }

        dependencies.add("detektPlugins", libs.findLibrary("detekt-formatting").get())
        dependencies.add("detektPlugins", libs.findLibrary("compose-rules-detekt").get())

        // detekt 1.23 embeds the Kotlin 2.0 compiler and refuses to run on a
        // newer one; keep the project's Kotlin version off its classpath.
        configurations.matching { it.name == "detekt" }.configureEach {
            resolutionStrategy.eachDependency {
                if (requested.group == "org.jetbrains.kotlin") {
                    useVersion(DETEKT_KOTLIN_VERSION)
                }
            }
        }

        tasks.withType<Detekt>().configureEach {
            this.jvmTarget = jvmTarget
            reports {
                html.required.set(true)
                xml.required.set(false)
                sarif.required.set(false)
                md.required.set(false)
                txt.required.set(false)
            }
        }
        tasks.withType<DetektCreateBaselineTask>().configureEach {
            this.jvmTarget = jvmTarget
        }
    }

    private companion object {
        /** The Kotlin version detekt 1.23.8 is compiled against. */
        const val DETEKT_KOTLIN_VERSION = "2.0.21"
    }
}
