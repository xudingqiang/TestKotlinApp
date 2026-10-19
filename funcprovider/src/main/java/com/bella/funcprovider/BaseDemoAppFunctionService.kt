package com.bella.funcprovider

import androidx.appfunctions.AppFunction
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint

/**
 * The target app in the demo: it publishes AppFunctions that another app (or an AI agent) may
 * invoke through `android.app.appfunctions`.
 *
 * The `appfunctions-compiler` KSP processor consumes the annotations below and generates:
 *  - `DemoAppFunctionService`: a concrete subclass of this abstract class that routes
 *    `onExecuteFunction(request, ...)` to the matching `@AppFunction` method.
 *  - `assets/app_functions_service.xml`: the schema describing the published functions.
 *
 * Note: the generated class must NOT have the same name as this abstract class.
 * Both are wired up in `src/main/AndroidManifest.xml`.
 */
@AppFunctionServiceEntryPoint(
    serviceName = "DemoAppFunctionService",
    appFunctionXmlFileName = "app_functions_service",
)
abstract class BaseDemoAppFunctionService : AppFunctionService() {

    /**
     * Adds two integers.
     *
     * @param a the first addend.
     * @param b the second addend.
     * @return the sum of [a] and [b].
     */
    @AppFunction(isDescribedByKDoc = true)
    fun add(a: Int, b: Int): Int {
        val sum = a + b
        lastResult = sum
        return sum
    }

    @AppFunction(isDescribedByKDoc = true)
    fun openImage(a: String, b: String): String {
       return  "test"
    }

    companion object {
        /**
         * Last value produced by [add]. The demo reads this back from the host app to prove the
         * function really executed in this process rather than in the caller.
         */
        @Volatile
        var lastResult: Int = Int.MIN_VALUE
            private set
    }
}
