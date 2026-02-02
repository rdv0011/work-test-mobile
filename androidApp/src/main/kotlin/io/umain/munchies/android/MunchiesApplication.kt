package io.umain.munchies.android

import android.app.Application
import io.umain.munchies.android.features.restaurant.di.registerAndroidUIWrappersModule
import io.umain.munchies.di.initKoin
import io.umain.munchies.feature.restaurant.di.registerFeatureRestaurantModule
import org.koin.android.ext.koin.androidContext

class MunchiesApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        initKoin {
            androidContext(this@MunchiesApplication)
        }
        // Register feature modules after Koin initialization
        registerFeatureRestaurantModule()
        // Register Android-specific UI wrappers
        registerAndroidUIWrappersModule()
    }
}
