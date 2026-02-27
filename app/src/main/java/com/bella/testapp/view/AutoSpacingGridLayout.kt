package com.bella.testapp.view

import android.content.Context
import android.util.AttributeSet
import android.widget.GridLayout

class AutoSpacingGridLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : GridLayout(context, attrs, defStyleAttr) {

    private var columnCount = 3 // 默认列数
    private var itemWidth = 0 // Item固定宽度
    private var itemHeight = 0 // Item固定高度

    fun setGridProperties(columns: Int, itemWidth: Int, itemHeight: Int) {
        this.columnCount = columns
        this.itemWidth = itemWidth
        this.itemHeight = itemHeight
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)

        // 计算可用宽度（减去padding）
        val availableWidth = parentWidth - paddingLeft - paddingRight

        // 计算总Item宽度
        val totalItemsWidth = itemWidth * columnCount

        // 计算可分配的总间距宽度
        val totalSpacingWidth = availableWidth - totalItemsWidth

        // 计算每列之间的间距（列数-1个间隔）
        val columnSpacing = if (columnCount > 1) {
            totalSpacingWidth / (columnCount - 1)
        } else 0

        // 设置列间距
        this.columnCount = columnCount

        // 更新每个子View的LayoutParams
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val layoutParams = child.layoutParams as LayoutParams

            // 设置固定宽高
            layoutParams.width = itemWidth
            layoutParams.height = itemHeight

            // 设置列间距
            if (i % columnCount != columnCount - 1) { // 不是每行最后一个
                layoutParams.rightMargin = columnSpacing
            }

            child.layoutParams = layoutParams
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}