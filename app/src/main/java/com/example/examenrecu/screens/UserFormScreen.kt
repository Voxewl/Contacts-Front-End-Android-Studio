package com.example.examenrecu.screens

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val imagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        imageUrl = uri?.toString() ?: ""
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        }
    }

    LaunchedEffect(userId) {
        if (userId != null) {
            // Si es edición, cargar datos
            viewModel.loadUser(userId)
        } else {
            // Si es creación, limpiar formulario
            name = ""
            email = ""
            phone = ""
            imageUrl = ""
            selectedImageUri = null
        }
    }

    val user by viewModel.selectedUser.collectAsState()
    LaunchedEffect(user, userId) {
        // Solo cargar datos si estamos en modo edición
        if (userId != null) {
            user?.let {
                name = it.name
                email = it.email
                phone = it.phone
                imageUrl = it.imageUrl ?: ""
            }
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
                viewModel.updateUser(userId!!, userToSave) { onNavigateBack() }
            } else {
                viewModel.createUser(userToSave) { onNavigateBack() }
            }
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Editar Contacto" else "Nuevo Contacto",
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Cancelar", color = Color(0xFF757575))
                    }
                },
                actions = {
                    TextButton(
                        onClick = { handleSubmit() },
                        enabled = !isLoading
                    ) {
                        Text(
                            "Guardar",
                            color = if (isLoading) Color(0xFFBDBDBD) else Color(0xFF2196F3)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F5F5))
                        .clickable { permissionLauncher.launch(imagePermission) },
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
                            modifier = Modifier.size(50.dp),
                            tint = Color(0xFFBDBDBD)
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 8.dp, y = 8.dp)
                            .size(32.dp),
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Cambiar foto",
                            modifier = Modifier.padding(6.dp),
                            tint = Color(0xFF757575)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Agregar foto",
                    fontSize = 14.sp,
                    color = Color(0xFF757575)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = null
                        },
                        placeholder = { Text("Nombre completo", color = Color(0xFFBDBDBD)) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = nameError != null,
                        enabled = !isLoading,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            errorBorderColor = Color.Transparent,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }
                if (nameError != null) {
                    Text(
                        text = nameError!!,
                        color = Color(0xFFF44336),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                    )
                }

                Divider(color = Color(0xFFE0E0E0))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() } && it.length <= 10) {
                                phone = it
                                phoneError = null
                            }
                        },
                        placeholder = { Text("Teléfono", color = Color(0xFFBDBDBD)) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = phoneError != null,
                        enabled = !isLoading,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            errorBorderColor = Color.Transparent,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }
                if (phoneError != null) {
                    Text(
                        text = phoneError!!,
                        color = Color(0xFFF44336),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                    )
                } else {
                    Text(
                        text = "${phone.length}/10 dígitos",
                        color = Color(0xFF757575),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                    )
                }

                Divider(color = Color(0xFFE0E0E0))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                        },
                        placeholder = { Text("Correo electrónico", color = Color(0xFFBDBDBD)) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = emailError != null,
                        enabled = !isLoading,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            errorBorderColor = Color.Transparent,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }
                if (emailError != null) {
                    Text(
                        text = emailError!!,
                        color = Color(0xFFF44336),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                    )
                }

                Divider(color = Color(0xFFE0E0E0))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "O ingresa URL de imagen:",
                    fontSize = 12.sp,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(start = 40.dp)
                )

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = {
                        imageUrl = it
                        selectedImageUri = null
                    },
                    placeholder = { Text("https://ejemplo.com/foto.jpg", color = Color(0xFFBDBDBD)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 40.dp),
                    enabled = !isLoading,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }
        }
    }
}