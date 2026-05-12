package com.fde.baselib.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toolbar
import com.fde.baselib.R

class CustomTitleBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val imgLeft: ImageView
    private val txtTitle: TextView
    private val imgImport: ImageView
    private val imgFullscreen: ImageView
    private val imgMinimize: ImageView
    private val imgMaximize: ImageView
    private val imgClose: ImageView
    private val toolBar: Toolbar
    private val layoutRootView: RelativeLayout

    enum class Type {
        LEFT,
        TITLE,
        OPTION,
        FULLSCREEN,
        MINIMIZE,
        MAXIMIZE,
        CLOSE
    }
    init {
        LayoutInflater.from(context).inflate(R.layout.layout_custom_title, this, true)
        imgLeft = findViewById(R.id.imgLeft)
        txtTitle = findViewById(R.id.txtTitle)
        imgImport = findViewById(R.id.imgImport)
        imgFullscreen = findViewById(R.id.imgFullscreen)
        imgMinimize = findViewById(R.id.imgMinimize)
        imgMaximize = findViewById(R.id.imgMaximize)
        imgClose = findViewById(R.id.imgClose)
        toolBar = findViewById(R.id.toolBar)
        layoutRootView = findViewById(R.id.layoutRootView)
    }

    /** 设置标题文字 */
    fun setTitle(title: String) {
        txtTitle.text = title
    }

    /** 设置按钮点击监听 */
    fun setOnButtonClickListener(listener: OnButtonClickListener) {
        imgLeft.setOnClickListener { listener.onLeftClick() }
        imgImport.setOnClickListener { listener.onImportClick() }
        imgFullscreen.setOnClickListener { listener.onFullscreenClick() }
        imgMinimize.setOnClickListener { listener.onMinimizeClick() }
        imgMaximize.setOnClickListener { listener.onMaximizeClick() }
        imgClose.setOnClickListener { listener.onCloseClick() }

    }

    fun getButton(type : Type) : View {
        return when(type){
            Type.LEFT -> imgLeft
            Type.TITLE -> txtTitle
            Type.OPTION -> imgImport
            Type.FULLSCREEN -> imgFullscreen
            Type.MINIMIZE -> imgMinimize
            Type.MAXIMIZE -> imgMaximize
            Type.CLOSE -> imgClose
        }
    }

    fun addChildView(view : View) {
        layoutRootView.addView(view)
    }

    fun setButtonBackground(type : Type,resId : Int) {
        when(type){
            Type.LEFT -> imgLeft.setImageResource(resId)
            Type.OPTION -> imgImport.setImageResource(resId)
            Type.FULLSCREEN -> imgFullscreen.setImageResource(resId)
            Type.MINIMIZE -> imgMinimize.setImageResource(resId)
            Type.MAXIMIZE -> imgMaximize.setImageResource(resId)
            Type.CLOSE -> imgClose.setImageResource(resId)
            Type.TITLE -> {}
        }
    }

    fun setViewVisible( type : Type,visible: Boolean) {
        when(type){
            Type.LEFT -> imgLeft.visibility = if (visible) VISIBLE else GONE
            Type.TITLE -> txtTitle.visibility = if (visible) VISIBLE else GONE
            Type.OPTION -> imgImport.visibility = if (visible) VISIBLE else GONE
            Type.FULLSCREEN -> imgFullscreen.visibility = if (visible) VISIBLE else GONE
            Type.MINIMIZE -> imgMinimize.visibility = if (visible) VISIBLE else GONE
            Type.MAXIMIZE -> imgMaximize.visibility = if (visible) VISIBLE else GONE
            Type.CLOSE -> imgClose.visibility = if (visible) VISIBLE else GONE
        }
    }

    interface OnButtonClickListener {
        fun onLeftClick() {}
        fun onImportClick() {}
        fun onFullscreenClick() {}
        fun onMinimizeClick() {}
        fun onMaximizeClick() {}
        fun onCloseClick() {}
    }
}