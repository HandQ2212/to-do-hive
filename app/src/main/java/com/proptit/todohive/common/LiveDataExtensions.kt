package com.proptit.todohive.common
// hoặc: package com.proptit.todohive.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

fun <A, B, R> LiveData<A>.combineWith(
    other: LiveData<B>,
    block: (A?, B?) -> R
): LiveData<R> = MediatorLiveData<R>().apply {
    var dataA: A? = null
    var dataB: B? = null
    fun update() { value = block(dataA, dataB) }
    addSource(this@combineWith) { a -> dataA = a; update() }
    addSource(other) { b -> dataB = b; update() }
}
