package com.bella.testapp

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bella.testapp.appfunctions.AgentRunResult
import com.bella.testapp.appfunctions.DeepSeekAgent
import com.bella.testapp.appfunctions.GalleryAppFunctionsClient
import kotlinx.coroutines.launch

/**
 * DeepSeek + AppFunctions demo, wired into [MainActivity].
 *
 * The demo shows the two halves of an on-device agent flow:
 *  - **DeepSeek** turns a natural-language request into a tool call (function calling).
 *  - **AppFunctions** (`android.app.appfunctions`) executes the requested function, which lives in
 *    a *different* app — here the gallery (`com.fde.gallery3d`), function `openImage(path, title)` —
 *    so the call genuinely crosses a package boundary.
 *
 * Everything lives in this file (and the `appfunctions` package) so the demo can be deleted again
 * without touching the rest of [MainActivity].
 */

/** DeepSeek API keys look like `sk-...`; kept as a soft check so demos stay easy to run. */
private const val DEEPSEEK_KEY_PREFIX = "sk-"

private const val TAG = "AgentDemo"

/** Views owned by the demo section at the bottom of `activity_main.xml`. */
private class AgentDemoViews(
    val keyInput: EditText,
    val promptInput: EditText,
    val imagePathInput: EditText,
    val runButton: Button,
    val openGalleryButton: Button,
    val grantButton: Button,
    val logView: TextView,
)

private fun MainActivity.bindAgentDemoViews(): AgentDemoViews = AgentDemoViews(
    keyInput = findViewById(R.id.editDeepSeekKey),
    promptInput = findViewById(R.id.editAgentPrompt),
    imagePathInput = findViewById(R.id.editImagePath),
    runButton = findViewById(R.id.btnRunAgent),
    openGalleryButton = findViewById(R.id.btnOpenGallery),
    grantButton = findViewById(R.id.btnGrantFunctionPermission),
    logView = findViewById(R.id.tvAgentLog),
)

/**
 * Binds the demo widgets. Called from [MainActivity.initView].
 */
internal fun MainActivity.initAgentDemoView() {
    val views = bindAgentDemoViews()
    val client = GalleryAppFunctionsClient(this)
    views.logView.text = buildString {
        append("API ${Build.VERSION.SDK_INT} | AppFunctions 可用: ")
        append(if (client.isSupported) "是" else "否")
        append('\n')
        append("目标应用: ${GalleryAppFunctionsClient.GALLERY_PACKAGE}\n")
        append("目标函数: ${GalleryAppFunctionsClient.FUNC_OPEN_IMAGE}(path, title)\n")
        append("EXECUTE_APP_FUNCTIONS: ")
        append(if (client.hasExecutePermission()) "已授予" else "未授予")
        append("\n\n填入 DeepSeek API Key 后点击“运行智能体”。")
    }
    if (!client.hasExecutePermission()) {
        views.grantButton.visibility = android.view.View.VISIBLE
    }
}

/**
 * Wires the demo buttons. Called from [MainActivity.initEvent].
 */
internal fun MainActivity.initAgentDemoEvent() {
    val views = bindAgentDemoViews()

    views.runButton.setOnClickListener { runDeepSeekAgent(views) }

    views.openGalleryButton.setOnClickListener {
        launchPackageOrWarn(GalleryAppFunctionsClient.GALLERY_PACKAGE)
    }

    // EXECUTE_APP_FUNCTIONS is a privileged/known-signer permission, so it cannot be requested at
    // runtime. The button only opens the per-app screen where a privileged build can be granted.
    views.grantButton.setOnClickListener {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:$packageName"),
        )
        runCatching { startActivity(intent) }
        Toast.makeText(
            this,
            "EXECUTE_APP_FUNCTIONS 是特权权限，需要系统签名或 allowlist 才能授予；" +
                "普通安装无法在设置里打开它。",
            Toast.LENGTH_LONG,
        ).show()
    }
}

private fun MainActivity.launchPackageOrWarn(pkg: String) {
    val intent = packageManager.getLaunchIntentForPackage(pkg)
    if (intent == null) {
        Toast.makeText(this, "未安装 $pkg", Toast.LENGTH_LONG).show()
    } else {
        startActivity(intent)
    }
}

/**
 * Runs one agent turn on [lifecycleScope] so the network call and the cross-app function call stay
 * off the main thread and are cancelled with the activity.
 */
private fun MainActivity.runDeepSeekAgent(views: AgentDemoViews) {
    val apiKey = views.keyInput.text?.toString()?.trim().orEmpty()
    val prompt = views.promptInput.text?.toString()?.trim().orEmpty()
    val imagePath = views.imagePathInput.text?.toString()?.trim().orEmpty()

    if (apiKey.isEmpty()) {
        Toast.makeText(this, "请先填写 DeepSeek API Key", Toast.LENGTH_SHORT).show()
        return
    }
    if (!apiKey.startsWith(DEEPSEEK_KEY_PREFIX)) {
        Toast.makeText(this, "API Key 通常以 $DEEPSEEK_KEY_PREFIX 开头，请确认", Toast.LENGTH_SHORT).show()
    }
    if (prompt.isEmpty()) {
        Toast.makeText(this, "请输入要对智能体说的话", Toast.LENGTH_SHORT).show()
        return
    }

    views.runButton.isEnabled = false
    views.logView.text = ""

    lifecycleScope.launch {
        val logLines = StringBuilder()
        fun log(line: String) {
            Log.d(TAG, line)
            logLines.append(line).append('\n')
            views.logView.text = logLines.toString()
        }

        val agent = DeepSeekAgent(
            context = this@runDeepSeekAgent,
            apiKey = apiKey,
            log = ::log,
        )
        try {
            log("用户: $prompt")
            if (imagePath.isNotEmpty()) log("指定图片路径: $imagePath")
            when (
                val result = agent.run(
                    prompt = prompt,
                    fallbackPath = imagePath.ifEmpty { null },
                )
            ) {
                is AgentRunResult.Answered -> {
                    log("")
                    log("最终回答: ${result.text}")
                }

                is AgentRunResult.Failure -> {
                    log("")
                    log("失败: ${result.message}")
                }
            }
        } catch (t: Throwable) {
            log("异常: ${t.javaClass.simpleName}: ${t.message}")
        } finally {
            agent.close()
            views.runButton.isEnabled = true
        }
    }
}
