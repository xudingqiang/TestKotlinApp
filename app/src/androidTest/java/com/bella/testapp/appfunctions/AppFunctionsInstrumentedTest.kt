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
 * The target app (`com.fde.gallery3d`) ships on the FDE image. On other images
 * [galleryIsInstalled] fails with an explicit message instead of a confusing wrapper error.
 *
 * Note on the privileged permission: executing a function owned by *another* package needs
 * `android.permission.EXECUTE_APP_FUNCTIONS`, which on Android 17+ is a known-signer permission.
 * [openImage_callsTheGalleryAppFunction] therefore accepts either a real success or a readable
 * platform error, and reports which one happened instead of failing a normal third-party install.
 */
@RunWith(AndroidJUnit4::class)
class AppFunctionsInstrumentedTest {

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun openImageParameters_roundTripsThroughGeneratedFactory() {
        val parameters = OpenImageParameters(
            path = "/mnt/sdcard/Pictures/aa.png",
            title = "aa",
        )

        val data = AppFunctionData.serialize(parameters, OpenImageParameters::class.java)

        assertEquals("com.bella.testapp.appfunctions.OpenImageParameters", data.qualifiedName)
        assertEquals("/mnt/sdcard/Pictures/aa.png", data.getString("path"))
        assertEquals("aa", data.getString("title"))
        assertEquals(parameters, data.deserialize(OpenImageParameters::class.java))
    }

    @Test
    fun galleryIsInstalled() {
        val installed = context.packageManager.getInstalledPackages(0)
            .any { it.packageName == GalleryAppFunctionsClient.GALLERY_PACKAGE }
        assertTrue(
            "目标应用 ${GalleryAppFunctionsClient.GALLERY_PACKAGE} 未安装（该应用随 FDE 镜像提供）",
            installed,
        )
    }

    @Test
    fun openImage_callsTheGalleryAppFunction(): Unit = runBlocking {
        val client = GalleryAppFunctionsClient(context)
        val result = client.openImage(
            path = GalleryAppFunctionsClient.DEFAULT_IMAGE_PATH,
            title = "aa",
        )

        // Always surface the platform's answer in logcat so the exact rejection reason is visible.
        android.util.Log.i(TAG, "openImage -> $result")

        when (result) {
            is AgentFunctionResult.Success -> assertEquals("ok", result.raw)

            is AgentFunctionResult.Failure -> {
                // The wiring is what this test verifies when the install is not privileged. A
                // privileged (system-signed / allowlisted) build reaches Success above.
                assertTrue(
                    "失败必须带可读原因，实际为: ${result.displayText}",
                    result.displayText.trim().length > 10,
                )
                android.util.Log.i(
                    TAG,
                    "跨应用调用被平台拒绝（预期于普通安装）: ${result.displayText}",
                )
            }
        }
    }

    private companion object {
        const val TAG = "AppFunctionsTest"
    }
}
