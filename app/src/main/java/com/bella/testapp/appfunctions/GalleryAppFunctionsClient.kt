package com.bella.testapp.appfunctions

import android.app.appfunctions.AppFunctionManager as PlatformAppFunctionManager
import android.app.appfunctions.ExecuteAppFunctionRequest as PlatformExecuteAppFunctionRequest
import android.app.appfunctions.ExecuteAppFunctionResponse as PlatformExecuteAppFunctionResponse
import android.app.appsearch.GenericDocument
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appfunctions.AppFunctionData
import androidx.appfunctions.AppFunctionSerializable
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Arguments of `com.fde.gallery3d`'s `openImage` AppFunction.
 *
 * The gallery service reads exactly these two string properties off the request's
 * `GenericDocument` (`getPropertyString("path")`, `getPropertyString("title")`), so the property
 * names must not change.
 */
@AppFunctionSerializable
data class OpenImageParameters(
    val path: String,
    val title: String,
)

/** Result of executing an AppFunction. */
sealed interface AgentFunctionResult {
    data class Success(val displayText: String, val raw: String) : AgentFunctionResult
    data class Failure(val displayText: String, val cause: Throwable? = null) : AgentFunctionResult
}

/**
 * Calls the media AppFunctions published by the on-device gallery (`com.fde.gallery3d`).
 *
 * Platform discovery data (see `adb shell cmd app_function list-app-functions`):
 * ```
 * com.fde.gallery3d
 *   serviceName: com.fde.gallery3d.appfunctions.MediaAppFunctionsService
 *   functionId:  openImage   (media / openImage / v1, enabled by default)
 *   functionId:  playVideo
 * ```
 * `openImage` takes `path` + `title` and starts the gallery's image viewer.
 *
 * ## Why the raw platform API
 *
 * On API 37 the Jetpack `androidx.appfunctions` wrapper resolves function metadata through its own
 * app-local inventory and then its schema inventory. This app owns functions of its own, so that
 * lookup answers "not found" for *other* apps' functions before the platform is ever asked. The
 * platform API (`android.app.appfunctions.AppFunctionManager`) is the supported route for a caller:
 * it hands the request straight to the system AppFunctions service.
 */
class GalleryAppFunctionsClient(private val context: Context) {

    companion object {
        /** Package that publishes the media AppFunctions. */
        const val GALLERY_PACKAGE = "com.fde.gallery3d"

        /** Function id of the "open an image" capability. */
        const val FUNC_OPEN_IMAGE = "openImage"

        /** Schema category the gallery registers its functions under. */
        const val SCHEMA_CATEGORY_MEDIA = "media"

        /** AppFunctions need API 36 (Baklava) or a device that ships the extension library. */
        const val MIN_SDK_FOR_APP_FUNCTIONS = Build.VERSION_CODES.BAKLAVA

        /** Demo image path used when the user does not provide one. */
        const val DEFAULT_IMAGE_PATH = "/mnt/sdcard/Pictures/aa.png"

        /** Permission a *caller* needs to execute another app's AppFunctions. */
        const val PERMISSION_EXECUTE_APP_FUNCTIONS =
            "android.permission.EXECUTE_APP_FUNCTIONS"
    }

    /** True when the platform AppFunctions service exists on this device. */
    val isSupported: Boolean
        get() = Build.VERSION.SDK_INT >= MIN_SDK_FOR_APP_FUNCTIONS &&
            context.getSystemService(PlatformAppFunctionManager::class.java) != null

    /**
     * True when this install may actually execute **another** app's AppFunctions.
     *
     * `EXECUTE_APP_FUNCTIONS` is a known-signer / privileged permission on Android 17, so a normal
     * third-party install is denied and the platform answers
     * "Caller does not have permission to execute the appfunction (code 1000)". Only a
     * system-signed app (or one whose certificate is in
     * `@config_executeAppFunctionsKnownSigners`) — i.e. the on-device agent — is granted it.
     */
    fun hasExecutePermission(): Boolean =
        context.checkSelfPermission(PERMISSION_EXECUTE_APP_FUNCTIONS) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * Executes `openImage` in [GALLERY_PACKAGE].
     *
     * @param path absolute path of the image file.
     * @param title title the gallery should display for the image.
     */
    suspend fun openImage(path: String, title: String): AgentFunctionResult {
        if (Build.VERSION.SDK_INT < MIN_SDK_FOR_APP_FUNCTIONS) {
            return AgentFunctionResult.Failure(
                "当前设备是 API ${Build.VERSION.SDK_INT}，AppFunctions 需要 API " +
                    "$MIN_SDK_FOR_APP_FUNCTIONS。",
            )
        }
        val manager = context.getSystemService(PlatformAppFunctionManager::class.java)
            ?: return AgentFunctionResult.Failure("本设备没有 android.app.appfunctions.AppFunctionManager。")

        if (!hasExecutePermission()) {
            return AgentFunctionResult.Failure(
                "缺少 $PERMISSION_EXECUTE_APP_FUNCTIONS（特权/系统签名权限）。" +
                    "普通安装无法授予，需要系统签名的 Agent 或 allowlist 证书。",
            )
        }

        return try {
            val parameters = GenericDocument.Builder<GenericDocument.Builder<*>>(
                /* namespace = */ "",
                /* id = */ FUNC_OPEN_IMAGE,
                /* schemaType = */ FUNC_OPEN_IMAGE,
            )
                .setPropertyString("path", path)
                .setPropertyString("title", title)
                .build()

            val request = PlatformExecuteAppFunctionRequest
                .Builder(GALLERY_PACKAGE, FUNC_OPEN_IMAGE)
                .setParameters(parameters)
                .build()

            val response = executePlatform(manager, request)
            // The platform wraps the gallery's `MediaResult` document; it carries `status="ok"`.
            val status = runCatching {
                response.resultDocument.getPropertyString("status")
            }.getOrNull()
            AgentFunctionResult.Success(
                displayText = "已调用 $GALLERY_PACKAGE#$FUNC_OPEN_IMAGE" +
                    (status?.let { "，返回 status=$it" } ?: "（返回文档无 status 字段）"),
                raw = status ?: "ok",
            )
        } catch (t: Throwable) {
            AgentFunctionResult.Failure(
                displayText = "AppFunctions 调用失败：${t.javaClass.simpleName}: ${t.message}",
                cause = t,
            )
        }
    }

    /** Bridges the platform `OutcomeReceiver` callback API into a suspend function. */
    private suspend fun executePlatform(
        manager: PlatformAppFunctionManager,
        request: PlatformExecuteAppFunctionRequest,
    ): PlatformExecuteAppFunctionResponse = suspendCancellableCoroutine { continuation ->
        manager.executeAppFunction(
            request,
            Runnable::run,
            android.os.CancellationSignal(),
            object :
                android.os.OutcomeReceiver<
                    PlatformExecuteAppFunctionResponse,
                    android.app.appfunctions.AppFunctionException,
                    > {
                override fun onResult(result: PlatformExecuteAppFunctionResponse) {
                    if (continuation.isActive) continuation.resume(result)
                }

                override fun onError(error: android.app.appfunctions.AppFunctionException) {
                    if (continuation.isActive) {
                        continuation.resumeWith(Result.failure(error))
                    }
                }
            },
        )
    }
}
