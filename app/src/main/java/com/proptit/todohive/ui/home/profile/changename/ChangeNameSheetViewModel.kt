package com.proptit.todohive.ui.home.profile.changename

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChangeNameSheetViewModel : ViewModel() {
    val name = MutableLiveData("")
    private val _isSaveEnabled = MutableLiveData(false)
    val isSaveEnabled: LiveData<Boolean> = _isSaveEnabled
    fun setInitialName(currentName: String) {
        if (name.value.isNullOrBlank()) {
            name.value = currentName
        }
    }
    fun onNameChanged(newValue: CharSequence?) {
        val text = newValue?.toString()?.trim().orEmpty()
        _isSaveEnabled.value = text.isNotEmpty()
    }
    fun getFinalName(): String? {
        val finalName = name.value?.trim()
        return if (!finalName.isNullOrEmpty()) finalName else null
    }
}
