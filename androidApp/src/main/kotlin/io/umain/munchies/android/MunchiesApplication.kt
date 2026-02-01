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
    }
}
