# DeepSeek + AppFunctions 跨应用调用 Demo

用 **DeepSeek 的函数调用（Function Calling）**决定“该调用哪个能力”，再由
**Android AppFunctions（`android.app.appfunctions`）**执行**另一个应用**里的 AppFunction。

本 Demo 的目标应用是设备自带的相册 `com.fde.gallery3d`，调用它的 **`openImage`** 函数
（参数 `path`、`title`），由相册打开图片预览。

```
MainActivity 输入自然语言 + 图片路径
        │
        ▼
DeepSeek /chat/completions（tools 由 AppFunctions 元数据动态生成）
        │  返回 tool_calls: openImage{path="/mnt/sdcard/Pictures/aa.png", title="aa"}
        ▼
android.app.appfunctions.AppFunctionManager.executeAppFunction(...)
        │  跨应用 → 系统 AppFunctions 服务
        ▼
com.fde.gallery3d / com.fde.gallery3d.appfunctions.MediaAppFunctionsService
        │  openImage(path, title) → startActivity(图片预览) → 返回 status="ok"
        ▼
结果作为 tool 消息回灌 DeepSeek → 最终中文回答
```

## 设备上真实存在的 AppFunction

```
$ adb shell cmd app_function list-app-functions
com.fde.gallery3d:
  serviceName : com.fde.gallery3d.appfunctions.MediaAppFunctionsService
  functionId  : openImage   (schemaCategory=media, schemaVersion=1, enabledByDefault=true)
  functionId  : playVideo
com.android.permissioncontroller:
  functionId  : getPermissionsDeviceState
```

相册服务端（反编译确认）读取请求 `GenericDocument` 的 `path` 与 `title` 两个字符串属性，
所以调用参数就是：

```json
{"path": "/mnt/sdcard/Pictures/aa.png", "title": "aa"}
```

## 关键文件

| 文件 | 作用 |
| --- | --- |
| `app/src/main/java/com/bella/testapp/appfunctions/DeepSeekAgent.kt` | DeepSeek 工具声明（由 AppFunctions 元数据动态生成）、`tool_calls` 循环、结果回灌 |
| `app/src/main/java/com/bella/testapp/appfunctions/GalleryAppFunctionsClient.kt` | 构造 `GenericDocument` 参数并调用平台 `AppFunctionManager.executeAppFunction` |
| `app/src/main/java/com/bella/testapp/appfunctions/BaseDemoAppFunctionService.kt` | 本应用也声明一个入口点 —— 这是生成 `$AggregatedAppFunctionInventory_Impl` 的前提 |
| `app/src/main/java/com/bella/testapp/MainActivityAgentDemo.kt` | Demo 界面，只以扩展函数挂到 `MainActivity`，便于整块删除 |

`app/build.gradle` 里与 AppFunctions 相关的两处配置都必须保留：

```groovy
id 'com.google.devtools.ksp'          // KSP >= 2.3.x（AGP 9 built-in Kotlin 兼容）
ksp { arg("appfunctions:aggregateAppFunctions", "true") }
```

## 运行

```bash
./gw.sh :app:assembleDebug
./gw.sh :app:installDebug        # 或 adb install -r app/build/outputs/apk/debug/app-debug.apk
```

打开 app，滚到底部：

1. 填 DeepSeek API Key（`sk-...`）
2. 图片路径默认 `/mnt/sdcard/Pictures/aa.png`（可改）
3. 输入“打开 /mnt/sdcard/Pictures/aa.png 这张图片预览”
4. 点「运行智能体」，日志区会打印：模型请求的 tool_call → AppFunctions 调用结果 → 最终回答

## ⚠️ 关键限制：`EXECUTE_APP_FUNCTIONS` 是特权权限

调用**其他应用**的 AppFunction 需要 `android.permission.EXECUTE_APP_FUNCTIONS`。
在 Android 17（API 37）上它是 **known-signer / 特权**权限：

- `pm grant` 会报 `not a changeable permission type`；
- 平台在 `executeAppFunction` 里直接返回
  `AppFunctionException: Caller does not have permission to execute the appfunction (code 1000)`；
- 只有 **系统签名**（或证书在 `@config_executeAppFunctionsKnownSigners` 白名单里）的应用
  ——也就是设备内置的 Agent 角色——才会被授予。

因此：

- 普通 `adb install` 的 APK：**代码路径完整可跑，跨应用那一步会被平台拒绝**。
  Demo 会在日志里明确显示“缺少 EXECUTE_APP_FUNCTIONS（特权/系统签名权限）”，不会静默失败。
- 要让相册真的被调用，需要把本应用放到以下任一位置：
  1. 用平台签名重新签名（`signapk` + platform key），或
  2. 作为 `priv-app` 预置进系统镜像，并把签名证书加入
     `config_executeAppFunctionsKnownSigners`，或
  3. 把这段调用逻辑移植进 FDE 自带的系统 Agent 应用。

已在本机验证（OpenFDE arm64 / API 37）：
`AppFunctions 可用: 是`、`目标应用 com.fde.gallery3d`、
`EXECUTE_APP_FUNCTIONS: 未授予`，测试输出正是上述权限拒绝原因。

## 测试

```bash
# 主机侧：目标包名/函数 ID/参数名契约
./gw.sh :app:testDebugUnitTest

# 设备侧：参数序列化往返、相册已安装、真实跨应用调用（按平台结果断言/报告）
./gw.sh :app:connectedDebugAndroidTest
```

> `connectedDebugAndroidTest` 需要 UTP 写 `~/.android`；在只读 HOME 的沙箱里可改用：
> ```bash
> adb install -r app/build/outputs/apk/debug/app-debug.apk
> adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
> adb shell am instrument -w -r \
>   -e class com.bella.testapp.appfunctions.AppFunctionsInstrumentedTest \
>   com.bella.testapp.test/androidx.test.runner.AndroidJUnitRunner
> ```

## 构建环境说明（AGP 9）

- AGP 9 默认开启 **built-in Kotlin**，所以不再 apply `org.jetbrains.kotlin.android`；
  `android.kotlinOptions {}` 已移除（`jvmTarget` 默认取 `compileOptions.targetCompatibility`）。
- `org.oremif:deepseek-kotlin:0.5.0` 依赖 `kotlin-stdlib 2.4.20`，根 `build.gradle` 把
  KGP 提到 **2.4.20**（AGP 9.0 基线为 2.2.10）。
- AppFunctions 注解处理器是 KSP：**KSP ≥ 2.3.x** 才与 AGP 9 的 built-in Kotlin 兼容。
- `gw.sh` / `gradle-home/` / `gradle-dist/` / `debug.keystore` 是为只读 HOME 的沙箱准备的
  （已 gitignore），普通开发机直接用 `./gradlew` 即可。
