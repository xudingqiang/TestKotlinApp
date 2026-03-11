package com.bella.testapp

import android.os.Bundle
import android.view.GestureDetector
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
import com.bella.testapp.adapter.SelectionAdapter
import com.bella.testapp.bean.Item
import com.bella.testapp.list.MyItemDetailsLookup
import com.bella.testapp.list.MyItemKeyProvider

class RecyclerviewSelectionActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var tracker: SelectionTracker<Long>
    private lateinit var adapter: SelectionAdapter
    private var data = mutableListOf<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recyclerview_selection)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        data = mutableListOf<Item>()
        for (i in 0..50) {
            data.add(Item(i.toLong(), "Item $i"))
        }

        recyclerView.layoutManager = GridLayoutManager(this,7)
        adapter = SelectionAdapter(data) {
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


      


        recyclerView.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {

            private val detector = GestureDetector(
                recyclerView.context,
                object : GestureDetector.SimpleOnGestureListener() {

                    override fun onSingleTapUp(e: MotionEvent): Boolean {

                        if (e.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE) {

                            val view = recyclerView.findChildViewUnder(e.x, e.y)

                            if (view != null) {
                                val vh = recyclerView.getChildViewHolder(view)
                                tracker.select(vh.adapterPosition.toLong())
                            }
                        }

                        return true
                    }
                }
            )

            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                detector.onTouchEvent(e)
                return false
            }
        })

    }
}