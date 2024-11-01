package com.example.assignment4.ui.theme.screen

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.assignment4.MarsPhotosApplication
import com.example.assignment4.data.MarsPhotosRepository
import com.example.assignment4.data.PictureRepository
import com.example.assignment4.model.MarsPhoto
import com.example.assignment4.model.Picture
import com.example.assignment4.model.PicturesSavedInformation
import com.example.assignment4.network.DataCallback
import com.example.assignment4.network.FireBaseAccess
import com.google.firebase.Firebase
import kotlinx.coroutines.launch


/**
 * UI state for the Home screen
 */
data class MarsUiState (
    val photos: List<MarsPhoto> = emptyList(),
    val currentPhoto: MarsPhoto? = null
)

data class PicturesUiState(
    val picturesList: List<Picture> = emptyList(),
    val currentPicture: Picture? = null
)

data class NumberOfRolls(
    val rolls: Int = 0
)

class MarsViewModel(
    private val marsPhotosRepository: MarsPhotosRepository,
    private val picturesRepository: PictureRepository
) : ViewModel() {
    /** The mutable State that stores the status of the most recent request */
    var marsUiState: MarsUiState by mutableStateOf(MarsUiState())
    var picturesUiState: PicturesUiState by  mutableStateOf(PicturesUiState())
    var rollsUiState: NumberOfRolls by  mutableStateOf(NumberOfRolls())

    /**
     * Call getMarsPhotos() on init so we can display status immediately.
     */
    init {
        getMarsPhotos()
        getPictures()
    }

    /**
     * Gets Mars photos information from the Mars API Retrofit service and updates the
     * [MarsPhoto] [List] [MutableList].
     */
    private fun getMarsPhotos() {
        viewModelScope.launch {
            val getPics = marsPhotosRepository.getMarsPhotos()
            marsUiState = MarsUiState(
                getPics,
                getPics.random()
            )
        }
    }

    fun rollPictureMars() {
        val newRandomPicture = marsUiState.photos.random()
        val currentRolls = rollsUiState.rolls
        rollsUiState = rollsUiState.copy(rolls = currentRolls + 1)
        marsUiState = marsUiState.copy(currentPhoto = newRandomPicture)
    }

    private fun getPictures() {
        viewModelScope.launch {
            val pictures = picturesRepository.getPicture()
            val randomPicture = pictures.random()
            picturesUiState = PicturesUiState(pictures, randomPicture)
        }
    }

    fun rollPicture() {
        val newRandomPicture = picturesUiState.picturesList.random()
        picturesUiState = picturesUiState.copy(currentPicture = newRandomPicture)
    }

    fun applyBlur() {
        picturesUiState.currentPicture?.let { current ->
            val blurredPicture = current.copy(
                download_url = "${current.download_url}?blur=4"
            )
            picturesUiState = picturesUiState.copy(currentPicture = blurredPicture)
        }
    }

    fun applyGrayScale() {
        picturesUiState.currentPicture?.let { current ->
            val grayScalePicture = current.copy(
                download_url = "${current.download_url}?grayscale"
            )
            picturesUiState = picturesUiState.copy(currentPicture = grayScalePicture)
        }
    }

    fun savePicture(){
        if (marsUiState.currentPhoto != null && picturesUiState.currentPicture != null){
            FireBaseAccess()
                .writeData(
                    marsUiState.currentPhoto!!.imgSrc,
                    picturesUiState.currentPicture!!.download_url,
                    rollsUiState.rolls
                )
        }
    }

    fun readDataFromFirebase(){
        FireBaseAccess()
            .readData(object : DataCallback {
            override fun onDataReceived(data: PicturesSavedInformation?) {
                if (data != null) {
                    val picture = picturesUiState.picturesList.find { it.download_url == data.pictureInfo }
                    val mars = marsUiState.photos.find { it.imgSrc == data.marsInfo }

                    picturesUiState = picturesUiState.copy(currentPicture = picture)
                    marsUiState = marsUiState.copy(currentPhoto = mars)
                    rollsUiState = rollsUiState.copy(data.rolls)

                    Log.i("Firebase","Received Data")
                } else {
                    Log.i("Firebase","Not received Data")
                }
            }
        })
    }

    val REQUEST_IMAGE_CAPTURE = 1

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
           // startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }

    /**
     * Factory for [MarsViewModel] that takes [MarsPhotosRepository] as a dependency
     */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MarsPhotosApplication)
                val marsPhotosRepository = application.container.marsPhotosRepository
                val picsPhotosRepository = application.container.pictureRepository

                MarsViewModel(
                    marsPhotosRepository = marsPhotosRepository,
                    picturesRepository = picsPhotosRepository
                )
            }
        }
    }


}