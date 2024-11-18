package com.example.parcial2lucasviacavaclima.model

import kotlinx.serialization.Serializable

@Serializable
data class ForecastDTO(
    val list: List<ListForecast>
)
