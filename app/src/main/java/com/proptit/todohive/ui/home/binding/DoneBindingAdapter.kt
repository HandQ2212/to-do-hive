package com.proptit.todohive.ui.home.binding

import android.content.res.ColorStateList
import android.widget.ImageView
import androidx.core.widget.ImageViewCompat
import androidx.databinding.BindingAdapter
import com.proptit.todohive.R

@BindingAdapter("isDone")
fun bindIsCompleted(iv: ImageView, completed: Boolean?) {
    val done = completed == true
    iv.setImageResource(
        if (done) R.drawable.ic_circle_checked else R.drawable.ic_circle_unchecked
    )
    val tint = if (done) 0xFF66C23A.toInt() else 0xFFD0D0D0.toInt()
    ImageViewCompat.setImageTintList(iv, ColorStateList.valueOf(tint))
    iv.contentDescription = iv.context.getString(
        if (done) R.string.cd_mark_undone else R.string.cd_mark_done
    )
}