package com.ucaldas.taller6

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// OJO: este archivo NO importa nada de androidx.compose.*
class CursosViewModel : ViewModel() {

    // Única fuente de verdad: _cursos. No hay "var" duplicadas.
    private val _cursos = MutableStateFlow<List<Curso>>(emptyList())
    val cursos: StateFlow<List<Curso>> = _cursos

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando

    // Parte 4 - Opción A: estado derivado, calculado con .map sobre el flujo principal
    val totalFavoritos: StateFlow<Int> = _cursos
        .map { lista -> lista.count { it.esFavorito } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        cargarCursos()
    }

    fun cargarCursos() {
        viewModelScope.launch {
            _cargando.value = true
            delay(1000) // simula una espera de red
            // Si ya había datos (recarga), se conservan los favoritos marcados.
            val favoritosPrevios = _cursos.value.filter { it.esFavorito }.map { it.id }.toSet()
            _cursos.value = catalogoInicial().map { it.copy(esFavorito = it.id in favoritosPrevios) }
            _cargando.value = false
        }
    }

    // Acción del usuario: función pública del ViewModel; la View nunca toca el estado.
    fun marcarFavorito(id: Int) {
        _cursos.value = _cursos.value.map {
            if (it.id == id) it.copy(esFavorito = !it.esFavorito) else it
        }
    }

    // Para la pantalla de detalle: filtra por id sobre el mismo StateFlow.
    fun cursoPorId(id: Int): StateFlow<Curso?> = _cursos
        .map { lista -> lista.firstOrNull { it.id == id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), _cursos.value.firstOrNull { it.id == id })

    private fun catalogoInicial(): List<Curso> = listOf(
        Curso(
            1,
            "Programación para Dispositivos Móviles",
            "Prof. Ramírez",
            "Desarrollo Android con Kotlin y Jetpack Compose."
        ),
        Curso(2, "Estructuras de Datos", "Prof. Gómez", "Listas, pilas, colas, árboles y grafos."),
        Curso(3, "Bases de Datos", "Prof. Londoño", "Modelo relacional, SQL y normalización."),
        Curso(4, "Ingeniería de Software", "Prof. Duque", "Ciclo de vida, requisitos, diseño y pruebas."),
        Curso(5, "Redes de Computadores", "Prof. Arias", "Modelo TCP/IP, enrutamiento y capa de transporte."),
        Curso(6, "Sistemas Operativos", "Prof. Marín", "Procesos, memoria, concurrencia y archivos."),
        Curso(7, "Inteligencia Artificial", "Prof. Salazar", "Búsqueda, aprendizaje automático y agentes."),
        Curso(8, "Arquitectura de Software", "Prof. Ospina", "Patrones, MVVM, capas y decisiones de diseño."),
        Curso(9, "Seguridad Informática", "Prof. Giraldo", "Criptografía, autenticación y amenazas comunes."),
        Curso(10, "Computación en la Nube", "Prof. Cardona", "Servicios, contenedores y despliegue escalable."),
        Curso(11, "Interacción Humano-Computador", "Prof. Ocampo", "Usabilidad, accesibilidad y prototipado."),
        Curso(12, "Análisis de Algoritmos", "Prof. Henao", "Complejidad, recurrencias y técnicas de diseño.")
    )
}