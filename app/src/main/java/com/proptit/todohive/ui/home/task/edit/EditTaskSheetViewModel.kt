package com.proptit.todohive.ui.home.task.edit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class EditTaskSheetViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val title = MutableLiveData(savedStateHandle.get<String>(EditTaskSheet.ARG_TITLE) ?: "")
    val description = MutableLiveData(savedStateHandle.get<String>(EditTaskSheet.ARG_DESC) ?: "")

    fun setTitle(newTitle: String) {
        title.value = newTitle
        savedStateHandle[EditTaskSheet.ARG_TITLE] = newTitle
    }

    fun setDescription(newDesc: String) {
        description.value = newDesc
        savedStateHandle[EditTaskSheet.ARG_DESC] = newDesc
    }
}