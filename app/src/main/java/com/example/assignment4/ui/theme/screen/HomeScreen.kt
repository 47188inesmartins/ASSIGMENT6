package com.example.assignment4.ui.theme.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.assignment4.R
import com.example.assignment4.model.MarsPhoto
import com.example.assignment4.model.Picture
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val viewModel: MarsViewModel = viewModel(factory = MarsViewModel.Factory)
    val uiState = viewModel.marsUiState
    val uiStatePicSum = viewModel.picturesUiState
    val rollsState = viewModel.rollsUiState
    val pictureTaken = viewModel.picTakenUiState
    val context = LocalContext.current
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    val file = remember { context.createImageFile() }

    val uri = FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        "${context.packageName}.provider", file
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                capturedImageUri = uri
                viewModel.savePictureTaken(capturedImageUri.toString())
            }
        }
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Mars Pictures Retrieved: ${uiState.photos.size}")
        if(uiState.currentPhoto != null)
            MarsPhotoCard(
                modifier = Modifier.weight(1f),
                photo = uiState.currentPhoto
            )
        Text(text = "Pictures Retrieved: ${uiStatePicSum.picturesList.size}")
        if (uiStatePicSum.currentPicture != null)
            PicPhotoCard(
                modifier = Modifier.weight(1f),
                picture = uiStatePicSum.currentPicture
            )
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) {
                cameraLauncher.launch(uri)
            }

            Button(onClick = {
                val permissionCheckResult = ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.CAMERA
                )
                if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                    cameraLauncher.launch(uri)
                } else {
                    permissionLauncher.launch(android.Manifest.permission.CAMERA)
                }
            }) {
                Text(text = "Take Photo")
            }
        }
        Row  (modifier = Modifier
                .wrapContentWidth()
                .padding(16.dp)
        ){
            if (capturedImageUri != null) {
                viewModel.updatePhotoTaken(capturedImageUri.toString())
                AsyncImage(
                    model = capturedImageUri,
                    contentDescription = "Captured Photo",
                    modifier = Modifier
                        .weight(1f)
                        .size(300.dp)
                        .padding(16.dp)
                )
            }
            if (pictureTaken.uri.isNotEmpty() || viewModel.numberSavedLocally.uri.isNotEmpty()) {
                Box(modifier = Modifier
                    .weight(1f)
                ) {
                    if(pictureTaken.uri.isNotEmpty()){
                        AsyncImage(
                            model = pictureTaken.uri,
                            contentDescription = "Saved Photo",
                            modifier = Modifier
                                .size(300.dp)
                                .padding(16.dp)
                        )
                    } else {
                        AsyncImage(
                            model = viewModel.numberSavedLocally.uri,
                            contentDescription = "Saved Photo",
                            modifier = Modifier
                                .size(300.dp)
                                .padding(16.dp)
                        )
                    }

                    Column {
                        Text(text = "Saved Before", fontSize = 14.sp)
                        if(viewModel.numberSavedLocally.uri.isNotEmpty())
                            Text(text = "This image saved locally", fontSize = 14.sp)
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .weight(0.5f)
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ){
            ActionButton(modifier = Modifier.weight(1f),{ viewModel.rollPicture() },{ viewModel.rollPictureMars() },"Roll")
            ActionButton(modifier = Modifier.weight(1f),{ viewModel.applyBlur() },{},"Blur")
            ActionButton(modifier = Modifier.weight(1f),{ viewModel.applyGrayScale() },{},"Gray")
            ActionButton(modifier = Modifier.weight(1f),{ viewModel.savePicture() },{},"Save")
            ActionButton(modifier = Modifier.weight(1f),{ viewModel.readDataFromFirebase() },{},"Load")

        }
        Text(text = "Rolls Retrieved: ${rollsState.rolls}")
    }
}

@Composable
fun MarsPhotoCard(photo: MarsPhoto, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context = LocalContext.current)
                .data(photo.imgSrc)
                .size(200, 200)
                .crossfade(true).build(),
            error = painterResource(R.drawable.ic_broken_image),
            placeholder = painterResource(R.drawable.loading_img),
            contentDescription = stringResource(R.string.mars_photo),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(300.dp)

        )
    }
}

@Composable
fun PicPhotoCard(picture: Picture, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(picture.download_url)
                    .size(200,200)
                    .build()
            ),
            contentDescription = null,
            modifier = Modifier
                .size(300.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(2.dp, Color.Gray, RoundedCornerShape(8.dp))
        )
    }
}

@Composable
fun ActionButton(modifier: Modifier = Modifier,actionFunction:() -> Unit,actionFunction1:() -> Unit = {}, buttonMessage: String){
    Button(onClick = {
        actionFunction()
        actionFunction1()
    }) {
        Text(text = buttonMessage)
    }
}

@SuppressLint("SimpleDateFormat")
fun Context.createImageFile(): File {
    // Create an image file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(
        imageFileName, /* prefix */
        ".jpg", /* suffix */
        externalCacheDir /* directory */
    )
    return image
}


