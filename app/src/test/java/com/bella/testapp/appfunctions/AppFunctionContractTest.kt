package com.bella.testapp.appfunctions

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Host-side checks for the two contracts the demo cannot get wrong, without needing a device or an
 * API key:
 *  - the JSON Schema handed to DeepSeek is a valid OpenAI/DeepSeek function-tool schema, and
 *  - the function id the agent asks the platform for matches the one the target app published.
 *
 * The AppFunctions serialization round-trip itself is covered by
 * `AppFunctionsInstrumentedTest` because `AppFunctionData` needs a real Android runtime.
 */
class AppFunctionContractTest {

    @Test
    fun addFunctionId_matchesTheProviderService() {
        assertEquals(
            "com.bella.funcprovider.BaseDemoAppFunctionService#add",
            AppFunctionsClient.FUNC_ADD,
        )
        assertEquals("com.bella.funcprovider", AppFunctionsClient.PROVIDER_PACKAGE)
    }

    @Test
    fun deepSeekToolSchema_isAnOpenAiFunctionSchema() {
        val tool = DeepSeekAgent.AGENT_TOOLS.single()
        assertEquals("add_numbers", tool.key)

        val schema = Json.parseToJsonElement(tool.openAiSchema) as JsonObject
        assertEquals("object", (schema["type"] as JsonPrimitive).content)

        val properties = schema["properties"] as JsonObject
        assertTrue(properties.containsKey("a"))
        assertTrue(properties.containsKey("b"))
        assertEquals("integer", ((properties["a"] as JsonObject)["type"] as JsonPrimitive).content)
        assertEquals("integer", ((properties["b"] as JsonObject)["type"] as JsonPrimitive).content)

        val required = (schema["required"] as JsonArray).map { (it as JsonPrimitive).content }
        assertEquals(listOf("a", "b"), required)
    }
}
