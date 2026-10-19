package com.bella.testapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.net.Uri
import android.openfde.AppTaskControllerProxy
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowInsetsController
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListPopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appfunctions.AppFunctionData
import androidx.appfunctions.AppFunctionSearchSpec
import androidx.appfunctions.ExecuteAppFunctionRequest
import androidx.appfunctions.metadata.AppFunctionMetadata
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fde.baselib.Animation.AnimDrawablePlayer
import com.fde.baselib.view.CustomTitleBar
import org.oremif.deepseek.models.FunctionRequest
import org.oremif.deepseek.models.Tool
import org.oremif.deepseek.models.ToolCallType

class MainActivity : AppCompatActivity() {
    lateinit var test1: TextView;
    lateinit var test2: TextView;
    lateinit var test3: TextView;
    lateinit var test4: TextView;
    lateinit var test5: TextView;
    lateinit var test6: TextView;
    lateinit var test7: TextView;
    lateinit var editTest: EditText;
    lateinit var titleBar: CustomTitleBar;
    lateinit var imgLoading: ImageView;
    lateinit var appTaskController : AppTaskControllerProxy ;

    lateinit var animDrawablePlayer: AnimDrawablePlayer ;
    lateinit var context : Context


    private lateinit var root: TextView
    private lateinit var captionBar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        context = this
//        appTaskController = AppTaskControllerProxy.create();




        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        } else {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(
//                    Manifest.permission.READ_EXTERNAL_STORAGE,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE
//                ),
//                REQUEST_CODE
//            )
        }
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initView();
        initEvent();
        initAgentDemoView();
        initAgentDemoEvent();
        checkMultiWindowMode()
    }


    fun updateFullscreenInnerColor(
        view: View,
        hoverColor: Int,
        pressedColor: Int,
        normalColor: Int
    ) {
        try {
            val layerDrawable = view.background.mutate() as? LayerDrawable
                ?: return

            val innerDrawable = layerDrawable
                .findDrawableByLayerId(R.id.fullscreen_inner_bg)
                .mutate() as? StateListDrawable
                ?: return

            // 遍历 selector 中的各个状态背景
            for (i in 0 until innerDrawable.stateCount) {
                val drawable = innerDrawable.getStateDrawable(i)
                    ?: continue

                when (i) {
                    0 -> drawable.setTint(hoverColor)
                    1 -> drawable.setTint(pressedColor)
                    else -> drawable.setTint(normalColor)
                }
            }

            view.background = layerDrawable
        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    fun initView() {

        test1 = findViewById(R.id.test1);
        test2 = findViewById(R.id.test2);
        test3 = findViewById(R.id.test3);
        test4 = findViewById(R.id.test4);
        test5 = findViewById(R.id.test5);
        test6 = findViewById(R.id.test6);
        test7 = findViewById(R.id.test7);
        editTest = findViewById(R.id.editTest);
        imgLoading = findViewById(R.id.imgLoading);

        val test9 = findViewById<TextView>(R.id.test9);
        test9.setOnClickListener {

        }


//        val layerDrawable = test9.background.mutate() as LayerDrawable
//        val innerDrawable = layerDrawable
//            .findDrawableByLayerId(R.id.fullscreen_inner_bg)
//            .mutate() as GradientDrawable
//
//        innerDrawable.setColor(Color.parseColor("#FF0000"))
//
//        test9.background = layerDrawable

        updateFullscreenInnerColor(
            view = test9,
            hoverColor = Color.parseColor("#FF0000"),
            pressedColor = Color.parseColor("#77757F"),
            normalColor = Color.parseColor("#B2B0BB")
        )


        val strokeColor = Color.parseColor("#FF00FF")
        val backgroundColor = Color.parseColor("#0000FF")

        val outSize = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 6f * 1
            setColor(strokeColor)
            setStroke(
                0,
                strokeColor
            )
        }

        val layerDrawable = test9.background.mutate() as? LayerDrawable
            ?: return

        layerDrawable.setDrawableByLayerId(
                    R.id.fullscreen_outsize,
                    outSize.mutate()
                )


        val inSize = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 6f * 1
            setColor(backgroundColor)
            setStroke(
                0,
                backgroundColor
            )
        }

        layerDrawable.setDrawableByLayerId(
            R.id.fullscreen_second,
            inSize.mutate()
        )

//        layerDrawable.setDrawableByLayerId(
//            R.id.fullscreen_inner_icon,
//            inSize.mutate()
//        )

        test9.background = layerDrawable

//        var layerDrawable =  getResources().getDrawable(R.drawable.desktop_mode_layout_menu_fullscreen_button_background);
//        val innerDrawable = GradientDrawable()
//        innerDrawable.shape = GradientDrawable.RECTANGLE
//        innerDrawable.cornerRadii = floatArrayOf(10f, 10f, 10f, 10f, 0f, 0f, 0f, 0f)
//        innerDrawable.setColor(Color.parseColor("#FF0000"));
//        test9.setBackground(layerDrawable);

        test1.setText("鼠标Grid选中效果")
        test2.setText("鼠标List选中效果")
        test3.setText("RecyclerviewSelection效果")
        test4.setText("ImageViewTest")
        test5.setText("JniTest")
        test6.setText("DragTest")
        test7.setText("标题栏测试")

//        animDrawablePlayer = AnimFactory.loading(this, imgLoading)
//        animDrawablePlayer.start()

        titleBar = findViewById<CustomTitleBar>(R.id.customTitleBar)
        titleBar.setTitle("我的应用标题")
//        titleBar.setVisible(CustomTitleBar.Type.OPTION,true)

        val textView = TextView(this).apply {
            text = "TEST 的TextView"
            textSize = 16f
            setTextColor(Color.BLACK)
        }
//        titleBar.addChildView(textView)


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
//                val popupMenu = PopupMenu(context, titleBar.getButton(CustomTitleBar.Type.OPTION))
//                popupMenu.menu.add("选项1")
//                popupMenu.menu.add("选项2")
//                popupMenu.menu.add("选项3")
//                popupMenu.setOnMenuItemClickListener { item ->
//                    when (item.title) {
//                        "选项1" -> { /* TODO */ }
//                        "选项2" -> { /* TODO */ }
//                    }
//                    true
//                }
//                popupMenu.show()

                val listPopupWindow = ListPopupWindow(context)

                val data = listOf("选项A", "选项B", "选项C")
                val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, data)

                listPopupWindow.anchorView = titleBar.getButton(CustomTitleBar.Type.OPTION)
                listPopupWindow.setAdapter(adapter)
                listPopupWindow.width = 150

                listPopupWindow.setOnItemClickListener { _, _, position, _ ->
                    val item = data[position]
                    Toast.makeText(context, item, Toast.LENGTH_SHORT).show()
                    listPopupWindow.dismiss()
                }

                titleBar.getButton(CustomTitleBar.Type.OPTION).setOnClickListener {
                    listPopupWindow.show()
                }
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
//        appTaskController?.initCustomCaption(
//            WeakReference(this),
//            true,
//            object : AppTaskStatusListener {
//                override fun onStatusChanged(
//                    windowingMode: Int,
//                    isSystemBarVisible: Boolean
//                ) {
//                    titleBar.setButtonBackground(CustomTitleBar.Type.MAXIMIZE,if(windowingMode == 5) com.fde.baselib.R.drawable.icon_maximize else com.fde.baselib.R.drawable.icon_exitmaximize)
//                    titleBar.setButtonBackground(CustomTitleBar.Type.FULLSCREEN,if(isSystemBarVisible) com.fde.baselib.R.drawable.icon_fullscreen else com.fde.baselib.R.drawable.icon_exitfullscreen)
//                }
//
//            }
//        )

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

