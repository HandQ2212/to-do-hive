package com.proptit.todohive.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.proptit.todohive.R

class SwipeToDeleteCallback(
    context: Context,
    private val onSwipedLeft: (RecyclerView.ViewHolder) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val background = ColorDrawable(Color.parseColor("#D32F2F"))
    private val deleteIcon: Drawable? =
        ContextCompat.getDrawable(context, R.drawable.ic_delete)

    private val iconW = deleteIcon?.intrinsicWidth ?: 0
    private val iconH = deleteIcon?.intrinsicHeight ?: 0

    override fun onMove(rv: RecyclerView, viewHolder: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, dir: Int) {
        if (dir == ItemTouchHelper.LEFT) onSwipedLeft(viewHolder)
    }

    override fun onChildDraw(
        canvas: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
    ) {
        val item = viewHolder.itemView
        if (dX < 0) {
            background.setBounds(item.right + dX.toInt(), item.top, item.right, item.bottom)
            background.draw(canvas)

            deleteIcon?.let { icon ->
                val margin = (item.height - iconH) / 2
                val top = item.top + margin
                val bottom = top + iconH
                val left = item.right - margin - iconW
                val right = item.right - margin
                icon.setBounds(left, top, right, bottom)
                icon.draw(canvas)
            }
        } else {
            background.setBounds(0, 0, 0, 0)
        }
        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}
