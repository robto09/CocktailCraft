package com.cocktailcraft.android

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

/**
 * Custom test runner for Android instrumented tests.
 * Uses TestApplication for dependency injection setup.
 */
class TestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, TestApplication::class.java.name, context)
    }
}
