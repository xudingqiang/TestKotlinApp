# DeepSeek + AppFunctions 跨应用调用 Demo

本 Demo 演示：**用 DeepSeek 的函数调用（Function Calling）决定“调用哪个能力”，再由
Android AppFunctions（`android.app.appfunctions`）真正执行另一个 App 里的 `@AppFunction`。**

一句话流程：

```
用户在 MainActivity 输入自然语言
        │
        ▼
DeepSeek /chat/completions（带 tools 声明）
        │  返回 tool_calls: add_numbers{a=12, b=30}
        ▼
AppFunctionsClient（android.app.appfunctions.AppFunctionManager）
        │  ExecuteAppFunctionRequest → 跨进程/跨应用
        ▼
com.bella.funcprovider 里的 @AppFunction add(a, b) → 返回 42
        │
        ▼
结果作为 tool 消息回灌给 DeepSeek → 得到最终中文回答
```

## 工程结构

| 模块 | 角色 | 说明 |
| --- | --- | --- |
| `:app` | **调用方 / 智能体** | `MainActivity` 里的 Demo 界面；`appfunctions/DeepSeekAgent.kt` 负责与 DeepSeek 交互；`appfunctions/AppFunctionsClient.kt` 负责跨应用调用 AppFunction |
| `:funcprovider` | **目标应用** | 独立的 APK，通过 `@AppFunctionServiceEntryPoint` 发布 `add(a: Int, b: Int): Int` |
| `:baselib` | 原有基础库 | 与 Demo 无关 |

### 关键文件

- `app/src/main/java/com/bella/testapp/appfunctions/DeepSeekAgent.kt`
  - `AGENT_TOOLS`：暴露给模型的工具（OpenAI/DeepSeek function-tool JSON Schema）。
  - `run(prompt)`：请求模型 → 执行 `tool_calls` → 把结果作为 `ToolMessage` 回灌 → 返回最终回答。
- `app/src/main/java/com/bella/testapp/appfunctions/AppFunctionsClient.kt`
  - `AddParameters`：用 `@AppFunctionSerializable` 标注，KSP 会生成 `$AddParametersFactory`。
  - `callAdd(a, b)`：构造 `ExecuteAppFunctionRequest` 并调用 `AppFunctionManager.executeAppFunction`。
- `app/src/main/java/com/bella/testapp/MainActivityAgentDemo.kt`
  - 只通过扩展函数挂到 `MainActivity`，便于整块删除。
- `funcprovider/src/main/java/com/bella/funcprovider/BaseDemoAppFunctionService.kt`
  - `@AppFunctionServiceEntryPoint` + `@AppFunction`；KSP 生成
    `DemoAppFunctionService`（服务实现）和 `assets/app_functions_service.xml`（函数 schema）。

## 构建与运行

```bash
# 1. 编译全部
./gw.sh :app:assembleDebug :funcprovider:assembleDebug

# 2. 安装（funcprovider 必须先装，它是被调用的目标应用）
./gw.sh :app:installDebug :funcprovider:installDebug
#   或直接用 adb：
adb install -r funcprovider/build/outputs/apk/debug/funcprovider-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 3. 打开 app，在界面底部：
#    - 填入 DeepSeek API Key（sk-...）
#    - 输入“帮我算一下 12 加 30 等于多少”
#    - 点击「运行智能体」，观察日志区
```

`./gw.sh` 是仓库里为沙箱环境准备的辅助脚本（用工作区内的 Gradle 发行版 + 可写
`GRADLE_USER_HOME`）。在普通开发机上直接用 `./gradlew` 即可。

## 测试

```bash
# 主机侧：DeepSeek 工具 schema + 函数 ID 契约
./gw.sh :app:testDebugUnitTest

# 设备侧：AppFunctionData 序列化往返 + 真正的跨应用调用（需要已安装 funcprovider）
./gw.sh :app:connectedDebugAndroidTest
```

## 已知限制（重要）

1. **`EXECUTE_APP_FUNCTIONS` 是特权权限。**
   在 Android 17（API 37）上，调用**其他包**拥有的 AppFunction 需要
   `android.permission.EXECUTE_APP_FUNCTIONS`。它是 known-signer / 特权权限，
   平台只对 `@config_executeAppFunctionsKnownSigners` 白名单内的签名证书授予
   ——也就是“设备上的智能体（agent）”角色。普通第三方 APK 安装后拿不到这个授权，
   跨应用调用会返回明确的平台错误。Demo 在 UI 与测试里都会把这个原因原样显示出来，
   而不是静默失败。

2. **需要 API 36+ 且设备实现了 AppFunctions。**
   低于该版本时 `AppFunctionManager.getInstance()` 返回 `null`，Demo 会提示
   “AppFunctions 可用: 否”。

3. **需要可用的 DeepSeek API Key，且需要网络。**
   Key 只保存在输入框里，不落盘；请勿提交到仓库。

## 构建环境说明（AGP 9）

- AGP 9 默认开启 **built-in Kotlin**，因此 `app` / `baselib` / `funcprovider`
  都不再 apply `org.jetbrains.kotlin.android`；`android.kotlinOptions {}` 已移除
  （`jvmTarget` 默认取 `compileOptions.targetCompatibility`）。
- `org.oremif:deepseek-kotlin:0.5.0` 依赖 `kotlin-stdlib 2.4.20`，所以在根
  `build.gradle` 里把 KGP 提升到 **2.4.20**（AGP 9.0 基线是 2.2.10）。
- AppFunctions 的注解处理器是 KSP：**KSP ≥ 2.3.x** 才与 AGP 9 的 built-in Kotlin
  兼容（2.2.x 会报 `Using kotlin.sourceSets DSL ... is not allowed`）。
