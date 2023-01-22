package com.singularitycoder.flowlauncher.helper

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

// https://stackoverflow.com/questions/28531996/android-recyclerview-gridlayoutmanager-column-spacing
class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
) : RecyclerView.ItemDecoration() {

    /**
     * App width -> 56dp
     * Number of columns = 4
     * Total Apps Width -> 56 * 4 = 224dp
     * Total width btw the apps -> deviceWidth() - 224dp
     * */
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.left = spacing
        outRect.right = spacing
        outRect.top = spacing
        outRect.bottom = spacing

        val position = parent.getChildAdapterPosition(view) // item position
        val column = position % spanCount // item column
    }
}