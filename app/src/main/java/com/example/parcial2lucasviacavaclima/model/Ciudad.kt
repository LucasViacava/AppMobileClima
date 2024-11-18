package com.example.parcial2lucasviacavaclima.model

import kotlinx.serialization.Serializable

@Serializable
data class Ciudad(
    val name: String,
    val local_names: Map<String, String>? = null,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String? = null
)

