package com.example.parcial2lucasviacavaclima.repository

import android.util.Log
import com.example.parcial2lucasviacavaclima.model.Ciudad
import com.example.parcial2lucasviacavaclima.model.Clima
import com.example.parcial2lucasviacavaclima.model.ForecastDTO
import com.example.parcial2lucasviacavaclima.model.ListForecast
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class RepositorioApi : Repositorio {

    private val apiKey = "95e93e4f7a36fc511148468d1774792d"

    private val cliente = HttpClient() {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    override suspend fun buscarCiudad(ciudad: String): List<Ciudad> {
        return try {
            val respuesta = cliente.get("https://api.openweathermap.org/geo/1.0/direct") {
                parameter("q", ciudad)
                parameter("lang", "es")
                parameter("limit", 100)
                parameter("appid", apiKey)
            }

            if (respuesta.status == HttpStatusCode.OK) {
                val responseText = respuesta.bodyAsText()
                Log.d("API Response", responseText)
                Json.decodeFromString<List<Ciudad>>(responseText)
            } else {
                throw Exception("Error en la respuesta: ${respuesta.status}")
            }
        } catch (e: Exception) {
            Log.e("RepositorioApi", "Error al buscar ciudades", e)
            throw Exception("Error al buscar ciudades: ${e.message}")
        }
    }


    override suspend fun traerClima(lat: Float, lon: Float): Clima {
        val respuesta = cliente.get("https://api.openweathermap.org/data/2.5/weather") {
            parameter("lat", lat)
            parameter("lon", lon)
            parameter("lang", "es")
            parameter("units", "metric")
            parameter("appid", apiKey)
        }
        if (respuesta.status == HttpStatusCode.OK) {
            return respuesta.body()
        } else {
            throw Exception("Error fetching weather data")
        }
    }

    override suspend fun traerPronostico(lat: Double, lon: Double): List<ListForecast> {
        val respuesta = cliente.get("https://api.openweathermap.org/data/2.5/forecast") {
            parameter("lat", lat)
            parameter("lon", lon)
            parameter("lang", "es")
            parameter("units", "metric")
            parameter("appid", apiKey)
        }
        if (respuesta.status == HttpStatusCode.OK) {
            val forecast = respuesta.body<ForecastDTO>()
            return forecast.list
        } else {
            throw Exception("Error fetching forecast data")
        }
    }

    override suspend fun traerPronosticoPorUbicacion(lat: Double, lon: Double): Clima {
        val respuesta = cliente.get("https://api.openweathermap.org/data/2.5/weather") {
            parameter("lat", lat)
            parameter("lon", lon)
            parameter("lang", "es")
            parameter("units", "metric")
            parameter("appid", apiKey)
        }
        if (respuesta.status == HttpStatusCode.OK) {
            return respuesta.body()
        } else {
            val errorMsg = "Error fetching current weather data: ${respuesta.status} - ${respuesta.bodyAsText()}"
            Log.e("RepositorioApi", errorMsg)
            throw Exception(errorMsg)
        }
    }


}
