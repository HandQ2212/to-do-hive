package com.proptit.todohive.ui.home.task

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import com.proptit.todohive.data.local.AppDatabase
import com.proptit.todohive.data.local.entity.CategoryEntity

class CategoryViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.get(app).categoryDao()

    val query = MutableLiveData("")
    val categories: LiveData<List<CategoryEntity>> = dao.observeAll().asLiveData()

    val filtered: LiveData<List<CategoryEntity>> = MediatorLiveData<List<CategoryEntity>>().apply {
        fun applyFilter() {
            val q = query.value.orEmpty().trim().lowercase()
            val list = (categories.value ?: emptyList())
            value = if (q.isEmpty()) list else list.filter { it.name.lowercase().contains(q) }
        }
        addSource(categories) { applyFilter() }
        addSource(query) { applyFilter() }
    }

    val selectedId = MutableLiveData<Long?>(null)

    fun pick(category: CategoryEntity) {
        selectedId.value = category.category_id
    }
}