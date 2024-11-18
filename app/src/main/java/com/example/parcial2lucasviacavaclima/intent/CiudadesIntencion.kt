package com.example.parcial2lucasviacavaclima.intent

import com.example.parcial2lucasviacavaclima.model.Ciudad

sealed class CiudadesIntencion {
    data class Buscar(val query: String) : CiudadesIntencion()
    data class Seleccionar(val ciudad: Ciudad) : CiudadesIntencion()
}
