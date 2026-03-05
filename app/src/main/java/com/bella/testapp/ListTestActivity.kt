package com.bella.testapp

import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.InputDevice
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bella.testapp.adapter.GridAdapter
import com.bella.testapp.bean.Item
import com.bella.testapp.list.MyItemDetailsLookup
import com.bella.testapp.list.MyItemKeyProvider
import com.bella.testapp.view.SelectionView

class ListTestActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var selectionView: SelectionView
    private lateinit var tracker: SelectionTracker<Long>
    private lateinit var adapter: GridAdapter


    companion object {
        private var isScroll = 0
    }

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


//        val data = List(20) { "Item $it" }
        val data = mutableListOf<Item>()
        for (i in 0..50) {
            data.add(Item(i.toLong(), "Item $i"))
        }

//        recyclerView.layoutManager = GridLayoutManager(this, 7)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = GridAdapter(data) {
            Toast.makeText(this, "点击: $it", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter
        recyclerView.isNestedScrollingEnabled = false


        tracker = SelectionTracker.Builder(
            "mySelection",
            recyclerView,
            MyItemKeyProvider(adapter),
            MyItemDetailsLookup(recyclerView),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        adapter.tracker = tracker

        setupMouseSelection()
    }

    private var dragging = false

    private fun setupMouseSelection() {

        selectionView.setOnGenericMotionListener { _, event ->
            isScroll = event.actionMasked
            if (event.source and InputDevice.SOURCE_MOUSE == InputDevice.SOURCE_MOUSE) {

                val isMouse = event.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE
                val isLeft = (event.buttonState  === MotionEvent.BUTTON_PRIMARY)

                Log.d("Grid", "event.actionMasked  "+event.actionMasked  +",event.action " +event.action  +",event.buttonState "+event.buttonState  + ",isMouse "+isMouse + ",isLeft "+isLeft)


            }

            Log.w("Grid","setupMouseSelection isScroll: "+isScroll);
            false
        }

        selectionView.setOnTouchListener { _, event ->

            val isMouse = event.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE
            val isLeft = (event.buttonState  === 0)

            Log.w("Grid","setupMouseSelection isScroll: "+isScroll + " ,event.actionMasked "+event.deviceId  + " ,event.buttonState  "+event.buttonState )
//            if(isScroll){
//                 false;
//            }
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
