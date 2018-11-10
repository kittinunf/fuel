package com.example.fuel

import android.app.Application
import com.facebook.stetho.Stetho

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }
}