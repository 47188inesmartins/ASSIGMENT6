package com.example.assignment4.localStore

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PhotoDao {

    @Insert
    fun insertPhoto(photoEntity: PhotoEntity)

    @Query("SELECT * FROM PhotoEntity")
    fun getAll(): List<PhotoEntity>
}