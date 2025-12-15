package com.example.examenrecu.models

import com.google.gson.annotations.SerializedName
data class User(
    val id: Int? = null,
    val name: String,
    val email: String,
    val phone: String,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class UserResponse(
    val data: List<User>
)

data class SingleUserResponse(
    val data: User
)