package com.example.assignment4

import android.app.Application
import com.example.assignment4.data.AppContainer
import com.example.assignment4.data.DefaultAppContainer

class MarsPhotosApplication : Application() {
    /** AppContainer instance used by the rest of classes to obtain dependencies */
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}