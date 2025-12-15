package com.example.examenrecu.repository

import com.example.examenrecu.models.User
import com.example.examenrecu.network.RetrofitClient

class UserRepository {
    private val api = RetrofitClient.apiService

    // INDEX - Obtener todos los usuarios
    suspend fun getUsers(): Result<List<User>> {
        return try {
            val response = api.getUsers()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Error al obtener usuarios: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    // SHOW - Obtener un usuario específico
    suspend fun getUser(id: Int): Result<User> {
        return try {
            val response = api.getUser(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Error al obtener usuario: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    // STORE - Crear un nuevo usuario
    suspend fun createUser(user: User): Result<User> {
        return try {
            val response = api.createUser(user)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Error al crear usuario: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    // UPDATE - Actualizar un usuario existente
    suspend fun updateUser(id: Int, user: User): Result<User> {
        return try {
            val response = api.updateUser(id, user)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Error al actualizar usuario: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    // DESTROY - Eliminar un usuario
    suspend fun deleteUser(id: Int): Result<Boolean> {
        return try {
            val response = api.deleteUser(id)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al eliminar usuario: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }
}