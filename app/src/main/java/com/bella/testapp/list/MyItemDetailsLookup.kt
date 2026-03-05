package com.bella.testapp.list

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.bella.testapp.adapter.GridAdapter

class MyItemDetailsLookup(
    private val recyclerView: RecyclerView
) : ItemDetailsLookup<Long>() {

    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {

        val view = recyclerView.findChildViewUnder(e.x, e.y)
        if (view != null) {
            val holder =
                recyclerView.getChildViewHolder(view) as GridAdapter.GridViewHolder
            return holder.getItemDetails()
        }
        return null
    }
}