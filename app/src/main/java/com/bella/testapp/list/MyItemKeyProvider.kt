package com.bella.testapp.list

import androidx.recyclerview.selection.ItemKeyProvider
import com.bella.testapp.adapter.GridAdapter

class MyItemKeyProvider(
    private val adapter: GridAdapter
) : ItemKeyProvider<Long>(SCOPE_CACHED) {

    override fun getKey(position: Int): Long {
        return adapter.getItemId(position)
    }

    override fun getPosition(key: Long): Int {
        return key.toInt()
    }
}