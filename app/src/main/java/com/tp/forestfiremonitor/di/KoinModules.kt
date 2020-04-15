package com.tp.forestfiremonitor.di

import com.tp.forestfiremonitor.map.MapViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class KoinModules {

    private val viewModelsModule = module {
        viewModel { MapViewModel() }
    }

    fun getAllModules() = listOf(viewModelsModule)
}