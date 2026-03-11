package com.bella.testapp

import android.app.WallpaperManager
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    lateinit var test1: TextView;
    lateinit var test2: TextView;
    lateinit var test3: TextView;
    lateinit var test4: TextView;
    lateinit var test5: TextView;
    lateinit var test6: TextView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initView();
        initEvent();

    }

    fun initView() {
        test1 = findViewById(R.id.test1);
        test2 = findViewById(R.id.test2);
        test3 = findViewById(R.id.test3);
        test4 = findViewById(R.id.test4);
        test5 = findViewById(R.id.test5);
        test6 = findViewById(R.id.test6);

        test1.setText("鼠标Grid选中效果")
        test2.setText("鼠标List选中效果")
        test3.setText("RecyclerviewSelection效果")
        test4.setText("ImageViewTest")
        test5.setText("JniTest")



    }

    fun initEvent(){
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
        })
    }


}