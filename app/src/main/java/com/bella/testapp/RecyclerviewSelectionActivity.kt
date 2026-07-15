package com.bella.testapp

import android.os.Bundle
import android.util.Log
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
import com.fde.baselib.view.CustomScrollBarView
import com.fde.baselib.view.RecyclerScrollBinder

class RecyclerviewSelectionActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var customScrollBarView: CustomScrollBarView
    private lateinit var tracker: SelectionTracker<Long>
    private lateinit var adapter: SelectionAdapter
    private var data = mutableListOf<Item>()

    private lateinit var binder : RecyclerScrollBinder;

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
        customScrollBarView = findViewById<CustomScrollBarView>(R.id.scrollBar)

        recyclerView.setHasFixedSize(true);
        val pool = recyclerView.getRecycledViewPool();
        pool.setMaxRecycledViews(0, 200); // 默认是 5，加大到 15~20 避免快速滑动时重新创建 View
        recyclerView.setRecycledViewPool(pool);
        // 2. 适当增加预加载缓存数量（默认是 2）
        recyclerView.setItemViewCacheSize(5);

//        RecyclerScrollBinder binder =
//        new RecyclerScrollBinder();



//        RecyclerScrollBinder.bind(recyclerView, customScrollBarView);
//        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                val range = recyclerView.computeVerticalScrollRange()
//                val offset = recyclerView.computeVerticalScrollOffset()
//                val extent = recyclerView.computeVerticalScrollExtent()
//
//                val progress = offset.toFloat() / (range - extent)
//
//                Log.w(
//                    "RecyclerScrollBinder",
//                    "progress:$progress, offset $offset, extent $extent, range $range"
//                )
//            }
//        })

        data = mutableListOf<Item>()
        for (i in 0..100000) {
            data.add(Item(i.toLong(), "Item $i"))
        }

        recyclerView.layoutManager = GridLayoutManager(this,14)
        adapter = SelectionAdapter(data) {
            Toast.makeText(this, "点击: $it", Toast.LENGTH_SHORT).show()
        }

        recyclerView.adapter = adapter
        recyclerView.isNestedScrollingEnabled = false
        binder = RecyclerScrollBinder();
        binder.bind(recyclerView,customScrollBarView)

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


    override fun onDestroy() {
        super.onDestroy()
        if(binder !=null){
            binder.unbind();
        }
    }

}