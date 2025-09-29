package com.proptit.todohive.ui.home.binding


import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.proptit.todohive.R
import com.proptit.todohive.data.local.entity.CategoryEntity
import com.proptit.todohive.ui.icons.CategoryIconRegistry

@BindingAdapter("bindCategory")
fun Chip.bindCategory(category: CategoryEntity?) {
    val label = category?.name?.takeIf { it.isNotBlank() }
        ?: context.getString(R.string.category_default)
    text = label
    contentDescription = label
    setTextColor(ContextCompat.getColor(context, android.R.color.white))

    val colorInt = runCatching { Color.parseColor(category?.color_hex ?: "#6C63FF") }
        .getOrElse { Color.parseColor("#6C63FF") }
    chipBackgroundColor = ColorStateList.valueOf(colorInt)

    val iconRes = CategoryIconRegistry.resolve(category?.icon)
    chipIcon = ContextCompat.getDrawable(context, iconRes)
    isChipIconVisible = true
    chipIconTint = ColorStateList.valueOf(
        ContextCompat.getColor(context, android.R.color.white)
    )
}

@BindingAdapter("cardBackgroundColorHex")
fun MaterialCardView.setCardBackgroundColorHex(hex: String?) {
    val color = try { Color.parseColor(hex ?: "#6C63FF") } catch (_: Throwable) { Color.parseColor("#6C63FF") }
    setCardBackgroundColor(color)
}

@BindingAdapter("srcByName")
fun ImageView.setSrcByName(name: String?) {
    val resId = name?.takeIf { it.isNotBlank() }?.let { resName ->
        context.resources.getIdentifier(resName, "drawable", context.packageName)
    } ?: 0
    if (resId != 0) setImageResource(resId)
    else setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_tag))
}