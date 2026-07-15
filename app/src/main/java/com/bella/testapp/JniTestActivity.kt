package com.bella.testapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bella.testapp.view.MyGLSurfaceView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JniTestActivity : AppCompatActivity() {
    var txtTest: TextView? = null

    companion object {
        init {
            System.loadLibrary("native-c")
            System.loadLibrary("native-lib")
        }
    }

    external fun stringFromJNIC(): String
    external fun stringFromJNICPP(): String
    external fun scanDirectory(path: String): Array<String>?
    external fun scanDirectoryCpp(path: String): Array<String>?
    external fun findFiles(path: String,str: String): Array<String>


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        setContentView(R.layout.activity_jni_test)
        setContentView( MyGLSurfaceView(this));
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

//        txtTest = findViewById(R.id.txtTest);
//
        val resultC = stringFromJNIC()
        val resultCPP = stringFromJNICPP()

        val targetPath = "/mnt/sdcard/Download/test/";//filesDir.absolutePath
        val start = System.currentTimeMillis()
        Log.d("bellaT","start:  "+ start)
        val fileList = scanDirectoryCpp(targetPath)
        if (fileList != null) {
            for (fileName in fileList) {
//                Log.d("bellaT", "发现文件/文件夹: $fileName")
            }
        } else {
            Log.e("bellaT", "读取目录失败或目录不存在: $targetPath")
        }



        val end = System.currentTimeMillis()
        Log.d("bellaT","end:  "+ end  + " ,time: "+(end-start))

        val  index = fileList?.indexOf("999");
        val end2 = System.currentTimeMillis()
        Log.d("bellaT","end:  "+ end  + " ,time: "+(end2-start)  + ",index "+index)

        Log.w("bellaT","result:  "+resultC  + "  "+resultCPP + ",fileList "+fileList?.size)
//        txtTest?.text = resultC + " ---   "+resultCPP
        startSearch()

    }

    private fun startSearch() {
        // 使用 lifecycleScope 启动协程，自动绑定 Activity 生命周期
        lifecycleScope.launch {

            // 1. 准备参数（这里以 App 的内部私有目录为例，不需要特殊权限）
            val searchPath = "/mnt/sdcard/Download/test/";
            val targetFile = "file0001"

            Log.d("FindTask", "开始在 Native 层查找文件...")

            // 2. 切换到 IO 线程执行 Native 耗时操作
            val results: Array<String> = withContext(Dispatchers.IO) {
                findFiles(searchPath, targetFile)
            }

            // 3. 自动切回主线程，处理 UI 结果
            if (results == null || results.isEmpty()) {
                Log.d("FindTask", "未找到匹配的文件")
            } else {
                Log.d("FindTask", "查找完成，共找到 ${results.size} 个文件：")
                results.forEach { path ->
                    Log.d("FindTask", "-> $path")
                }
            }
        }
    }

}