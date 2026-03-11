package com.bella.testapp.list

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.bella.testapp.adapter.SelectionAdapter

class MyItemDetailsLookup(
    private val recyclerView: RecyclerView
) : ItemDetailsLookup<Long>() {

    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {

//        val view = recyclerView.findChildViewUnder(e.x, e.y)
//        if (view != null) {
//            val holder =
//                recyclerView.getChildViewHolder(view) as SelectionAdapter.GridViewHolder
//            return holder.getItemDetails()
//        }
//        return null
        if (e.getToolType(0) != MotionEvent.TOOL_TYPE_MOUSE &&
            e.getToolType(0) != MotionEvent.TOOL_TYPE_FINGER
        ) return null

        // 只允许左键触发
        if (e.buttonState and MotionEvent.BUTTON_PRIMARY == 0) return null

        val view = recyclerView.findChildViewUnder(e.x, e.y) ?: return null
        val holder = recyclerView.getChildViewHolder(view)
        if (holder is SelectionAdapter.GridViewHolder) {
            return holder.getItemDetails()
        }
        return null
    }
}