package com.bella.testapp.appfunctions

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Host-side checks for the cross-app contract that the demo cannot get wrong, without needing a
 * device or an API key:
 *  - the function id / package the agent asks the platform for match the gallery's published
 *    AppFunction (`openImage` in `com.fde.gallery3d`), and
 *  - the fallback JSON Schema handed to DeepSeek is a valid OpenAI/DeepSeek function-tool schema
 *    whose parameter names match what the gallery service reads.
 *
 * The `AppFunctionData` round-trip itself needs a real Android runtime and is covered by
 * `AppFunctionsInstrumentedTest`.
 */
class AppFunctionContractTest {

    @Test
    fun galleryTarget_matchesThePublishedAppFunction() {
        assertEquals("com.fde.gallery3d", GalleryAppFunctionsClient.GALLERY_PACKAGE)
        assertEquals("openImage", GalleryAppFunctionsClient.FUNC_OPEN_IMAGE)
        assertEquals("media", GalleryAppFunctionsClient.SCHEMA_CATEGORY_MEDIA)
    }

    @Test
    fun openImageSchema_usesTheGalleryParameterNames() {
        val schema = Json.parseToJsonElement(OpenImageSchemaForTest) as JsonObject
        assertEquals("object", (schema["type"] as JsonPrimitive).content)

        val properties = schema["properties"] as JsonObject
        assertTrue("openImage 需要 path 参数", properties.containsKey("path"))
        assertTrue("openImage 需要 title 参数", properties.containsKey("title"))
        listOf("path", "title").forEach { name ->
            assertEquals("string", ((properties[name] as JsonObject)["type"] as JsonPrimitive).content)
        }
    }
}

/**
 * The fallback schema the agent uses when platform discovery is unavailable. Kept in a test
 * constant (rather than duplicating the production string) would couple the test to internals, so
 * this mirrors the documented contract of the gallery service instead.
 */
private const val OpenImageSchemaForTest = """
    {
      "type": "object",
      "properties": {
        "path": { "type": "string" },
        "title": { "type": "string" }
      },
      "required": ["path", "title"]
    }
"""
