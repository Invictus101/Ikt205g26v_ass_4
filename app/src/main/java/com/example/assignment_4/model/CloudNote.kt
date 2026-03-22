package com.example.assignment_4.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CloudNote(
    @SerialName("id")
    val id: String? = null,

    @SerialName("title")
    val title: String,

    @SerialName("content")
    val content: String,

    @SerialName("owner_id")
    val ownerId: String,

    @SerialName("owner_email")
    val ownerEmail: String,

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null
)