package com.bella.testapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bella.testapp.view.MyGLSurfaceView

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
        Log.w("bella","result:  "+resultC  + "  "+resultCPP)
//        txtTest?.text = resultC + " ---   "+resultCPP

    }
}