package com.ucaldas.taller6

data class Curso(
    val id: Int,
    val nombre: String,
    val docente: String,
    val descripcion: String,
    val esFavorito: Boolean = false
)