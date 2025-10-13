package com.proptit.todohive.data.local.model

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

data class CategoryOption(
    val id: Long?,
    val name: String,
    @ColorInt val color: Int,
    @DrawableRes val iconRes: Int
)