package com.example.examenrecu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examenrecu.models.User
import com.example.examenrecu.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val repository = UserRepository()

    // Lista de usuarios (INDEX)
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    // Usuario seleccionado (SHOW)
    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser: StateFlow<User?> = _selectedUser

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Mensajes de error
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Mensajes de éxito
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    init {
        loadUsers()
    }

    // INDEX - Cargar todos los usuarios
    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getUsers().fold(
                onSuccess = {
                    _users.value = it
                    _error.value = null
                },
                onFailure = {
                    _error.value = it.message ?: "Error desconocido"
                }
            )
            _isLoading.value = false
        }
    }

    // SHOW - Cargar un usuario específico
    fun loadUser(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getUser(id).fold(
                onSuccess = {
                    _selectedUser.value = it
                    _error.value = null
                },
                onFailure = {
                    _error.value = it.message ?: "Error al cargar usuario"
                }
            )
            _isLoading.value = false
        }
    }

    // STORE - Crear un nuevo usuario
    fun createUser(user: User, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.createUser(user).fold(
                onSuccess = {
                    _successMessage.value = "Usuario creado exitosamente"
                    loadUsers() // Recargar la lista
                    onSuccess()
                },
                onFailure = {
                    _error.value = it.message ?: "Error al crear usuario"
                }
            )
            _isLoading.value = false
        }
    }

    // UPDATE - Actualizar un usuario existente
    fun updateUser(id: Int, user: User, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.updateUser(id, user).fold(
                onSuccess = {
                    _successMessage.value = "Usuario actualizado exitosamente"
                    loadUsers() // Recargar la lista
                    loadUser(id) // Recargar el usuario actual
                    onSuccess()
                },
                onFailure = {
                    _error.value = it.message ?: "Error al actualizar usuario"
                }
            )
            _isLoading.value = false
        }
    }

    // DESTROY - Eliminar un usuario
    fun deleteUser(id: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.deleteUser(id).fold(
                onSuccess = {
                    _successMessage.value = "Usuario eliminado exitosamente"
                    loadUsers() // Recargar la lista
                    onSuccess()
                },
                onFailure = {
                    _error.value = it.message ?: "Error al eliminar usuario"
                }
            )
            _isLoading.value = false
        }
    }

    // Limpiar mensajes de error y éxito
    fun clearMessages() {
        _error.value = null
        _successMessage.value = null
    }
}