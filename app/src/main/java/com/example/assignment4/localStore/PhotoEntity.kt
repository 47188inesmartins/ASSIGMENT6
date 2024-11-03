package com.example.assignment4.localStore

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PhotoEntity (
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    val uri: String?
)