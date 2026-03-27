package com.bella.testapp

import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Rect
import android.openfde.AppTaskControllerProxy
import android.openfde.AppTaskStatusListener
import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.text.font.FontVariation
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fde.baselib.Animation.AnimDrawablePlayer
import com.fde.baselib.Animation.AnimFactory
import com.fde.baselib.view.CustomTitleBar
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {
    lateinit var test1: TextView;
    lateinit var test2: TextView;
    lateinit var test3: TextView;
    lateinit var test4: TextView;
    lateinit var test5: TextView;
    lateinit var test6: TextView;
    lateinit var editTest: EditText;
    lateinit var imgLoading: ImageView;
    lateinit var appTaskController : AppTaskControllerProxy ;

    lateinit var animDrawablePlayer: AnimDrawablePlayer ;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        appTaskController = AppTaskControllerProxy.create();


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initView();
        initEvent();
        checkMultiWindowMode()
    }

    fun initView() {
        test1 = findViewById(R.id.test1);
        test2 = findViewById(R.id.test2);
        test3 = findViewById(R.id.test3);
        test4 = findViewById(R.id.test4);
        test5 = findViewById(R.id.test5);
        test6 = findViewById(R.id.test6);
        editTest = findViewById(R.id.editTest);
        imgLoading = findViewById(R.id.imgLoading);

        test1.setText("鼠标Grid选中效果")
        test2.setText("鼠标List选中效果")
        test3.setText("RecyclerviewSelection效果")
        test4.setText("ImageViewTest")
        test5.setText("JniTest")

//        animDrawablePlayer = AnimFactory.loading(this, imgLoading)
//        animDrawablePlayer.start()

        val titleBar = findViewById<CustomTitleBar>(R.id.customTitleBar)
        titleBar.setTitle("我的应用标题")
//        titleBar.setVisible(CustomTitleBar.Type.OPTION,true)
        titleBar.setOnButtonClickListener(object : CustomTitleBar.OnButtonClickListener {
            override fun onLeftClick() {
                finish() // 左上角返回
            }

            override fun onCloseClick() {
                appTaskController.closeTask();
            }

            override fun onFullscreenClick() {
                // 全屏逻辑
                appTaskController.enterOrExitFullscreen();
            }

            override fun onImportClick() {

            }

            override fun onMinimizeClick() {
                appTaskController.minimize();
            }

            override fun onMaximizeClick() {
                appTaskController.maximizeOrNot();
            }
        })

        editTest.customSelectionActionModeCallback = object : ActionMode.Callback2() {

            override fun onGetContentRect(
                mode: ActionMode,
                view: View,
                outRect: Rect
            ) {
                // 👉 自定义菜单位置
                outRect.set(100, 600, 300, 900)
            }

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode) {}
        }
    }

    fun initEvent(){
        appTaskController?.initCustomCaption(
            WeakReference(this),
            true,
            object : AppTaskStatusListener {
                override fun onStatusChanged(
                    windowingMode: Int,
                    isSystemBarVisible: Boolean
                ) {
//                    mWindowingMode.value = windowingMode
//                    mIsSystemBarVisible.value  =isSystemBarVisible
                }

            }
        )

        test1.setOnClickListener({
            startActivity(Intent(this,GridListActivity::class.java))
//            val intent =  Intent(Intent.ACTION_PICK);
//            intent.setType("image/*");
//            startActivity( intent);
        })

        test2.setOnClickListener({
            startActivity(Intent(this,ListTestActivity::class.java))
        })

        test3.setOnClickListener({
            startActivity(Intent(this, RecyclerviewSelectionActivity::class.java))
        })

        test4.setOnClickListener({
            startActivity(Intent(this, ImageViewTestActivity::class.java))
        })

        test5.setOnClickListener({
            startActivity(Intent(this, JniTestActivity::class.java))

//            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
//                addCategory(Intent.CATEGORY_OPENABLE)
//                type = "image/*"
//            }
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            startActivityForResult(intent, 1)
        })
    }

    fun  checkMultiWindowMode() {
        val isMultiWindow = isInMultiWindowMode();
        Log.d("SplitScreen", "isInMultiWindowMode: " + isMultiWindow);
    }

    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean) {
        super.onMultiWindowModeChanged(isInMultiWindowMode)
        Log.d("SplitScreen", "onMultiWindowModeChanged: $isInMultiWindowMode")
    }

}