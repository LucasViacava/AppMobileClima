package com.example.parcial2lucasviacavaclima.repository

import com.example.parcial2lucasviacavaclima.model.Ciudad
import com.example.parcial2lucasviacavaclima.model.Clima
import com.example.parcial2lucasviacavaclima.model.ListForecast

interface Repositorio {
    suspend fun buscarCiudad(ciudad: String): List<Ciudad>
    suspend fun traerClima(lat: Float, lon: Float): Clima
    suspend fun traerPronostico(lat: Double, lon: Double): List<ListForecast>
    suspend fun traerPronosticoPorUbicacion(lat: Double, lon: Double): Clima
}
