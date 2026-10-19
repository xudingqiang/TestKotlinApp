package com.bella.funcprovider

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Minimal launcher activity for the target app. It only displays the AppFunction published by
 * this app and the last value it computed, so the demo host app has something to verify against.
 */
class ProviderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provider)

        findViewById<TextView>(R.id.tvProviderInfo).text = buildString {
            append("本应用是一个 AppFunctions 提供方(target app)。\n")
            append("发布包名: $packageName\n")
            append("服务类: com.bella.funcprovider.DemoAppFunctionService\n")
            append("函数 ID: ${DemoAppFunctionService.FUNCTION_ID_ADD}\n\n")
            append("函数签名: add(a: Int, b: Int): Int\n")
            append("上次计算结果: ")
            append(
                if (BaseDemoAppFunctionService.lastResult == Int.MIN_VALUE) {
                    "尚未被调用"
                } else {
                    BaseDemoAppFunctionService.lastResult.toString()
                },
            )
        }
    }
}
