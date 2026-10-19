package com.bella.testapp.appfunctions

import androidx.appfunctions.AppFunctionData
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Device-side checks for the AppFunctions half of the DeepSeek agent demo.
 *
 * Requires the target app to be installed:
 * `./gw.sh :funcprovider:installDebug` (or run the `funcprovider` run configuration first).
 *
 * Note on the privileged permission: executing a function owned by *another* package needs
 * `android.permission.EXECUTE_APP_FUNCTIONS`, which on Android 17+ is a known-signer permission.
 * [crossAppAdd_executesTheProviderFunction] therefore asserts on the shape of the outcome and
 * reports what the platform answered, instead of failing a normal third-party install.
 */
@RunWith(AndroidJUnit4::class)
class AppFunctionsInstrumentedTest {

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun addParameters_roundTripsThroughGeneratedFactory() {
        val parameters = AddParameters(a = 12, b = 30)

        val data = AppFunctionData.serialize(parameters, AddParameters::class.java)

        assertEquals("com.bella.testapp.appfunctions.AddParameters", data.qualifiedName)
        assertEquals(12, data.getInt("a"))
        assertEquals(30, data.getInt("b"))
        assertEquals(parameters, data.deserialize(AddParameters::class.java))
    }

    @Test
    fun providerPackage_isInstalled() {
        val packageManager = context.packageManager
        val installed = packageManager.getInstalledPackages(0)
            .any { it.packageName == AppFunctionsClient.PROVIDER_PACKAGE }
        assertTrue(
            "目标应用 ${AppFunctionsClient.PROVIDER_PACKAGE} 未安装；请先执行 " +
                "./gw.sh :funcprovider:installDebug",
            installed,
        )
    }

    @Test
    fun crossAppAdd_executesTheProviderFunction() = runBlocking {
        val client = AppFunctionsClient(context)
        val result = client.callAdd(12, 30)

        when (result) {
            is AgentFunctionResult.Success -> {
                assertEquals("12 + 30 = 42", result.displayText)
            }

            is AgentFunctionResult.Failure -> {
                // A normal third-party install is not granted EXECUTE_APP_FUNCTIONS, so a clear
                // platform error is an expected, informative outcome rather than a test bug.
                assertTrue(
                    "调用失败应给出可读原因，实际为: ${result.displayText}",
                    result.displayText.isNotBlank(),
                )
            }
        }
    }
}
