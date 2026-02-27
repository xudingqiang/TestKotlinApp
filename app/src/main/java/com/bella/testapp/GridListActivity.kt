package com.bella.testapp

import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bella.testapp.view.AutoSpacingGridLayout
import com.bella.testapp.view.SelectionView

class GridListActivity : AppCompatActivity() {
    private lateinit var grid: AutoSpacingGridLayout
    private lateinit var selectionView: SelectionView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_grid_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        grid = findViewById(R.id.grid)
        selectionView = findViewById(R.id.selectionView)

        grid.setGridProperties(7,80,100);

        // 添加假数据
        repeat(40) {
            val item = layoutInflater.inflate(R.layout.item_grid, grid, false)
            grid.addView(item)
        }

        setupMouseSelection()

    }


    private fun setupMouseSelection() {

        selectionView.setOnTouchListener { _, event ->

            val isMouse = event.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE
            val isLeft = (event.buttonState  === 0)

            Log.w("Grid","setupMouseSelection isMouse: "+isMouse + " ,event.actionMasked "+event.actionMasked  + " ,BUTTON_PRIMARY "+MotionEvent.BUTTON_PRIMARY)

//            if (!isMouse) return@setOnTouchListener false

            when (event.actionMasked) {

                MotionEvent.ACTION_DOWN -> {
                    if (isLeft) {
                        clearSelection()
                        selectionView.start(event.x, event.y)
                        true
                    } else false
                }

                MotionEvent.ACTION_MOVE -> {
                    selectionView.update(event.x, event.y)
                    updateSelection()
                    true
                }

                MotionEvent.ACTION_UP -> {
                    selectionView.stop()
                    true
                }

                else -> false
            }
        }
    }

    private fun updateSelection() {

        val rect = selectionView.getRect()

        for (i in 0 until grid.childCount) {

            val child = grid.getChildAt(i)

            val childRect = Rect()
            child.getGlobalVisibleRect(childRect)

            val location = IntArray(2)
            selectionView.getLocationInWindow(location)

            childRect.offset(-location[0], -location[1])

            val intersect = RectF.intersects(rect, RectF(childRect))

            child.isSelected = intersect
        }
    }

    private fun clearSelection() {
        for (i in 0 until grid.childCount) {
            grid.getChildAt(i).isSelected = false
        }
    }
}


//    private lateinit var selectionBoxView: SelectionBoxView
//    private lateinit var containerLayout: ViewGroup
//    private val selectedItems = mutableSetOf<View>()
//    private val fileNames = listOf(
//        "back.jpg", "backbar.jpg", "backbar2.jpg",
//        "back-of-sound-card.jpg", "backup.jpg", "battery-check.jpg",
//        "bigmb.jpg", "bios.jpg", "breadboard.jpg"
//    )
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_grid_list)
//
//        containerLayout = findViewById(R.id.container_layout)
//        selectionBoxView = findViewById(R.id.selection_box)
//
//        setupItems()
//        setupTouchListener()
//    }
//
//    private fun setupItems() {
//        fileNames.forEach { fileName ->
//            val item = layoutInflater.inflate(R.layout.item_selectable, containerLayout, false)
//            val textView = item.findViewById<TextView>(R.id.item_text)
//            val iconView = item.findViewById<ImageView>(R.id.item_icon)
//
//            textView.text = fileName
//
//            // 根据文件类型设置不同图标
//            when {
//                fileName.endsWith(".jpg") -> iconView.setImageResource(R.drawable.ic_file)
//                fileName.endsWith(".mp3") -> iconView.setImageResource(R.drawable.ic_file)
//                else -> iconView.setImageResource(R.drawable.ic_file)
//            }
//
//            // 单个点击选择/取消
//            item.setOnClickListener {
//                toggleItemSelection(item)
//            }
//
//            containerLayout.addView(item)
//        }
//    }
//
//    private fun setupTouchListener() {
//        containerLayout.setOnTouchListener { _, event ->
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    // 清除之前的选中
//                    clearSelection()
//                    // 开始选择框
//                    selectionBoxView.startSelection(event.x, event.y)
//                    true
//                }
//                MotionEvent.ACTION_MOVE -> {
//                    selectionBoxView.updateSelection(event.x, event.y)
//                    // 实时更新选中状态
//                    updateSelectionStatus()
//                    true
//                }
//                MotionEvent.ACTION_UP -> {
//                    selectionBoxView.endSelection()
//                    true
//                }
//                else -> false
//            }
//        }
//    }
//
//    private fun updateSelectionStatus() {
//        if (!selectionBoxView.isSelecting()) return
//
//        val selectionRect = selectionBoxView.getSelectionRect()
//
//        for (i in 0 until containerLayout.childCount) {
//            val item = containerLayout.getChildAt(i)
//            val itemRect = Rect()
//            item.getGlobalVisibleRect(itemRect)
//
//            // 转换为相对container的坐标
//            val relativeRect = RectF(
//                itemRect.left.toFloat() - containerLayout.left,
//                itemRect.top.toFloat() - containerLayout.top,
//                itemRect.right.toFloat() - containerLayout.left,
//                itemRect.bottom.toFloat() - containerLayout.top
//            )
//
//            val isSelected = RectF.intersects(selectionRect, relativeRect)
//
//            if (isSelected && !selectedItems.contains(item)) {
//                selectedItems.add(item)
//                item.isSelected = true
//            } else if (!isSelected && selectedItems.contains(item)) {
//                selectedItems.remove(item)
//                item.isSelected = false
//            }
//        }
//    }
//
//    private fun toggleItemSelection(item: View) {
//        if (selectedItems.contains(item)) {
//            selectedItems.remove(item)
//            item.isSelected = false
//        } else {
//            selectedItems.add(item)
//            item.isSelected = true
//        }
//    }
//
//    private fun clearSelection() {
//        selectedItems.forEach { it.isSelected = false }
//        selectedItems.clear()
//    }
//}