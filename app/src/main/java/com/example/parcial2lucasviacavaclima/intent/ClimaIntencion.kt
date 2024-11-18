package com.example.parcial2lucasviacavaclima.intent

import com.example.parcial2lucasviacavaclima.model.Ciudad

sealed class ClimaIntencion {
    data class BuscarPronostico(val ciudad: Ciudad) : ClimaIntencion()
    data class BuscarClimaPorUbicacion(val lat: Double, val lon: Double) : ClimaIntencion()
    object Compartir : ClimaIntencion()
}
