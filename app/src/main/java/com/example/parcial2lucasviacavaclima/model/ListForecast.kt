package com.example.parcial2lucasviacavaclima.model

import kotlinx.serialization.Serializable

@Serializable
data class ListForecast(
    val dt_txt: String? = null,
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind
)
