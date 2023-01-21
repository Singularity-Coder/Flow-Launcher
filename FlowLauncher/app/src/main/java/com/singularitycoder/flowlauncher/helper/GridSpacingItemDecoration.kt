package com.singularitycoder.flowlauncher.helper

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

// https://stackoverflow.com/questions/28531996/android-recyclerview-gridlayoutmanager-column-spacing
class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {

    // App width -> 40dp
    // Total Apps Width -> 40 * 4 = 160dp
    // width btw the apps -> deviceWidth() - 160dp
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view) // item position
        val column = position % spanCount // item column
        outRect.left = spacing
        outRect.right = spacing
//        outRect.left = (deviceWidth() - 160.dpToPx()) / 5
//        outRect.right = (deviceWidth() - 160.dpToPx()) / 5
        outRect.top = spacing
        outRect.bottom = spacing
//        if (position < spanCount) {
//            outRect.right = spacing
//        }
//        if (includeEdge) {
//            outRect.left = spacing - column * (spacing / spanCount) // spacing - column * ((1f / spanCount) * spacing)
//            outRect.right = (column + 1) * spacing / spanCount // (column + 1) * ((1f / spanCount) * spacing)
//            if (position < spanCount) { // top edge
//                outRect.top = spacing
//            }
//            outRect.bottom = spacing // item bottom
//        } else {
//            outRect.left = column * spacing / spanCount // column * ((1f / spanCount) * spacing)
//            outRect.right = spacing - (column + 1) * spacing / spanCount // spacing - (column + 1) * ((1f /    spanCount) * spacing)
//            if (position >= spanCount) {
//                outRect.top = spacing // item top
//            }
//        }
    }
}