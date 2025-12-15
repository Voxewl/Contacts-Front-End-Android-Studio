package com.example.examenrecu.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.examenrecu.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    userId: Int,
    viewModel: UserViewModel,
    onNavigateBack: () -> Unit,
    onEditClick: (Int) -> Unit
) {
    val user by viewModel.selectedUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.loadUser(userId)
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color(0xFF212121)
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { onEditClick(userId) }) {
                        Text(
                            "Editar",
                            color = Color(0xFF2196F3),
                            fontSize = 14.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF2196F3)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error ?: "Error desconocido",
                            color = Color(0xFFF44336)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadUser(userId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            )
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
                user != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))

                        // Foto grande
                        if (user!!.imageUrl != null && user!!.imageUrl!!.isNotBlank()) {
                            AsyncImage(
                                model = user!!.imageUrl,
                                contentDescription = "Foto de ${user!!.name}",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Surface(
                                modifier = Modifier.size(120.dp),
                                shape = CircleShape,
                                color = Color(0xFFE0E0E0)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.padding(30.dp),
                                    tint = Color(0xFF757575)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Nombre
                        Text(
                            text = user!!.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF212121)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Botones de acción (solo diseño, no funcionales)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ActionButton(
                                icon = Icons.Default.Send,
                                label = "Mensaje",
                                color = Color(0xFF2196F3)
                            )
                            ActionButton(
                                icon = Icons.Default.Phone,
                                label = "Llamar",
                                color = Color(0xFF2196F3)
                            )
                            ActionButton(
                                icon = Icons.Default.VideoCall,
                                label = "Video",
                                color = Color(0xFF2196F3)
                            )
                            ActionButton(
                                icon = Icons.Default.Email,
                                label = "Correo",
                                color = Color(0xFF2196F3)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Información de contacto
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        ) {
                            // Teléfono
                            ContactInfoItem(
                                label = "móvil",
                                value = user!!.phone,
                                icon = Icons.Default.Phone
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Email
                            ContactInfoItem(
                                label = "correo electrónico",
                                value = user!!.email,
                                icon = Icons.Default.Email
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // Botón eliminar
                            TextButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Eliminar Contacto",
                                    color = Color(0xFFF44336),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar contacto") },
            text = { Text("¿Estás seguro de que deseas eliminar este contacto?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteUser(userId) {
                            showDeleteDialog = false
                            onNavigateBack()
                        }
                    }
                ) {
                    Text("Eliminar", color = Color(0xFFF44336))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = Color(0xFF757575))
                }
            }
        )
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = Color(0xFFE3F2FD)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.padding(12.dp),
                tint = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF757575)
        )
    }
}

@Composable
fun ContactInfoItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF757575)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 16.sp,
                color = Color(0xFF2196F3)
            )
        }
    }
}