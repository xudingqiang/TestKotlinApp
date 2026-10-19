package com.bella.funcprovider

import android.os.CancellationSignal
import androidx.appfunctions.AppFunctionFunctionNotFoundException
import androidx.appfunctions.ExecuteAppFunctionRequest
import androidx.appfunctions.ExecuteAppFunctionResponse
import androidx.appfunctions.`internal`.AppFunctionExecutionDispatcher
import androidx.appfunctions.`internal`.AppFunctionInventory
import androidx.appfunctions.`internal`.Dispatchers
import java.util.function.Consumer
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.String
import kotlin.Unit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@Generated("androidx.appfunctions.compiler.AppFunctionCompiler")
public class DemoAppFunctionService : BaseDemoAppFunctionService() {
  private lateinit var scope: CoroutineScope

  override fun onCreate() {
    super.onCreate()
    scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  }

  override fun onDestroy() {
    scope.cancel()
    super.onDestroy()
  }

  override fun onExecuteFunction(
    request: ExecuteAppFunctionRequest,
    cancellationSignal: CancellationSignal,
    callback: Consumer<ExecuteAppFunctionResponse>,
  ): Unit = AppFunctionExecutionDispatcher.dispatchExecuteAppFunction(
    scope,
    request,
    resolveInventory(),
    cancellationSignal,
    callback,
  ) { parameters ->
    when (request.functionIdentifier) {
      FUNCTION_ID_ADD -> {
        this.add(
          parameters["a"] as Int,
          parameters["b"] as Int,
        )
      }
      FUNCTION_ID_OPEN_IMAGE -> {
        this.openImage(
          parameters["a"] as String,
          parameters["b"] as String,
        )
      }
      else -> {
        throw AppFunctionFunctionNotFoundException(
                "${request.functionIdentifier} is not available"
            )
      }
    }
  }

  override fun resolveInventory(): AppFunctionInventory = `$BaseDemoAppFunctionService_AppFunctionInventory`()

  public companion object {
    public const val FUNCTION_ID_ADD: String =
        "com.bella.funcprovider.BaseDemoAppFunctionService#add"

    public const val FUNCTION_ID_OPEN_IMAGE: String =
        "com.bella.funcprovider.BaseDemoAppFunctionService#openImage"
  }
}
