package com.tp.base.lifecycle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

@Suppress("UNCHECKED_CAST")
class MultipleLiveData<T> : MediatorLiveData<T>() {

    private var values = mutableListOf<Any?>(null, null, null, null)

    fun <K, S> addSources(
        source1: LiveData<K>,
        source2: LiveData<S>,
        onChanged: (value1: K, value2: S) -> Unit
    ) {
        addSource(source1) {
            values[0] = it
            update(onChanged)
        }
        addSource(source2) {
            values[1] = it
            update(onChanged)
        }
    }

    private fun <K, S> update(onChanged: (value1: K, value2: S) -> Unit) {
        val localValue1 = values[0] as? K
        val localValue2 = values[1] as? S
        if (localValue1 != null && localValue2 != null) {
            onChanged(localValue1, localValue2)
        }
    }

    fun <K, S, Z> addSources(
        source1: LiveData<K>,
        source2: LiveData<S>,
        source3: LiveData<Z>,
        onChanged: (value1: K, value2: S, value3: Z) -> Unit
    ) {
        addSource(source1) {
            values[0] = it
            update(onChanged)
        }
        addSource(source2) {
            values[1] = it
            update(onChanged)
        }
        addSource(source3) {
            values[2] = it
            update(onChanged)
        }
    }

    private fun <K, S, Z> update(onChanged: (value1: K, value2: S, value3: Z) -> Unit) {
        val localValue1 = values[0] as? K
        val localValue2 = values[1] as? S
        val localValue3 = values[2] as? Z
        if (localValue1 != null && localValue2 != null && localValue3 != null) {
            onChanged(localValue1, localValue2, localValue3)
        }
    }

    fun <K, S, Z, V> addSources(
        source1: LiveData<K>,
        source2: LiveData<S>,
        source3: LiveData<Z>,
        source4: LiveData<V>,
        onChanged: (value1: K, value2: S, value3: Z, value4: V) -> Unit
    ) {
        addSource(source1) {
            values[0] = it
            update(onChanged)
        }
        addSource(source2) {
            values[1] = it
            update(onChanged)
        }
        addSource(source3) {
            values[2] = it
            update(onChanged)
        }
        addSource(source4) {
            values[3] = it
            update(onChanged)
        }
    }

    private fun <K, S, Z, V> update(onChanged: (value1: K, value2: S, value3: Z, value4: V) -> Unit) {
        val localValue1 = values[0] as? K
        val localValue2 = values[1] as? S
        val localValue3 = values[2] as? Z
        val localValue4 = values[3] as? V
        if (localValue1 != null && localValue2 != null && localValue3 != null && localValue4 != null) {
            onChanged(localValue1, localValue2, localValue3, localValue4)
        }
    }
}