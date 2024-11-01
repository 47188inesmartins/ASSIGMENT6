package com.example.assignment4.network

import com.example.assignment4.model.Picture
import retrofit2.http.GET

interface PictureApiService {
    @GET("v2/list")
    suspend fun getPicture(): List<Picture>
}