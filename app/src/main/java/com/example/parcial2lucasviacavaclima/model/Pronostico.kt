package com.example.parcial2lucasviacavaclima.model

import kotlinx.serialization.Serializable

@Serializable
data class Pronostico(
    val latitud: Double? = null,
    val longitud: Double? = null,
    val ciudad: Ciudad,
    val climaHoy: Clima,
    val climaSemana: List<Clima> = emptyList()
)