//        test6.setText("获取焦点")
//        test6.isFocusable = true
//        test6.isFocusableInTouchMode = true
        test6.setOnClickListener({
//            test6.requestFocus();
            startActivity(Intent(this, DragTestActivity::class.java))

        })


        test7.setOnClickListener({
            startActivity(Intent(this, TitleBarTestActivity::class.java))
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
//
//    fun buildDeepSeekTool(metadata: AppFunctionMetadata): Tool {
//        return Tool(
//            type = ToolCallType.FUNCTION,
//            function = FunctionRequest(
//                name = metadata.functionIdentifier, // 函数的唯一标识符
//                description = metadata.description, // 来自 KDoc
//                parameters = buildJsonSchema(metadata.parameters)
//            )
//        )
//    }
//
//    suspend fun executeAppFunction(
//        functionIdentifier: String,
//        argumentsJson: String
//    ): String {
//        val appFunctionManager = AppFunctionManagerCompat.getInstance(context)
//
//        // 查询函数元数据
//        val metadata = appFunctionManager.observeAppFunctions(
//            AppFunctionSearchSpec(
//                packageNames = listOf(context.packageName),
//                schemaName = "my_app_function_service"
//            )
//        ).first().first().appFunctions
//            .first { it.functionIdentifier == functionIdentifier }
//
//        // 将 JSON 参数转换为 AppFunctionData
//        val paramsData = AppFunctionData.fromJson(argumentsJson, metadata.parameters)
//
//        // 构建并执行请求
//        val request = ExecuteAppFunctionRequest.Builder()
//            .setAppFunctionMetadata(metadata)
//            .setParameters(paramsData)
//            .build()
//
//        val response = appFunctionManager.executeAppFunction(request).await()
//        return response.result.toString()
//    }


}