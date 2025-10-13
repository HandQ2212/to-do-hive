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

class SwipeToRestoreDeleteCallback(
    context: Context,
    private val onSwipedLeft: (RecyclerView.ViewHolder) -> Unit,
    private val onSwipedRight: (RecyclerView.ViewHolder) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private val bgDelete  = ColorDrawable(Color.parseColor("#D32F2F"))
    private val bgRestore = ColorDrawable(Color.parseColor("#2E7D32"))

    private val icDelete : Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_delete)
    private val icRestore: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_restore)

    private val iconW = (icDelete?.intrinsicWidth ?: 0).coerceAtLeast(icRestore?.intrinsicWidth ?: 0)
    private val iconH = (icDelete?.intrinsicHeight ?: 0).coerceAtLeast(icRestore?.intrinsicHeight ?: 0)

    override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

    override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {
        when (dir) {
            ItemTouchHelper.LEFT  -> onSwipedLeft(vh)
            ItemTouchHelper.RIGHT -> onSwipedRight(vh)
        }
    }

    override fun onChildDraw(
        c: Canvas, rv: RecyclerView, vh: RecyclerView.ViewHolder,
        dX: Float, dY: Float, state: Int, active: Boolean
    ) {
        val item = vh.itemView
        val h = item.height
        val margin = (h - iconH) / 2

        if (dX < 0) {
            bgDelete.setBounds(item.right + dX.toInt(), item.top, item.right, item.bottom)
            bgDelete.draw(c)
            icDelete?.let { icon ->
                val left = item.right - margin - iconW
                val right = item.right - margin
                val top = item.top + margin
                val bottom = top + iconH
                icon.setBounds(left, top, right, bottom)
                icon.draw(c)
            }
        } else if (dX > 0) {
            bgRestore.setBounds(item.left, item.top, item.left + dX.toInt(), item.bottom)
            bgRestore.draw(c)
            icRestore?.let { icon ->
                val left = item.left + margin
                val right = left + iconW
                val top = item.top + margin
                val bottom = top + iconH
                icon.setBounds(left, top, right, bottom)
                icon.draw(c)
            }
        } else {
            bgDelete.setBounds(0, 0, 0, 0)
            bgRestore.setBounds(0, 0, 0, 0)
        }

        super.onChildDraw(c, rv, vh, dX, dY, state, active)
    }
}
