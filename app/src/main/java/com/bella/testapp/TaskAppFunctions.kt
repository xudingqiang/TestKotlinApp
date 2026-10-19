package com.bella.testapp

import androidx.appfunctions.AppFunction
import androidx.appfunctions.AppFunctionContext
import com.bella.testapp.bean.Task
import java.time.LocalDateTime

class TaskAppFunctions {
    /**
     * 创建一个新的待办事项。
     *
     * @param context AppFunction 的执行上下文（系统自动注入）
     * @param title 任务的标题（例如：“下午三点开会”）
     * @return 返回创建好的任务详情对象
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun createTask(
        context: AppFunctionContext,
        title: String
    ): Task {
        // 在这里编写你的本地业务逻辑（如存入 Room 数据库）
        val generatedId = System.currentTimeMillis().toString()

        // 模拟耗时操作
        kotlinx.coroutines.delay(50)

        return Task(
            id = generatedId,
            title = title,
            isCompleted = false
        )
    }

    /**
     * 获取当前所有的待办事项列表。
     *
     * @param context AppFunction 的执行上下文
     * @return 返回待办任务列表
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun getTaskList(
        context: AppFunctionContext
    ): List<Task> {
        // 模拟从数据库或内存中读取数据
        return listOf(
            Task(id = "1", title = "完成 Android 16 AppFunctions 适配", isCompleted = true),
            Task(id = "2", title = "提交代码评审", isCompleted = false)
        )
    }
}