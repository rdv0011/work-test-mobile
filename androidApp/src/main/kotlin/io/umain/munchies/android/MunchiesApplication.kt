package io.umain.munchies.android

import android.app.Application
import io.umain.munchies.di.initKoin
import org.koin.android.ext.koin.androidContext

class MunchiesApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        initKoin {
            androidContext(this@MunchiesApplication)
        }
        // Register feature modules after Koin initialization
        io.umain.munchies.feature.restaurant.di.registerFeatureRestaurantModule()
        // Register Android-specific UI wrappers
        org.koin.core.context.loadKoinModules(
            org.koin.dsl.module {
                // Android wrappers that expose lifecycle-aware ViewModels
                org.koin.androidx.viewmodel.dsl.viewModel {
                    io.umain.munchies.android.features.restaurantlist.RestaurantListAndroidViewModel(get())
                }
                org.koin.androidx.viewmodel.dsl.viewModel {
                    io.umain.munchies.android.features.restaurantdetail.RestaurantDetailAndroidViewModel(get())
                }
            }
        )
    }
}
