package com.proptit.todohive.ui.icons

import androidx.annotation.DrawableRes
import com.proptit.todohive.R

object CategoryIconRegistry {
    private val map = mapOf(
        "ic_university" to R.drawable.ic_flag,
        "ic_work" to R.drawable.ic_flag,
        "ic_home" to R.drawable.ic_flag,
        "ic_tag" to R.drawable.ic_tag,
        "ic_calendar" to R.drawable.ic_calendar,
    )

    @DrawableRes
    fun resolve(name: String?): Int =
        if (name.isNullOrBlank()) R.drawable.ic_tag else map[name] ?: R.drawable.ic_tag
}