package com.example.examenrecu.screens

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.examenrecu.models.User
import com.example.examenrecu.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFormScreen(
    userId: Int? = null,
    viewModel: UserViewModel,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val isEditMode = userId != null

    // Determinar qué permiso usar según la versión de Android
    val imagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    // Launcher para seleccionar imagen de la galería (DEBE IR PRIMERO)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        imageUrl = uri?.toString() ?: ""
    }

    // Launcher para solicitar permisos (USA imagePickerLauncher, por eso va después)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido, abrir galería
            imagePickerLauncher.launch("image/*")
        }
    }

    // Cargar datos si es edición
    LaunchedEffect(userId) {
        userId?.let {
            viewModel.loadUser(it)
        }
    }

    val user by viewModel.selectedUser.collectAsState()
    LaunchedEffect(user) {
        user?.let {
            name = it.name
            email = it.email
            phone = it.phone
            imageUrl = it.imageUrl ?: ""
        }
    }

    fun validateForm(): Boolean {
        var isValid = true

        if (name.isBlank()) {
            nameError = "El nombre es obligatorio"
            isValid = false
        } else {
            nameError = null
        }

        if (email.isBlank()) {
            emailError = "El email es obligatorio"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Email inválido"
            isValid = false
        } else {
            emailError = null
        }

        if (phone.isBlank()) {
            phoneError = "El teléfono es obligatorio"
            isValid = false
        } else if (phone.length < 10) {
            phoneError = "El teléfono debe tener al menos 10 dígitos"
            isValid = false
        } else {
            phoneError = null
        }

        return isValid
    }

    fun handleSubmit() {
        if (validateForm()) {
            val userToSave = User(
                name = name.trim(),
                email = email.trim(),
                phone = phone.trim(),
                imageUrl = if (imageUrl.isNotBlank()) imageUrl.trim() else null
            )

            if (isEditMode) {
                viewModel.updateUser(userId!!, userToSave) {
                    onNavigateBack()
                }
            } else {
                viewModel.createUser(userToSave) {
                    onNavigateBack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Editar Contacto" else "Nuevo Contacto") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Preview de la foto
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                            .clickable {
                                // Solicitar permiso antes de abrir galería
                                permissionLauncher.launch(imagePermission)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Foto seleccionada",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (imageUrl.isNotBlank() && imageUrl.startsWith("http")) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Foto actual",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Sin foto",
                                modifier = Modifier.size(60.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Ícono de cámara en la esquina
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(36.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Seleccionar foto",
                                modifier = Modifier.padding(8.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Toca para seleccionar foto",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Campo Nombre
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = null
                        },
                        label = { Text("Nombre completo *") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = nameError != null,
                        supportingText = {
                            nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        },
                        enabled = !isLoading,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Teléfono
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            // Solo permitir números y máximo 10 dígitos
                            if (it.all { char -> char.isDigit() } && it.length <= 10) {
                                phone = it
                                phoneError = null
                            }
                        },
                        label = { Text("Teléfono *") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = phoneError != null,
                        supportingText = {
                            if (phoneError != null) {
                                Text(phoneError!!, color = MaterialTheme.colorScheme.error)
                            } else {
                                Text(
                                    "${phone.length}/10 dígitos",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        enabled = !isLoading,
                        singleLine = true,
                        placeholder = { Text("1234567890") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                        },
                        label = { Text("Correo electrónico *") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = emailError != null,
                        supportingText = {
                            emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        enabled = !isLoading,
                        singleLine = true,
                        placeholder = { Text("usuario@ejemplo.com") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo URL de Imagen (alternativo)
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = {
                            imageUrl = it
                            selectedImageUri = null // Limpiar selección local
                        },
                        label = { Text("O ingresa URL de foto") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        singleLine = true,
                        placeholder = { Text("https://ejemplo.com/foto.jpg") },
                        supportingText = {
                            Text(
                                "Puedes usar una URL o seleccionar de la galería",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón de guardar
                    Button(
                        onClick = { handleSubmit() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(if (isEditMode) "Actualizar Contacto" else "Guardar Contacto")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "* Campos obligatorios",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}