package com.bella.testapp.list

import androidx.recyclerview.selection.ItemKeyProvider
import com.bella.testapp.adapter.SelectionAdapter

class MyItemKeyProvider(
    private val adapter: SelectionAdapter
) : ItemKeyProvider<Long>(SCOPE_CACHED) {

    override fun getKey(position: Int): Long {
        return adapter.getItemId(position)
    }

    override fun getPosition(key: Long): Int {
        return key.toInt()
    }
}