package com.bella.funcprovider

import androidx.appfunctions.`internal`.AppFunctionInventory
import androidx.appfunctions.metadata.AppFunctionComponentsMetadata
import androidx.appfunctions.metadata.AppFunctionDataTypeMetadata
import androidx.appfunctions.metadata.AppFunctionIntTypeMetadata
import androidx.appfunctions.metadata.AppFunctionParameterMetadata
import androidx.appfunctions.metadata.AppFunctionResponseMetadata
import androidx.appfunctions.metadata.AppFunctionSchemaMetadata
import androidx.appfunctions.metadata.AppFunctionStringTypeMetadata
import androidx.appfunctions.metadata.CompileTimeAppFunctionMetadata
import javax.`annotation`.processing.Generated
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

/**
 * Source Files:
 * BaseDemoAppFunctionService.kt
 */
@Generated("androidx.appfunctions.compiler.AppFunctionCompiler")
public class `$BaseDemoAppFunctionService_AppFunctionInventory` : AppFunctionInventory {
  override val functionIdToMetadataMap: Map<String, CompileTimeAppFunctionMetadata> = mapOf(
    "com.bella.funcprovider.BaseDemoAppFunctionService#add" to ComBellaFuncproviderBaseDemoAppFunctionServiceAddMetadataObject.APP_FUNCTION_METADATA,
    "com.bella.funcprovider.BaseDemoAppFunctionService#openImage" to ComBellaFuncproviderBaseDemoAppFunctionServiceOpenImageMetadataObject.APP_FUNCTION_METADATA,
  )

  private val componentsMetadataDataTypesMap: Map<String, AppFunctionDataTypeMetadata> = mapOf(
    )

  override val componentsMetadata: AppFunctionComponentsMetadata = AppFunctionComponentsMetadata(
          dataTypes = componentsMetadataDataTypesMap
      )

  private object ComBellaFuncproviderBaseDemoAppFunctionServiceAddMetadataObject {
    private val SCHEMA_METADATA: AppFunctionSchemaMetadata? = null

    private val PARAMETER_METADATA_A_PRIMITIVE_DATA_TYPE: AppFunctionIntTypeMetadata =
        AppFunctionIntTypeMetadata(
            isNullable = false,
            description = "",
            enumValues = null,
        )

    private val A_PARAMETER_METADATA: AppFunctionParameterMetadata = AppFunctionParameterMetadata(
            name = "a",
            isRequired = true,
            dataType = PARAMETER_METADATA_A_PRIMITIVE_DATA_TYPE,
            description = "the first addend.",
        )

    private val PARAMETER_METADATA_B_PRIMITIVE_DATA_TYPE: AppFunctionIntTypeMetadata =
        AppFunctionIntTypeMetadata(
            isNullable = false,
            description = "",
            enumValues = null,
        )

    private val B_PARAMETER_METADATA: AppFunctionParameterMetadata = AppFunctionParameterMetadata(
            name = "b",
            isRequired = true,
            dataType = PARAMETER_METADATA_B_PRIMITIVE_DATA_TYPE,
            description = "the second addend.",
        )

    private val PARAMETER_METADATA_LIST: List<AppFunctionParameterMetadata> = listOf(
      A_PARAMETER_METADATA,
      B_PARAMETER_METADATA,
    )

    private val PRIMITIVE_RESPONSE_VALUE_TYPE: AppFunctionIntTypeMetadata =
        AppFunctionIntTypeMetadata(
            isNullable = false,
            description = "",
            enumValues = null,
        )

    private val RESPONSE_METADATA: AppFunctionResponseMetadata = AppFunctionResponseMetadata(
            valueType = PRIMITIVE_RESPONSE_VALUE_TYPE,
            description = "the sum of [a] and [b]."
        )

    public val APP_FUNCTION_METADATA: CompileTimeAppFunctionMetadata =
        CompileTimeAppFunctionMetadata(
            id = "com.bella.funcprovider.BaseDemoAppFunctionService#add",
            isEnabledByDefault = true,
            schema =  SCHEMA_METADATA,
            parameters = PARAMETER_METADATA_LIST,
            response = RESPONSE_METADATA,
        )
  }

  private object ComBellaFuncproviderBaseDemoAppFunctionServiceOpenImageMetadataObject {
    private val SCHEMA_METADATA: AppFunctionSchemaMetadata? = null

    private val PARAMETER_METADATA_A_PRIMITIVE_DATA_TYPE: AppFunctionStringTypeMetadata =
        AppFunctionStringTypeMetadata(
            isNullable = false,
            description = "",
            enumValues = null,
        )

    private val A_PARAMETER_METADATA: AppFunctionParameterMetadata = AppFunctionParameterMetadata(
            name = "a",
            isRequired = true,
            dataType = PARAMETER_METADATA_A_PRIMITIVE_DATA_TYPE,
            description = "",
        )

    private val PARAMETER_METADATA_B_PRIMITIVE_DATA_TYPE: AppFunctionStringTypeMetadata =
        AppFunctionStringTypeMetadata(
            isNullable = false,
            description = "",
            enumValues = null,
        )

    private val B_PARAMETER_METADATA: AppFunctionParameterMetadata = AppFunctionParameterMetadata(
            name = "b",
            isRequired = true,
            dataType = PARAMETER_METADATA_B_PRIMITIVE_DATA_TYPE,
            description = "",
        )

    private val PARAMETER_METADATA_LIST: List<AppFunctionParameterMetadata> = listOf(
      A_PARAMETER_METADATA,
      B_PARAMETER_METADATA,
    )

    private val PRIMITIVE_RESPONSE_VALUE_TYPE: AppFunctionStringTypeMetadata =
        AppFunctionStringTypeMetadata(
            isNullable = false,
            description = "",
            enumValues = null,
        )

    private val RESPONSE_METADATA: AppFunctionResponseMetadata = AppFunctionResponseMetadata(
            valueType = PRIMITIVE_RESPONSE_VALUE_TYPE,
            description = ""
        )

    public val APP_FUNCTION_METADATA: CompileTimeAppFunctionMetadata =
        CompileTimeAppFunctionMetadata(
            id = "com.bella.funcprovider.BaseDemoAppFunctionService#openImage",
            isEnabledByDefault = true,
            schema =  SCHEMA_METADATA,
            parameters = PARAMETER_METADATA_LIST,
            response = RESPONSE_METADATA,
        )
  }
}
