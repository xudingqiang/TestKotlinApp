package com.bella.testapp.appfunctions

import android.content.Context
import androidx.appfunctions.AppFunctionManager
import androidx.appfunctions.AppFunctionSearchSpec
import androidx.appfunctions.metadata.AppFunctionAllOfTypeMetadata
import androidx.appfunctions.metadata.AppFunctionArrayTypeMetadata
import androidx.appfunctions.metadata.AppFunctionBooleanTypeMetadata
import androidx.appfunctions.metadata.AppFunctionDataTypeMetadata
import androidx.appfunctions.metadata.AppFunctionDoubleTypeMetadata
import androidx.appfunctions.metadata.AppFunctionFloatTypeMetadata
import androidx.appfunctions.metadata.AppFunctionIntTypeMetadata
import androidx.appfunctions.metadata.AppFunctionLongTypeMetadata
import androidx.appfunctions.metadata.AppFunctionMetadata
import androidx.appfunctions.metadata.AppFunctionObjectTypeMetadata
import androidx.appfunctions.metadata.AppFunctionOneOfTypeMetadata
import androidx.appfunctions.metadata.AppFunctionStringTypeMetadata
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import org.oremif.deepseek.api.chat
import org.oremif.deepseek.client.DeepSeekClient
import org.oremif.deepseek.models.ChatMessage
import org.oremif.deepseek.models.ChatModel
import org.oremif.deepseek.models.FunctionRequest
import org.oremif.deepseek.models.SystemMessage
import org.oremif.deepseek.models.Tool
import org.oremif.deepseek.models.ToolCallType
import org.oremif.deepseek.models.ToolMessage
import org.oremif.deepseek.models.UserMessage
import org.oremif.deepseek.models.argumentsAsJsonOrNull
import org.oremif.deepseek.models.chatCompletionParams

/**
 * The bridge this demo is about: **DeepSeek decides what to call, AppFunctions performs the
 * cross-app call.**
 *
 * The DeepSeek tools are not hard-coded: they are generated from the AppFunctions metadata the
 * platform has indexed for [GalleryAppFunctionsClient.GALLERY_PACKAGE]
 * (`cmd app_function list-app-functions`). If discovery is unavailable — no AppFunctions on the
 * device, or no permission to query them — the agent falls back to the known `openImage` schema so
 * the model → tool-call path is still demonstrated.
 */
