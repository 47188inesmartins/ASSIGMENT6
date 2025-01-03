package com.example.assignment4

import android.app.Application
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraXConfig
import androidx.room.Room
import com.example.assignment4.data.AppContainer
import com.example.assignment4.data.DefaultAppContainer
import com.example.assignment4.localStore.AppDatabase
import com.example.assignment4.localStore.PhotoDao

class MarsPhotosApplication : Application(),  CameraXConfig.Provider{
    /** AppContainer instance used by the rest of classes to obtain dependencies */
    lateinit var container: AppContainer
    lateinit var offlineContainer: PhotoDao
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
        offlineContainer = AppDatabase.getDatabase(this).photoDao()
    }

    override fun getCameraXConfig(): CameraXConfig {
        return CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
            .setAvailableCamerasLimiter(CameraSelector.DEFAULT_BACK_CAMERA)
            .build()
    }
}