package com.bella.testapp

import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bella.testapp.appfunctions.AgentRunResult
import com.bella.testapp.appfunctions.AppFunctionsClient
import com.bella.testapp.appfunctions.DeepSeekAgent
import kotlinx.coroutines.launch

/**
 * DeepSeek + AppFunctions demo, wired into [MainActivity].
 *
 * The demo shows the two halves of an on-device agent flow:
 *  - **DeepSeek** turns a natural-language request into a tool call (function calling).
 *  - **AppFunctions** (`android.app.appfunctions`) executes the requested function, which here lives
 *    in a *different* app (`com.bella.funcprovider`) - so the call genuinely crosses a package
 *    boundary instead of running locally.
 *
 * Everything lives in this file (and the `appfunctions` package) so the demo can be deleted again
 * without touching the rest of [MainActivity].
 */

/** DeepSeek API keys look like `sk-...`; kept as a soft check so demos stay easy to run. */
private const val DEEPSEEK_KEY_PREFIX = "sk-"

/** Views owned by the demo section at the bottom of `activity_main.xml`. */
private class AgentDemoViews(
    val keyInput: EditText,
    val promptInput: EditText,
    val runButton: Button,
    val openProviderButton: Button,
    val logView: TextView,
)

private fun MainActivity.bindAgentDemoViews(): AgentDemoViews = AgentDemoViews(
    keyInput = findViewById(R.id.editDeepSeekKey),
    promptInput = findViewById(R.id.editAgentPrompt),
    runButton = findViewById(R.id.btnRunAgent),
    openProviderButton = findViewById(R.id.btnOpenProvider),
    logView = findViewById(R.id.tvAgentLog),
)

/**
 * Binds the demo widgets. Called from [MainActivity.initView].
 */
internal fun MainActivity.initAgentDemoView() {
    val views = bindAgentDemoViews()
    val supported = AppFunctionsClient(this).isSupported
    views.logView.text = buildString {
        append("AppFunctions 可用: ")
        append(if (supported) "是" else "否")
        append("（API ${Build.VERSION.SDK_INT}）\n")
        append("提供方应用: ${AppFunctionsClient.PROVIDER_PACKAGE}\n")
        append("函数: ${AppFunctionsClient.FUNC_ADD}\n\n")
        append("填入 DeepSeek API Key 后点击“运行智能体”。")
    }
}

/**
 * Wires the demo buttons. Called from [MainActivity.initEvent].
 */
internal fun MainActivity.initAgentDemoEvent() {
    val views = bindAgentDemoViews()

    views.runButton.setOnClickListener {
        runDeepSeekAgent(views)
    }

    views.openProviderButton.setOnClickListener {
        val intent = packageManager.getLaunchIntentForPackage("com.fde.gallery3d")
        if (intent == null) {
            Toast.makeText(
                this,
                "未安装 ${AppFunctionsClient.PROVIDER_PACKAGE}，" +
                    "请先执行 ./gw.sh :funcprovider:installDebug",
                Toast.LENGTH_LONG,
            ).show()
        } else {
            startActivity(intent)
        }
    }
}

/**
 * Runs one agent turn on [lifecycleScope] so the network call and the cross-app function call stay
 * off the main thread and are cancelled with the activity.
 */
private fun MainActivity.runDeepSeekAgent(views: AgentDemoViews) {
    val apiKey = views.keyInput.text?.toString()?.trim().orEmpty()
    val prompt = views.promptInput.text?.toString()?.trim().orEmpty()

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
            when (val result = agent.run(prompt)) {
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

private const val TAG = "AgentDemo"
