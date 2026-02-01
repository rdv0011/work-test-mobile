package io.umain.munchies.android

import android.app.Application
import io.umain.munchies.di.initKoin
import io.umain.munchies.localization.PlatformTranslationService
import org.koin.android.ext.koin.androidContext

class MunchiesApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        PlatformTranslationService.init(this)
        
        initKoin {
            androidContext(this@MunchiesApplication)
        }
    }
}
