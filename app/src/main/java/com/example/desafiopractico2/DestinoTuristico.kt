package com.example.desafiopractico2

    data class DestinoTuristico(
        var id: String = "",
        val nombre: String = "",
        val pais: String = "",
        val precio: Double = 0.0,
        val descripcion: String = "",
        var imagenUrl: String = ""
    )