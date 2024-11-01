package com.example.assignment4.data

import com.example.assignment4.network.MarsApiService
import com.example.assignment4.network.PictureApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit


interface AppContainer {
    val pictureRepository: PictureRepository
    val marsPhotosRepository: MarsPhotosRepository
}

class DefaultAppContainer() : AppContainer {

    override val pictureRepository: PictureRepository by lazy {
        DefaultPicturesRepository(retrofitServicePicsum)
    }

    override val marsPhotosRepository: MarsPhotosRepository by lazy {
        NetworkMarsPhotosRepository(retrofitServiceMars)
    }

    private val retrofitMars: Retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(BASE_URL_MARS)
        .build()

    private val retrofitPicsum: Retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(BASE_URL_PICSUM)
        .build()

    private val retrofitServicePicsum: PictureApiService by lazy {
        retrofitPicsum.create(PictureApiService::class.java)
    }

    private val retrofitServiceMars: MarsApiService by lazy {
        retrofitMars.create(MarsApiService::class.java)
    }

    companion object {
        private const val BASE_URL_MARS = "https://android-kotlin-fun-mars-server.appspot.com/"
        private const val BASE_URL_PICSUM = "https://picsum.photos/"
    }
}