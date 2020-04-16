package com.tp.forestfiremonitor.di

import androidx.room.Room
import com.tp.forestfiremonitor.data.ForestFireMonitorDatabase
import com.tp.forestfiremonitor.data.area.repository.AreaDataSource
import com.tp.forestfiremonitor.data.area.repository.AreaRepository
import com.tp.forestfiremonitor.map.viewmodel.MapViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class KoinModules {

    private val componentsModule = module {
        single {
            Room.databaseBuilder(
                androidContext().applicationContext,
                ForestFireMonitorDatabase::class.java,
                "FOREST_FIRE_MONITOR.db"
            ).build()
        }
    }

    private val repositoriesModule = module {
        single<AreaDataSource> { AreaRepository(get<ForestFireMonitorDatabase>().areaDao()) }
    }

    private val viewModelsModule = module {
        viewModel { MapViewModel(get()) }
    }

    fun getAllModules() = listOf(componentsModule, repositoriesModule, viewModelsModule)
}