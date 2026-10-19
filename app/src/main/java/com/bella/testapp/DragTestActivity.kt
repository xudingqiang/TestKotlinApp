package com.bella.testapp

import android.content.ClipData
import android.content.ClipDescription
import android.view.DragEvent
import android.graphics.Canvas
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.util.Log

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class DragTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_drag_test)

        val tvSource = findViewById<TextView>(R.id.tvSource)
        val layoutTarget = findViewById<LinearLayout>(R.id.layoutTarget)

        // 1. 设置源组件的长按事件，触发拖拽
        tvSource.setOnLongClickListener { v ->
            // 创建携带的数据，这里以文本为例
            val item = ClipData.Item(tvSource.text)
//            val dragData = ClipData(
//                tvSource.text.toString(),
//                arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
//                item
//            )
            val path = "/storage/emulated/0/Pictures/aa.png"
            val fileUri : Uri = Uri.parse(path);

            val dragData = ClipData.newUri(
                contentResolver,
                fileUri.lastPathSegment ?: "file",
                fileUri
            )

            // 创建拖拽阴影（跟随手指移动的图像）
            val myShadow = View.DragShadowBuilder(v)

            // 开始拖拽
            v.startDragAndDrop(dragData, myShadow, v, 0)
            true
        }

        

        tvSource.setOnDragListener { v, event ->
            when (event.action) {
                android.view.DragEvent.ACTION_DRAG_STARTED -> true

                android.view.DragEvent.ACTION_DRAG_ENDED -> {
                    Log.w("DragTestActivity", "Drag ended  taskId: "+taskId);
                    // event.result 为 true 表示被其他 App 成功接收并 Drop
                    // event.result 为 false 表示用户在中途取消了，或者拖到了不支持的地方
//                    if (event.result) {
//                        // 跨应用拖拽成功：执行你的删除逻辑
//                        val sourceView = event.localState as? View
//                        val parentGroup = sourceView?.parent as? ViewGroup
//
//                        // 从原布局中移除（删除原组件）
//                        parentGroup?.removeView(sourceView)
//
//                        // TODO: 同步删除你的本地数据源（如 Database / List 中的数据）
//                    } else {
//                        // 拖拽失败/取消，如果需要可以恢复原组件的可见性或状态
//                        v.visibility = View.VISIBLE
//                    }
                    true
                }
                else -> false
            }
        }

        // 2. 设置目标组件的拖拽监听器
        layoutTarget.setOnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    // 检查是否能处理此类型的数据
                    event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    // 进入目标区域时改变背景色提示用户（可选）
                    layoutTarget.setBackgroundColor(0xFFB2DFDB.toInt())
                    true
                }
                DragEvent.ACTION_DRAG_LOCATION -> {
                    // 拖拽中持续触发
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    // 离开目标区域恢复背景色
                    layoutTarget.setBackgroundColor(0xFFE0E0E0.toInt())
                    true
                }
                DragEvent.ACTION_DROP -> {
                    // 3. 手指松开，完成放置
                    layoutTarget.setBackgroundColor(0xFFE0E0E0.toInt())

                    // 获取传递过来的源 View
                    val sourceView = event.localState as? View

                    if (sourceView != null) {
                        // 从原父布局中移除源组件（实现“删除原组件”）
                        val parentGroup = sourceView.parent as? ViewGroup
                        parentGroup?.removeView(sourceView)

                        // 在新组件中显示（可以重新创建一个新 View，或者直接把原 View 添加进去）
                        // 这里我们直接将原 View 添加到目标容器中，实现外观的转移
                        layoutTarget.removeAllViews() // 如果目标区域只允许放一个，先清空提示

                        // 确保 sourceView 的 LayoutParams 正确
                        val params = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        sourceView.layoutParams = params

                        // 添加到目标容器
                        layoutTarget.addView(sourceView)
                    }
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    // 拖拽结束（无论成功与否都可以做清理工作）
                    true
                }
                else -> false
            }
        }
    }

}