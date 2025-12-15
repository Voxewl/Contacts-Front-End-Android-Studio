package com.example.examenrecu.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import com.example.examenrecu.models.User
import com.example.examenrecu.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    viewModel: UserViewModel,
    onUserClick: (Int) -> Unit,
    onCreateUserClick: () -> Unit
) {
    val users by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    // Filtrar y agrupar usuarios
    val filteredUsers = remember(users, searchQuery) {
        users.filter { user ->
            user.name.contains(searchQuery, ignoreCase = true) ||
                    user.email.contains(searchQuery, ignoreCase = true) ||
                    user.phone.contains(searchQuery, ignoreCase = true)
        }
    }

    val groupedUsers = remember(filteredUsers) {
        filteredUsers.groupBy { it.name.first().uppercaseChar() }
            .toSortedMap()
    }

    LaunchedEffect(error, successMessage) {
        if (error != null || successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateUserClick,
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar contacto")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            // Header con título
            Text(
                text = "Contactos",
                fontSize = 28.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF212121),
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 12.dp)
            )

            // Barra de búsqueda
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFFF5F5F5),
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 16.sp,
                            color = Color(0xFF212121)
                        ),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Buscar",
                                    fontSize = 16.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de contactos
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading && users.isEmpty() -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFF2196F3)
                        )
                    }
                    error != null && users.isEmpty() -> {
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
                                onClick = { viewModel.loadUsers() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2196F3)
                                )
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                    filteredUsers.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (searchQuery.isEmpty())
                                    "No hay contactos registrados"
                                else
                                    "No se encontraron contactos",
                                color = Color(0xFF757575)
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            groupedUsers.forEach { (letter, contactsForLetter) ->
                                // Encabezado de letra
                                item {
                                    Text(
                                        text = letter.toString(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White)
                                            .padding(horizontal = 20.dp, vertical = 8.dp),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2196F3)
                                    )
                                }

                                // Contactos de esta letra
                                items(contactsForLetter) { user ->
                                    ContactListItem(
                                        user = user,
                                        onClick = { onUserClick(user.id!!) }
                                    )
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }

                // Snackbars
                error?.let {
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        containerColor = Color(0xFFF44336),
                        contentColor = Color.White
                    ) {
                        Text(it)
                    }
                }

                successMessage?.let {
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ) {
                        Text(it)
                    }
                }
            }
        }
    }
}

@Composable
fun ContactListItem(user: User, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Foto circular
        if (user.imageUrl != null && user.imageUrl.isNotBlank()) {
            AsyncImage(
                model = user.imageUrl,
                contentDescription = "Foto de ${user.name}",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color(0xFFE0E0E0)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = Color(0xFF757575)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Información del contacto
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF212121)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = user.email,
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
        }
    }
}