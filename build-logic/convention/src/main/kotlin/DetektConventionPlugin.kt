import dev.detekt.gradle.Detekt
import dev.detekt.gradle.DetektCreateBaselineTask
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

/**
 * Static analysis for every Gradle module with Kotlin sources: detekt with
 * the shared config in config/detekt/detekt.yml, plus ktlint (through
 * detekt-rules-ktlint-wrapper) and the Compose rules as detekt rule plugins.
 *
 * `detekt` is the light (PSI-only) pass over all of src/, so it also covers
 * the iOS and common source sets; type-aware rules are skipped there. The
 * type-resolved per-compilation tasks detekt 2.x registers
 * (`detektMainAndroid` in KMP modules, `detektMain`/`detektDebug` in
 * :androidApp) are opt-in and stay off the `check` path.
 *
 * Check-only by default: `./gradlew detekt` never rewrites sources unless run
 * with `-Pdetekt.autoCorrect=true` (ktlint formatting fixes). Findings that
 * predate the setup live in each module's detekt-baseline.xml; regenerate
 * with `./gradlew detektBaseline`.
 */
class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("dev.detekt")

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        val jvmTarget = libs.findVersion("jvmTarget").get().requiredVersion

        extensions.configure<DetektExtension> {
            toolVersion.set(libs.findVersion("detekt").get().requiredVersion)
            config.setFrom(isolated.rootProject.projectDirectory.file("config/detekt/detekt.yml"))
            buildUponDefaultConfig.set(true)
            baseline.set(layout.projectDirectory.file("detekt-baseline.xml"))
            // Every source set (commonMain, androidMain, iosMain, test sets,
            // androidApp's src/main/java, ...), not just the JVM defaults
            source.setFrom(layout.projectDirectory.dir("src"))
            parallel.set(true)
            // Opt-in per run: ./gradlew detekt -Pdetekt.autoCorrect=true
            autoCorrect.set(providers.gradleProperty("detekt.autoCorrect").map(String::toBoolean).orElse(false))
        }

        dependencies.add("detektPlugins", libs.findLibrary("detekt-rules-ktlint-wrapper").get())
        dependencies.add("detektPlugins", libs.findLibrary("compose-rules-detekt").get())

        tasks.withType<Detekt>().configureEach {
            this.jvmTarget.set(jvmTarget)
            reports {
                html.required.set(true)
                checkstyle.required.set(false)
                sarif.required.set(false)
                markdown.required.set(false)
            }
        }
        tasks.withType<DetektCreateBaselineTask>().configureEach {
            this.jvmTarget.set(jvmTarget)
        }
    }
}
