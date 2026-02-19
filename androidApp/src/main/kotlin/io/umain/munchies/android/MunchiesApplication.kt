package io.umain.munchies.android

import android.app.Application
import io.umain.munchies.android.features.restaurant.di.registerAndroidUIWrappersModule
import io.umain.munchies.di.initKoin
import io.umain.munchies.feature.restaurant.di.registerFeatureRestaurantModule
import io.umain.munchies.feature.settings.di.registerFeatureSettingsModule
import org.koin.android.ext.koin.androidContext

class MunchiesApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        initKoin {
            androidContext(this@MunchiesApplication)
        }
        registerFeatureRestaurantModule()
        registerFeatureSettingsModule()
        registerAndroidUIWrappersModule()
    }
}
