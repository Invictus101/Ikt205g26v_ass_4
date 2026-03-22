package com.example.assignment_4

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.assignment_4.model.CloudNote

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    notes: List<CloudNote>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    canLoadMore: Boolean,
    message: String?,
    onAddClick: () -> Unit,
    onEditClick: (CloudNote) -> Unit,
    onDeleteClick: (CloudNote) -> Unit,
    onLoadMoreClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val noteToDelete = remember { mutableStateOf<CloudNote?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jobb Notater") },
                actions = {
                    TextButton(onClick = onLogoutClick) {
                        Text("Logg ut")
                    }
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onAddClick,
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Nytt notat")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!message.isNullOrBlank()) {
                Text(
                    text = message,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                notes.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Ingen notater ennå.")
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = notes,
                            key = { note -> note.id ?: "${note.title}-${note.updatedAt}" }
                        ) { note ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onEditClick(note) }
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (!note.imageUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = note.imageUrl,
                                            contentDescription = "Bilde for notat ${note.title}",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    Text(
                                        text = note.title,
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Text(
                                        text = note.content,
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Text(
                                        text = "Av: ${note.ownerEmail}",
                                        style = MaterialTheme.typography.bodySmall
                                    )

                                    Text(
                                        text = "Sist endret: ${note.updatedAt ?: "Ukjent"}",
                                        style = MaterialTheme.typography.bodySmall
                                    )

                                    Button(
                                        onClick = { noteToDelete.value = note },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Slett")
                                    }
                                }
                            }
                        }

                        item {
                            if (canLoadMore) {
                                Button(
                                    onClick = onLoadMoreClick,
                                    enabled = !isLoadingMore,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(if (isLoadingMore) "Laster..." else "Last mer")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    noteToDelete.value?.let { note ->
        AlertDialog(
            onDismissRequest = { noteToDelete.value = null },
            title = { Text("Bekreft sletting") },
            text = { Text("Er du sikker på at du vil slette \"${note.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick(note)
                        noteToDelete.value = null
                    }
                ) {
                    Text("Ja, slett")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { noteToDelete.value = null }
                ) {
                    Text("Avbryt")
                }
            }
        )
    }
}