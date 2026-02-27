package com.bella.testapp.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

class SelectionView(context: Context, attrs: AttributeSet?) :
    View(context, attrs) {

    private var startX = 0f
    private var startY = 0f
    private var currentX = 0f
    private var currentY = 0f
    private var dragging = false

    private val fillPaint = Paint().apply {
        color = Color.parseColor("#223388FF")
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint().apply {
        color = Color.parseColor("#3388AAFF")
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    fun start(x: Float, y: Float) {
        startX = x
        startY = y
        currentX = x
        currentY = y
        dragging = true
        invalidate()
    }

    fun update(x: Float, y: Float) {
        currentX = x
        currentY = y
        invalidate()
    }

    fun stop() {
        dragging = false
        invalidate()
    }

    fun getRect(): RectF {
        return RectF(
            min(startX, currentX),
            min(startY, currentY),
            max(startX, currentX),
            max(startY, currentY)
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (!dragging) return

        val rect = getRect()
        canvas.drawRect(rect, fillPaint)
        canvas.drawRect(rect, strokePaint)
    }
}