class DeepSeekAgent(
    private val context: Context,
    private val apiKey: String,
    private val log: (String) -> Unit,
) {

    companion object {
        /** Target package this demo drives. */
        private const val TARGET_PACKAGE = GalleryAppFunctionsClient.GALLERY_PACKAGE

        /**
         * Schema of `com.fde.gallery3d#openImage`, taken from the gallery's service, which reads
         * `getPropertyString("path")` and `getPropertyString("title")`.
         */
        private const val OPEN_IMAGE_SCHEMA = """
            {
              "type": "object",
              "properties": {
                "path": {
                  "type": "string",
                  "description": "图片的绝对路径，例如 /mnt/sdcard/Pictures/aa.png"
                },
                "title": {
                  "type": "string",
                  "description": "图片显示标题，例如 aa"
                }
              },
              "required": ["path", "title"]
            }
        """

        private val json = Json { ignoreUnknownKeys = true }
    }

    /** One AppFunction exposed to the model as a DeepSeek tool. */
    data class DiscoveredTool(
        val functionId: String,
        val packageName: String,
        val description: String,
        val schema: String,
    )

    private val gallery = GalleryAppFunctionsClient(context)

    private val client = DeepSeekClient(apiKey) {
        chatCompletionTimeout(120_000)
    }

    /** True when a cross-app AppFunction call can actually succeed on this install. */
    val canExecuteCrossApp: Boolean
        get() = gallery.isSupported && gallery.hasExecutePermission()

    /**
     * Asks the platform which AppFunctions [TARGET_PACKAGE] publishes and converts their metadata
     * into [DiscoveredTool]s.
     */
    suspend fun discoverTools(): List<DiscoveredTool> {
        if (!gallery.isSupported) return emptyList()

        val manager = AppFunctionManager.getInstance(context) ?: return emptyList()
        return try {
            manager.searchAppFunctions(
                AppFunctionSearchSpec(packageNames = setOf(TARGET_PACKAGE)),
            ).mapNotNull { metadata -> metadata.toDiscoveredTool() }
        } catch (t: Throwable) {
            log("发现 AppFunctions 失败（改用内置 schema）: ${t.javaClass.simpleName}: ${t.message}")
            emptyList()
        }
    }

    /**
     * Runs one agent turn for [prompt]:
     * 1. discover the target app's AppFunctions and expose them as DeepSeek tools,
     * 2. let the model emit `tool_calls`,
     * 3. execute the requested AppFunction in the target app,
     * 4. feed the result back and return the final natural-language answer.
     */
    suspend fun run(
        prompt: String,
        maxToolRounds: Int = 3,
        fallbackPath: String? = null,
    ): AgentRunResult {
        val tools = discoverTools().ifEmpty {
            listOf(
                DiscoveredTool(
                    functionId = GalleryAppFunctionsClient.FUNC_OPEN_IMAGE,
                    packageName = TARGET_PACKAGE,
                    description = "打开指定路径的图片（由相册应用执行）",
                    schema = OPEN_IMAGE_SCHEMA,
                ),
            )
        }

        log(
            "→ 发现 ${tools.size} 个 AppFunction: " +
                tools.joinToString { "${it.packageName}#${it.functionId}" },
        )
        log(
            "→ 跨应用执行权限(EXECUTE_APP_FUNCTIONS): " +
                if (canExecuteCrossApp) "已授予" else "未授予",
        )

        val defaultPath = fallbackPath ?: GalleryAppFunctionsClient.DEFAULT_IMAGE_PATH
        val messages = mutableListOf<ChatMessage>(
            SystemMessage(
                "你是一个 Android 智能体。用户想打开图片时必须调用工具，不要自己编造路径。" +
                    "path 必须是设备上的绝对路径；如果用户没有给路径，使用 $defaultPath。" +
                    "调用工具后再用一句中文总结。",
            ),
            UserMessage(content = prompt),
        )

        log("→ POST https://api.deepseek.com/chat/completions (model=deepseek-flash)")
        var rounds = 0
        while (true) {
            val completion = client.chat(
                params = chatCompletionParams {
                    model = ChatModel.DEEPSEEK_FLASH
                    temperature = 0.0
                    this.tools = tools.map { it.toDeepSeekTool() }
                },
                messages = messages,
            )

            val choice = completion.choices.firstOrNull()
                ?: return AgentRunResult.Failure("DeepSeek 没有返回任何 choice。")
            val assistant = choice.message
            val toolCalls = assistant.toolCalls

            if (toolCalls.isNullOrEmpty()) {
                log("← 模型直接回答（未调用工具）")
                return AgentRunResult.Answered(assistant.content.orEmpty(), messages.size)
            }

            messages += assistant
            for (call in toolCalls) {
                val name = call.function.name
                val args = call.function.argumentsAsJsonOrNull()
                log("← 模型请求调用: $name  参数: ${call.function.arguments}")

                val tool = tools.firstOrNull { it.functionId == name }
                val resultText = when {
                    tool == null -> "未知工具: $name"

                    tool.functionId == GalleryAppFunctionsClient.FUNC_OPEN_IMAGE -> {
                        val path = args?.get("path")?.let { (it as? JsonPrimitive)?.content }
                        val title = args?.get("title")?.let { (it as? JsonPrimitive)?.content }
                            ?: path?.substringAfterLast('/')?.substringBeforeLast('.')
                            ?: ""
                        if (path.isNullOrBlank()) {
                            "参数错误：openImage 需要绝对路径 path"
                        } else {
                            when (val outcome = gallery.openImage(path, title)) {
                                is AgentFunctionResult.Success -> {
                                    log("  ↳ ${outcome.displayText}")
                                    outcome.displayText
                                }

                                is AgentFunctionResult.Failure -> {
                                    log("  ↳ ${outcome.displayText}")
                                    outcome.displayText
                                }
                            }
                        }
                    }

                    else -> "暂未实现: ${tool.functionId}"
                }

                messages += ToolMessage(content = resultText, toolCallId = call.id)
            }

            rounds++
            if (rounds >= maxToolRounds) {
                return AgentRunResult.Failure("工具调用轮数超过上限($maxToolRounds)。")
            }
        }
    }

    fun close() = client.close()

    private fun DiscoveredTool.toDeepSeekTool(): Tool = Tool(
        type = ToolCallType.FUNCTION,
        function = FunctionRequest(
            name = functionId,
            description = description.ifBlank { "$packageName#$functionId" },
            parameters = json.parseToJsonElement(schema) as JsonObject,
        ),
    )

    private fun AppFunctionMetadata.toDiscoveredTool(): DiscoveredTool = DiscoveredTool(
        functionId = id,
        packageName = packageName,
        description = description.ifBlank { "$packageName#$id" },
        schema = objectSchemaFor(parameters.map { it.name to it.dataType }, parameters
            .filter { it.isRequired }
            .map { it.name }).toString(),
    )

    private fun objectSchemaFor(
        properties: List<Pair<String, AppFunctionDataTypeMetadata>>,
        required: List<String>,
    ): JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            for ((name, type) in properties) {
                put(name, type.toJsonSchema())
            }
        }
        if (required.isNotEmpty()) {
            putJsonArray("required") { required.forEach { add(JsonPrimitive(it)) } }
        }
    }

    private fun AppFunctionDataTypeMetadata.toJsonSchema(): JsonObject = when (this) {
        is AppFunctionObjectTypeMetadata -> buildJsonObject {
            put("type", "object")
            putJsonObject("properties") {
                for ((name, type) in properties) {
                    put(name, type.toJsonSchema())
                }
            }
            if (required.isNotEmpty()) {
                putJsonArray("required") { required.forEach { add(JsonPrimitive(it)) } }
            }
            if (description.isNotBlank()) put("description", description)
        }

        is AppFunctionArrayTypeMetadata -> buildJsonObject {
            put("type", "array")
            put("items", itemType.toJsonSchema())
        }

        is AppFunctionStringTypeMetadata -> buildJsonObject { put("type", "string") }
        is AppFunctionIntTypeMetadata -> buildJsonObject { put("type", "integer") }
        is AppFunctionLongTypeMetadata -> buildJsonObject { put("type", "integer") }
        is AppFunctionFloatTypeMetadata -> buildJsonObject { put("type", "number") }
        is AppFunctionDoubleTypeMetadata -> buildJsonObject { put("type", "number") }
        is AppFunctionBooleanTypeMetadata -> buildJsonObject { put("type", "boolean") }

        // Unions cannot be expressed as one JSON-Schema primitive; the model only needs to know
        // that something has to be passed, so describe them as objects.
        is AppFunctionOneOfTypeMetadata -> buildJsonObject { put("type", "object") }
        is AppFunctionAllOfTypeMetadata -> buildJsonObject { put("type", "object") }

        else -> buildJsonObject { put("type", "string") }
    }
}

/** Outcome of one [DeepSeekAgent.run] call. */
sealed interface AgentRunResult {
    data class Answered(val text: String, val messageCount: Int) : AgentRunResult
    data class Failure(val message: String) : AgentRunResult
}
