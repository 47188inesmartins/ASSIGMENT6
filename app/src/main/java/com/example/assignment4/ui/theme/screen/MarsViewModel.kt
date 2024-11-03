package com.example.assignment4.ui.theme.screen

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.assignment4.MarsPhotosApplication
import com.example.assignment4.data.MarsPhotosRepository
import com.example.assignment4.data.PictureRepository
import com.example.assignment4.localStore.AppDatabase
import com.example.assignment4.localStore.PhotoDao
import com.example.assignment4.localStore.PhotoEntity
import com.example.assignment4.model.MarsPhoto
import com.example.assignment4.model.Picture
import com.example.assignment4.model.PicturesSavedInformation
import com.example.assignment4.network.DataCallback
import com.example.assignment4.network.FireBaseAccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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

data class PictureTaken(
    val uri: String = ""
)

data class PictureTakenLocally(
    val uri: String = ""
)

class MarsViewModel(
    private val marsPhotosRepository: MarsPhotosRepository,
    private val picturesRepository: PictureRepository,
    private val localPictureRepository: PhotoDao,
) : ViewModel() {
    /** The mutable State that stores the status of the most recent request */
    var marsUiState: MarsUiState by mutableStateOf(MarsUiState())
    var picturesUiState: PicturesUiState by  mutableStateOf(PicturesUiState())
    var rollsUiState: NumberOfRolls by  mutableStateOf(NumberOfRolls())
    var picTakenUiState: PictureTaken by  mutableStateOf(PictureTaken())
    var numberSavedLocally: PictureTakenLocally by  mutableStateOf(PictureTakenLocally())

    /**
     * Call getMarsPhotos() on init so we can display status immediately.
     */
    init {
        getMarsPhotos()
        getPictures()
        getPictureNumber()
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
            Log.i("Firebase123", picTakenUiState.uri)
            FireBaseAccess()
                .writeData(
                    picTakenUiState.uri,
                    marsUiState.currentPhoto!!.imgSrc,
                    picturesUiState.currentPicture!!.download_url,
                    rollsUiState.rolls,
                )
        }
    }

    fun updatePhotoTaken(uri: String){
        picTakenUiState = picTakenUiState.copy(uri = uri)
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
                    rollsUiState = rollsUiState.copy(rolls = data.rolls)
                    picTakenUiState = picTakenUiState.copy(uri = data.capturePhoto)
                    Log.i("Firebase123", picTakenUiState.uri)
                    Log.i("Firebase","Received Data")
                } else {
                    Log.i("Firebase","Not received Data")
                }
            }
        })
    }

    fun savePictureTaken(uri: String){
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                localPictureRepository.insertPhoto(PhotoEntity(uri = uri))
                Log.i("Picture Room", "Saved on local database")
            }
        }
    }

    private fun getPictureNumber(){
        viewModelScope.launch {
            val lastPicture = withContext(Dispatchers.IO) {
                val allPictures = localPictureRepository.getAll()
                    if (allPictures.isNotEmpty()) {
                        allPictures.last().uri ?: ""
                    }else{
                        ""
                    }
            }

            numberSavedLocally = PictureTakenLocally(uri = lastPicture)
            Log.i("Picture Room", "Last image saved: $lastPicture")
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
                val roomRepository = application.offlineContainer
                MarsViewModel(
                    marsPhotosRepository = marsPhotosRepository,
                    picturesRepository = picsPhotosRepository,
                    roomRepository
                )
            }
        }
    }

}