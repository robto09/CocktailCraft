// Cross-platform design tokens (DesignTokens.kt): plain Kotlin, no
// dependencies. Android maps them into its Compose theme; iOS reads them
// through the :shared framework, which exports this module.
plugins {
    id("cocktailcraft.kmp.library")
}
