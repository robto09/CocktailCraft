plugins {
    id("cocktailcraft.kmp.library")
    alias(libs.plugins.moko.resources)
}

kotlin {
    // The generated MR object is an expect/actual class. Those are Beta in
    // Kotlin; this flag acknowledges that and silences the per-file warning.
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain.dependencies {
            // The generated MR accessors (StringResource) and StringDesc are
            // part of this module's API, and the framework exports them for Swift.
            api(libs.moko.resources)
        }
    }
}

// Single source for the app's user-facing strings: base (English) and
// per-language strings.xml/plurals.xml under src/commonMain/moko-resources.
// Generates MR.strings / MR.plurals, Android string resources in this
// module's R class, and Localizable.strings(dict) in a resource bundle that
// the :shared framework carries and the iOS app copies in
// (copyFrameworkResourcesToApp, run by the "Compile Kotlin Framework" phase
// in iosApp/project.yml). See README "Localization".
multiplatformResources {
    // Unchanged from the single-module layout: Swift and Kotlin both import
    // com.cocktailcraft.resources.MR
    resourcesPackage.set("com.cocktailcraft.resources")
    iosBaseLocalizationRegion.set("en")
}
