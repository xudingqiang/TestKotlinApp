package com.bella.testapp.appfunctions

import android.app.appfunctions.AppFunctionException
import android.app.appfunctions.ExecuteAppFunctionRequest
import android.app.appfunctions.ExecuteAppFunctionResponse
import android.content.pm.SigningInfo
import android.os.Build
import android.os.CancellationSignal
import android.os.OutcomeReceiver
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunction
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint

/**
 * Required for the **caller** side too.
 *
 * `androidx.appfunctions` resolves cross-app function metadata through
 * `Dependencies.appFunctionInventory`, which looks up the generated
 * `androidx.appfunctions.internal.$AggregatedAppFunctionInventory_Impl`. That class is only
 * produced by the `appfunctions-compiler` KSP processor when a module declares at least one
 * `@AppFunctionServiceEntryPoint`. Without it every `AppFunctionManager.executeAppFunction` call
 * fails with "Cannot find implementation for AggregatedAppFunctionInventory".
 *
 * This demo has no interesting function of its own, so it publishes a single word-count helper.
 * The KSP processor turns it into `DemoAppFunctionService` +
 * `assets/app_functions_service.xml`; nothing here is used by DeepSeek.
 */
@RequiresApi(Build.VERSION_CODES.BAKLAVA)
@AppFunctionServiceEntryPoint(
    serviceName = "DemoAppFunctionService",
    appFunctionXmlFileName = "app_functions_service",
)
abstract class BaseDemoAppFunctionService : AppFunctionService() {

    /**
     * Counts the characters of [text].
     *
     * @param text the text to measure.
     * @return the number of characters in [text].
     */
    @AppFunction(isDescribedByKDoc = true)
    fun countCharacters(text: String): Int = text.length

    @AppFunction(isDescribedByKDoc = true)
    suspend fun fakeFunctionFde(text: String): String {
        return "Hello, $text"
    }

    override fun onCreate() {
        super.onCreate()
        Log.i("BaseDemoAppFunction", "Service onCreate")
    }

}
