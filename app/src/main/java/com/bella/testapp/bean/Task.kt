package com.bella.testapp.bean

import androidx.appfunctions.AppFunctionSerializable

/**
 * 任务数据模型
 */
@AppFunctionSerializable
data class Task(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false
)