package com.example.examenrecu.network

import com.example.examenrecu.models.SingleUserResponse
import com.example.examenrecu.models.User
import com.example.examenrecu.models.UserResponse
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // GET /users - Obtener todos los usuarios en orden alfabético (INDEX)
    @GET("users")
    suspend fun getUsers(): Response<UserResponse>

    // GET /users/{id} - Obtener un usuario específico (SHOW)
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Int): Response<SingleUserResponse>

    // POST /users - Crear un nuevo contacto (STORE)
    @POST("users")
    suspend fun createUser(@Body user: User): Response<SingleUserResponse>

    // PUT /users/{id} - Actualizar un contacto (UPDATE)
    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body user: User
    ): Response<SingleUserResponse>

    // DELETE /users/{id} - Eliminar un contacto (DESTROY)
    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<Unit>
}