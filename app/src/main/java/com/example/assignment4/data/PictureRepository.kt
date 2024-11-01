package com.example.assignment4.data

import com.example.assignment4.model.Picture
import com.example.assignment4.network.PictureApiService

interface PictureRepository{

    suspend fun getPicture(): List<Picture>
}

class DefaultPicturesRepository(
    private val picturesApiService: PictureApiService
) : PictureRepository {
    override suspend fun getPicture(): List<Picture> =
        picturesApiService.getPicture()
}