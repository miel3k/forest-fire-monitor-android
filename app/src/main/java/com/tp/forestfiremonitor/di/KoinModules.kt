package com.tp.forestfiremonitor.di

import androidx.room.Room
import com.tp.forestfiremonitor.data.ForestFireMonitorDatabase
import com.tp.forestfiremonitor.data.area.repository.AreaDataSource
import com.tp.forestfiremonitor.data.area.repository.AreaRepository
import com.tp.forestfiremonitor.data.fire.remote.FireService
import com.tp.forestfiremonitor.data.fire.repository.FireDataSource
import com.tp.forestfiremonitor.data.fire.repository.FireRepository
import com.tp.forestfiremonitor.presentation.viewmodel.MapViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class KoinModules {

    private val componentsModule = module {
        single {
            Room.databaseBuilder(
                androidContext().applicationContext,
                ForestFireMonitorDatabase::class.java,
                "FOREST_FIRE_MONITOR.db"
            ).build()
        }
        single {
            Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080")
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
        }
    }

    private val repositoriesModule = module {
        single<AreaDataSource> { AreaRepository(get<ForestFireMonitorDatabase>().areaDao()) }
        single<FireDataSource> { FireRepository(get<Retrofit>().create(FireService::class.java)) }
    }

    private val viewModelsModule = module {
        viewModel { MapViewModel(get(), get()) }
    }

    fun getAllModules() = listOf(componentsModule, repositoriesModule, viewModelsModule)
}