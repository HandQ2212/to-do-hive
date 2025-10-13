package com.proptit.todohive.ui.home.profile.changepassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChangePasswordSheetViewModel : ViewModel() {
    val oldPassword = MutableLiveData("")
    val newPassword = MutableLiveData("")

    private val _isSaveEnabled = MutableLiveData(false)
    val isSaveEnabled: LiveData<Boolean> = _isSaveEnabled

    fun onFieldChanged() {
        val ok = !oldPassword.value.isNullOrBlank() && !newPassword.value.isNullOrBlank()
        _isSaveEnabled.value = ok
    }

    fun getPair(): Pair<String, String>? {
        val o = oldPassword.value?.trim().orEmpty()
        val n = newPassword.value?.trim().orEmpty()
        return if (o.isNotEmpty() && n.isNotEmpty()) o to n else null
    }
}