package com.example.assignment_4

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.assignment_4.data.NoteImageValidator
import com.example.assignment_4.model.CloudNote

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    note: CloudNote?,
    isLoading: Boolean,
    message: String?,
    onSaveNew: (String, String, Uri?) -> Unit,
    onUpdate: (String, String, String, Uri?) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var imageError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(note) {
        title = note?.title ?: ""
        content = note?.content ?: ""
        selectedImageUri = null
        imageError = null
    }

    val isEditing = note != null

    fun validateAndStage(uri: Uri) {
        try {
            NoteImageValidator.validateOrThrow(context, uri)
            selectedImageUri = uri
            imageError = null
        } catch (e: Exception) {
            imageError = e.message ?: "Ugyldig bilde."
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraUri?.let { validateAndStage(it) }
        } else {
            imageError = "Bildet ble ikke tatt."
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            validateAndStage(uri)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val newUri = ImageUriFactory.createTempImageUri(context)
            tempCameraUri = newUri
            takePictureLauncher.launch(newUri)
        } else {
            imageError = "Kameratilgang ble avslått."
        }
    }

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pickImageLauncher.launch("image/*")
        } else {
            imageError = "Tilgang til bilder ble avslått."
        }
    }

    val galleryPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val imageToPreview: Any? = selectedImageUri ?: note?.imageUrl

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditing) "Rediger notat" else "Nytt notat")
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tittel") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Innhold") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            OutlinedButton(
                onClick = {
                    imageError = null
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ta bilde")
            }

            OutlinedButton(
                onClick = {
                    imageError = null
                    galleryPermissionLauncher.launch(galleryPermission)
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Velg fra galleri")
            }

            if (imageToPreview != null) {
                Text(
                    text = "Forhåndsvisning",
                    style = MaterialTheme.typography.titleMedium
                )

                AsyncImage(
                    model = imageToPreview,
                    contentDescription = "Valgt bilde",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                OutlinedButton(
                    onClick = {
                        selectedImageUri = null
                        imageError = null
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fjern nytt valgt bilde")
                }
            }

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text("Laster opp / lagrer notat...")
                CircularProgressIndicator()
            }

            Button(
                onClick = {
                    if (isEditing) {
                        onUpdate(note!!.id!!, title, content, selectedImageUri)
                    } else {
                        onSaveNew(title, content, selectedImageUri)
                    }
                },
                enabled = !isLoading && title.isNotBlank() && content.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "Oppdater" else "Lagre")
            }

            Button(
                onClick = onBack,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tilbake")
            }

            if (!imageError.isNullOrBlank()) {
                Text(
                    text = imageError!!,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (!message.isNullOrBlank()) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}