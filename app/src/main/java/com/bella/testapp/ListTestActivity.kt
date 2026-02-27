package com.bella.testapp

import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bella.testapp.adapter.GridAdapter
import com.bella.testapp.view.SelectionView

class ListTestActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var selectionView: SelectionView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_list_test)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        selectionView = findViewById(R.id.selectionView)


        val data = List(20) { "Item $it" }

//        recyclerView.layoutManager = GridLayoutManager(this, 7)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = GridAdapter(data) {
            Toast.makeText(this, "点击: $it", Toast.LENGTH_SHORT).show()
        }
        recyclerView.isNestedScrollingEnabled = false

        setupMouseSelection()
    }


    private fun setupMouseSelection() {

        selectionView.setOnTouchListener { _, event ->

            val isMouse = event.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE
            val isLeft = (event.buttonState  === 0)

            Log.w("Grid","setupMouseSelection isMouse: "+event.actionIndex + " ,event.actionMasked "+event.deviceId  + " ,event.buttonState  "+event.buttonState )

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

        for (i in 0 until recyclerView.childCount) {

            val child = recyclerView.getChildAt(i)

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
        for (i in 0 until recyclerView.childCount) {
            recyclerView.getChildAt(i).isSelected = false
        }
    }
}
