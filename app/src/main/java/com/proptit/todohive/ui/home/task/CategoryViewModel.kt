package com.proptit.todohive.ui.home.task

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.proptit.todohive.data.local.AppDatabase
import com.proptit.todohive.data.local.entity.CategoryEntity
import java.util.Locale

class CategoryViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.get(app).categoryDao()

    private val currentUserId: Long by lazy {
        val prefs = app.getSharedPreferences("app", Context.MODE_PRIVATE)
        prefs.getLong("current_user_id", 0L).also {
            require(it > 0L) { "No logged-in user. current_user_id missing in SharedPreferences." }
        }
    }

    val query = MutableLiveData("")
    val categories: LiveData<List<CategoryEntity>> =
        dao.observeVisibleForUser(currentUserId)

    val filtered: LiveData<List<CategoryEntity>> =
        MediatorLiveData<List<CategoryEntity>>().apply {
            fun applyFilter() {
                val q = query.value.orEmpty().trim().lowercase(Locale.getDefault())
                val list = categories.value.orEmpty()
                value = if (q.isEmpty()) {
                    list
                } else {
                    list.filter { it.name.lowercase(Locale.getDefault()).contains(q) }
                }
            }
            addSource(categories) { applyFilter() }
            addSource(query) { applyFilter() }
        }

    val selectedId = MutableLiveData<Long?>(null)

    fun pick(category: CategoryEntity) {
        selectedId.value = category.category_id
    }
}
