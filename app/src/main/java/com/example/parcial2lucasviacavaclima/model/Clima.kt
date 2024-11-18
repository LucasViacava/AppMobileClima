package com.example.parcial2lucasviacavaclima.model

import kotlinx.serialization.Serializable

@Serializable
data class Clima(
    val coord: Coordenadas,
    val weather: List<Weather>,
    val base: String,
    val main: Main,
    val visibility: Int,
    val wind: Wind,
    val clouds: Clouds,
    val dt: Long,
    val sys: Sys,
    val timezone: Int,
    val id: Int,
    val name: String,
    val cod: Int,
    val dt_txt: String? = null
) {
    val descripcion: String
        get() = weather.firstOrNull()?.description ?: "Desconocido"

    val temperaturaMax: Double
        get() = main.temp_max

    val temperaturaMin: Double
        get() = main.temp_min

    val fecha: String
        get() = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(dt * 1000))
}


@Serializable
data class Coordenadas(
    val lon: Double,
    val lat: Double
)

@Serializable
data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

@Serializable
data class Main(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val humidity: Int,
    val sea_level: Int? = null,
    val grnd_level: Int? = null
)

@Serializable
data class Wind(
    val speed: Double,
    val deg: Int
)

@Serializable
data class Clouds(
    val all: Int
)

@Serializable
data class Sys(
    val type: Int,
    val id: Int,
    val country: String,
    val sunrise: Long,
    val sunset: Long
)
