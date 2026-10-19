package com.bella.testapp

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowInsetsController
import android.widget.ImageButton
import android.widget.LinearLayout

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TitleBarTestActivity : AppCompatActivity() {

    private lateinit var root: View
    private lateinit var captionBar: View

//    private lateinit var minimizeButton: ImageButton
//    private lateinit var maximizeButton: ImageButton
//    private lateinit var closeButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_title_bar_test)

        root = findViewById(R.id.root)
        captionBar = findViewById(R.id.caption_bar)

//        minimizeButton = findViewById(R.id.minimize)
//        maximizeButton = findViewById(R.id.maximize)
//        closeButton = findViewById(R.id.close)

        setupTransparentCaptionBar()

        setupCaptionBarInsets()

        setupGestureExclusion()

        setupWindowButtons()
    }

    /**
     * 设置透明 CaptionBar 背景。
     *
     * Android 17 Desktop Mode 下，
     * App 可以在 CaptionBar 区域绘制自己的 UI。
     */
    private fun setupTransparentCaptionBar() {

        window.insetsController?.setSystemBarsAppearance(
            WindowInsetsController
                .APPEARANCE_TRANSPARENT_CAPTION_BAR_BACKGROUND,

            WindowInsetsController
                .APPEARANCE_TRANSPARENT_CAPTION_BAR_BACKGROUND
        )
    }

    /**
     * 获取 CaptionBar Insets。
     */
    private fun setupCaptionBarInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->

            val captionInsets =
                windowInsets.getInsets(
                    WindowInsetsCompat.Type.captionBar()
                )

            /*
             * captionInsets.top
             * captionInsets.bottom
             * captionInsets.left
             * captionInsets.right
             */

            // 自定义标题栏不需要额外 padding
            captionBar.setPadding(
                0,
                0,
                0,
                0
            )

            windowInsets
        }

        ViewCompat.requestApplyInsets(root)
    }

    /**
     * 设置标题栏区域的 System Gesture Exclusion。
     *
     * 这里假设标题栏高度为 48dp。
     */
    private fun setupGestureExclusion() {

        val decorView = window.decorView

        fun updateGestureExclusion() {

            if (decorView.width <= 0) {
                return
            }

            val height =
                (48 * resources.displayMetrics.density).toInt()

            val rects = listOf(
                Rect(
                    0,
                    0,
                    decorView.width,
                    height
                )
            )

            decorView.systemGestureExclusionRects = rects
        }

        updateGestureExclusion()

        decorView.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {

                override fun onGlobalLayout() {
                    updateGestureExclusion()
                }
            }
        )
    }

    /**
     * 设置窗口按钮。
     */
    private fun setupWindowButtons() {

        /*
         * 最小化
         *
         * 注意：
         * App 本身没有一个通用的 public API
         * 可以像 Windows 一样直接 minimize 自己。
         *
         * Desktop Mode 下可以通过 Activity/Task
         * 或 Shell 侧实现。
         */
//        minimizeButton.setOnClickListener {
//
//            moveTaskToBack(true)
//        }
//
//        /*
//         * 最大化 / 恢复
//         *
//         * 普通 App 不能简单通过
//         * window.maximize()
//         *
//         * Desktop Mode 下窗口尺寸由 WM Shell 管理。
//         *
//         * 这里先作为按钮入口。
//         */
//        maximizeButton.setOnClickListener {
//
//            toggleMaximize()
//        }
//
//        /*
//         * 关闭
//         */
//        closeButton.setOnClickListener {
//
//            finish()
//        }
    }

    private fun toggleMaximize() {

        /*
         * Android 17 Desktop Mode 下，
         * 如果需要真正控制 Desktop Task：
         *
         * App → WindowManager
         * App → Shell
         *
         * 需要结合系统提供的 Desktop Mode API。
         *
         * 普通第三方 App 不建议直接调用隐藏 API。
         */

    }
}