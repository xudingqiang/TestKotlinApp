package com.bella.testapp.appfunctions

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import org.oremif.deepseek.api.chat
import org.oremif.deepseek.client.DeepSeekClient
import org.oremif.deepseek.models.ChatModel
import org.oremif.deepseek.models.FunctionRequest
import org.oremif.deepseek.models.SystemMessage
import org.oremif.deepseek.models.Tool
import org.oremif.deepseek.models.ToolCallType
import org.oremif.deepseek.models.UserMessage
import org.oremif.deepseek.models.argumentsAsJsonOrNull
import org.oremif.deepseek.models.chatCompletionParams

/**
 * The bridge this demo is about: DeepSeek decides **what** to call, AppFunctions decides **how**
 * the call crosses the app boundary.
 *
 * Flow:
 * 1. The tool declarations in [AGENT_TOOLS] are registered as `tools` on the DeepSeek
 *    chat-completions request, so the model can answer with a `tool_calls` message instead of prose.
 * 2. When the model asks for `add_numbers`, this class executes the matching AppFunction that is
 *    published by the separate target app (`com.bella.funcprovider`) - i.e. a real cross-app call.
 * 3. The tool result is sent back to the model as a `tool` message, which then produces the final
 *    natural-language answer shown in the UI.
 */
class DeepSeekAgent(
    private val context: Context,
    private val apiKey: String,
    private val log: (String) -> Unit,
) {

    companion object {
        /** JSON Schema for the `add_numbers` tool, in OpenAI/DeepSeek function-tool format. */
        private val ADD_SCHEMA = buildJsonObject {
            put("type", "object")
            putJsonObject("properties") {
                putJsonObject("a") {
                    put("type", "string")
                    put("description", "第一个加数")
                }
                putJsonObject("b") {
                    put("type", "string")
                    put("description", "第二个加数")
                }
            }
            putJsonArray("required") {
                add(kotlinx.serialization.json.JsonPrimitive("path"))
                add(kotlinx.serialization.json.JsonPrimitive("title"))
            }
        }

        /**
         * Tools exposed to the model. The schema is the tool's contract with the LLM; the matching
         * AppFunction is what actually runs, in the target app's process.
         */
        val AGENT_TOOLS: List<AgentTool> = listOf(
            AgentTool(
                key = "openImage",
                description = "计算两个整数之和。结果由另一个应用通过 AppFunctions 计算。",
                openAiSchema = ADD_SCHEMA.toString(),
            ),
        )

        private val json = Json { ignoreUnknownKeys = true }

        private fun toDeepSeekTools(): List<Tool> = AGENT_TOOLS.map { tool ->
            Tool(
                type = ToolCallType.FUNCTION,
                function = FunctionRequest(
                    name = tool.key,
                    description = tool.description,
                    parameters = json.parseToJsonElement(tool.openAiSchema).jsonObject,
                ),
            )
        }
    }

    private val client = DeepSeekClient(apiKey) {
        chatCompletionTimeout(120_000)
    }

    private val appFunctions = AppFunctionsClient(context)

    /**
     * Runs one agent turn for [prompt]: ask the model, execute whatever AppFunction it requested,
     * feed the result back, and return the model's final answer.
     *
     * @param prompt natural-language request typed by the user.
     * @param maxToolRounds upper bound on tool-call round trips, to keep the demo bounded.
     */
    suspend fun run(prompt: String, maxToolRounds: Int = 3): AgentRunResult {
        val messages = mutableListOf<org.oremif.deepseek.models.ChatMessage>(
            SystemMessage(
                "你是一个 Android 智能体。需要做算术时,必须调用 add_numbers 工具," +
                    "不要自己心算。工具返回后再用一句中文总结结果。",
            ),
            UserMessage(content = prompt),
        )

        log("→ POST https://api.deepseek.com/chat/completions (model=deepseek-flash)")
        log("→ tools: ${AGENT_TOOLS.joinToString { it.key }}")

        var rounds = 0
        while (true) {
            val completion = client.chat(
                params = chatCompletionParams {
                    model = ChatModel.DEEPSEEK_FLASH
                    temperature = 0.0
                    tools = toDeepSeekTools()
                },
                messages = messages,
            )

            val choice = completion.choices.firstOrNull()
                ?: return AgentRunResult.Failure("DeepSeek 没有返回任何 choice。")
            val assistant = choice.message
            val toolCalls = assistant.toolCalls

            if (toolCalls.isNullOrEmpty()) {
                val answer = assistant.content.orEmpty()
                log("← 模型直接回答(未调用工具)")
                return AgentRunResult.Answered(answer, messages.size)
            }

            messages += assistant
            for (call in toolCalls) {
                val name = call.function.name
                val args = call.function.argumentsAsJsonOrNull()
                log("← 模型请求调用工具: $name  参数: ${call.function.arguments}")

                val resultText = when (name) {
                    "add_numbers" -> {
                        val a = args?.get("a")?.jsonPrimitive?.intOrNull
                        val b = args?.get("b")?.jsonPrimitive?.intOrNull
                        if (a == null || b == null) {
                            "参数错误：需要整数 a 和 b"
                        } else {
                            when (val outcome = appFunctions.callAdd(a, b)) {
                                is AgentFunctionResult.Success -> {
                                    log("  ↳ AppFunctions(${AppFunctionsClient.PROVIDER_PACKAGE}) 返回 ${outcome.raw}")
                                    outcome.displayText
                                }

                                is AgentFunctionResult.Failure -> {
                                    log("  ↳ ${outcome.displayText}")
                                    outcome.displayText
                                }
                            }
                        }
                    }

                    else -> "未知工具: $name"
                }

                messages += org.oremif.deepseek.models.ToolMessage(
                    content = resultText,
                    toolCallId = call.id,
                )
            }

            rounds++
            if (rounds >= maxToolRounds) {
                return AgentRunResult.Failure("工具调用轮数超过上限($maxToolRounds)。")
            }
        }
    }

    /** Releases the underlying HTTP client. */
    fun close() = client.close()
}

/** Outcome of one [DeepSeekAgent.run] call. */
sealed interface AgentRunResult {
    data class Answered(val text: String, val messageCount: Int) : AgentRunResult
    data class Failure(val message: String) : AgentRunResult
}
