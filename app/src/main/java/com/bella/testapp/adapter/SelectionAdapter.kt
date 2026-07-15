package com.bella.testapp.adapter

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.bella.testapp.R
import com.bella.testapp.bean.Item
import com.bumptech.glide.Glide

class SelectionAdapter(
    private val list: List<Item>,
    private val onItemClick: (Item) -> Unit
) : RecyclerView.Adapter<SelectionAdapter.GridViewHolder>() {

    var tracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }

    inner class GridViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var textView : TextView;
        var imageView : ImageView;

        init {
            textView = view.findViewById<TextView>(R.id.textView)
            imageView = view.findViewById<ImageView>(R.id.imageView)
            view.setOnClickListener {
                onItemClick(list[adapterPosition])
            }
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> {
            return object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int {
                    return adapterPosition
                }

                override fun getSelectionKey(): Long {
                    return itemId
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_grid, parent, false)

        val tv = TextView(parent.context)
        tv.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            150
        )
        tv.textSize = 20f
        tv.gravity = Gravity.CENTER_VERTICAL
        tv.setPadding(40,0,0,0)

        return GridViewHolder(view)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        val item = list[position]
        holder.textView.text = item.name

        Glide.with(holder.imageView)
            .load(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .thumbnail(0.1f) // ⭐ 首帧快
            .dontAnimate()
            .centerCrop()
            .into(holder.imageView);


        if (tracker?.isSelected(item.id) == true) {
            holder.textView.setBackgroundColor(Color.LTGRAY)
        } else {
            holder.textView.setBackgroundColor(Color.WHITE)
        }
    }

    override fun getItemCount(): Int = list.size

    override fun getItemId(position: Int): Long {
        return list[position].id
    }
}