package com.bella.testapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bella.testapp.R

class GridAdapter(
    private val list: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<GridAdapter.GridViewHolder>() {

    inner class GridViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        init {
            view.setOnClickListener {
                onItemClick(list[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list, parent, false)
        return GridViewHolder(view)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
    }

    override fun getItemCount(): Int = list.size
}