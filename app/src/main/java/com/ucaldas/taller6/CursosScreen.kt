package com.ucaldas.taller6

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaScreen(
    viewModel: CursosViewModel,
    onCursoClick: (Int) -> Unit
) {
    val cursos by viewModel.cursos.collectAsStateWithLifecycle()
    val cargando by viewModel.cargando.collectAsStateWithLifecycle()
    val totalFavoritos by viewModel.totalFavoritos.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Cursos  ★ $totalFavoritos") }) }
    ) { padding ->
        if (cargando) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cursos, key = { it.id }) { curso ->
                    Card(Modifier.fillMaxWidth().clickable { onCursoClick(curso.id) }) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(curso.nombre, style = MaterialTheme.typography.titleMedium)
                                Text(curso.docente, style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { viewModel.marcarFavorito(curso.id) }) {
                                Icon(
                                    imageVector = if (curso.esFavorito) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = "Marcar favorito"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    id: Int,
    viewModel: CursosViewModel,
    onBack: () -> Unit
) {
    val flujoCurso = remember(id) { viewModel.cursoPorId(id) }
    val curso by flujoCurso.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        val c = curso
        if (c == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Curso no encontrado")
            }
        } else {
            Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(c.nombre, style = MaterialTheme.typography.headlineSmall)
                Text("Docente: ${c.docente}")
                Text(c.descripcion)
                Button(onClick = { viewModel.marcarFavorito(c.id) }) {
                    Text(if (c.esFavorito) "Quitar de favoritos" else "Marcar como favorito")
                }
            }
        }
    }
}