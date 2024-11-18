package com.example.parcial2lucasviacavaclima.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parcial2lucasviacavaclima.intent.ClimaIntencion
import com.example.parcial2lucasviacavaclima.model.Ciudad
import com.example.parcial2lucasviacavaclima.model.Clima
import com.example.parcial2lucasviacavaclima.model.Clouds
import com.example.parcial2lucasviacavaclima.model.Coordenadas
import com.example.parcial2lucasviacavaclima.model.ListForecast
import com.example.parcial2lucasviacavaclima.model.Pronostico
import com.example.parcial2lucasviacavaclima.model.Sys
import com.example.parcial2lucasviacavaclima.repository.RepositorioApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ClimaEstado {
    object Cargando : ClimaEstado()
    data class MostrarPronostico(val pronostico: Pronostico) : ClimaEstado()
    data class Error(val mensaje: String) : ClimaEstado()
}

class ClimaViewModel : ViewModel() {
    private val repositorio = RepositorioApi()

    private val _estado = MutableStateFlow<ClimaEstado>(ClimaEstado.Cargando)
    val estado: StateFlow<ClimaEstado> = _estado

    private val _ciudadesEncontradas = MutableStateFlow<List<Ciudad>>(emptyList())
    val ciudadesEncontradas: StateFlow<List<Ciudad>> = _ciudadesEncontradas

    fun procesarIntencion(intencion: ClimaIntencion) {
        viewModelScope.launch {
            when (intencion) {
                is ClimaIntencion.BuscarPronostico -> buscarPronostico(intencion.ciudad)
                ClimaIntencion.Compartir -> compartirPronostico()
                is ClimaIntencion.BuscarClimaPorUbicacion -> getWeatherForLocation(intencion.lat, intencion.lon)
            }
        }
    }

    fun getWeatherForLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            _estado.value = ClimaEstado.Cargando
            try {
                val climaHoy = repositorio.traerPronosticoPorUbicacion(lat, lon)
                Log.e("climaHoy", climaHoy.toString())
                val pronostico = Pronostico(
                    latitud = lat,
                    longitud = lon,
                    ciudad = Ciudad(
                        name = "Ubicación actual",
                        country = "N/A",
                        lat = lat,
                        lon = lon
                    ),
                    climaHoy = climaHoy,
                    climaSemana = emptyList()
                )
                Log.e("pronostico", pronostico.toString())
                _estado.value = ClimaEstado.MostrarPronostico(pronostico)
                Log.e("_estado.value", _estado.value.toString())
            } catch (e: Exception) {
                Log.e("error:: ", e.message.toString())
                _estado.value = ClimaEstado.Error("Error al obtener el pronóstico de la ubicación actual")
            }
        }
    }

    private suspend fun buscarPronostico(ciudad: Ciudad) {
        _estado.value = ClimaEstado.Cargando
        try {
            val pronosticoSemanal = repositorio.traerPronostico(ciudad.lat, ciudad.lon)
            val climaHoy = pronosticoSemanal.firstOrNull()?.toClima() ?: throw Exception("No se pudo obtener el clima de hoy")
            val pronostico = Pronostico(
                latitud = ciudad.lat,
                longitud = ciudad.lon,
                ciudad = ciudad,
                climaHoy = climaHoy,
                climaSemana = pronosticoSemanal.map { it.toClima() }
            )
            _estado.value = ClimaEstado.MostrarPronostico(pronostico)
        } catch (e: Exception) {
            _estado.value = ClimaEstado.Error("Error al obtener el pronóstico")
        }
    }

    fun buscarCiudad(ciudadNombre: String) {
        viewModelScope.launch {
            try {
                val resultados = repositorio.buscarCiudad(ciudadNombre)
                _ciudadesEncontradas.value = resultados
            } catch (e: Exception) {
                _ciudadesEncontradas.value = emptyList()
            }
        }
    }

    fun generarMensajeClima(pronostico: Pronostico): String {
        val mensaje = StringBuilder()
        mensaje.append("Clima para ${pronostico.ciudad.name}, ${pronostico.ciudad.country}:\n")
        mensaje.append("Cielo: ${pronostico.climaHoy.descripcion}\n")
        mensaje.append("Temperatura Máxima: ${pronostico.climaHoy.temperaturaMax}°C\n")
        mensaje.append("Temperatura Mínima: ${pronostico.climaHoy.temperaturaMin}°C\n")
        mensaje.append("Humedad: ${pronostico.climaHoy.main.humidity}%\n")

        if (pronostico.climaSemana.isNotEmpty()) {
            mensaje.append("\nPronóstico semanal:\n")
            pronostico.climaSemana.forEach { clima ->
                mensaje.append("- ${clima.dt_txt}: ${clima.descripcion}, ${clima.main.temp}°C\n")
            }
        }

        return mensaje.toString()
    }

    private fun compartirPronostico() {
        val estadoActual = _estado.value
        if (estadoActual is ClimaEstado.MostrarPronostico) {
            val mensaje = generarMensajeClima(estadoActual.pronostico)
            println("Compartir mensaje: $mensaje")
        } else {
            println("No hay pronóstico para compartir")
        }
    }

}

private fun ListForecast.toClima(): Clima {
    return Clima(
        coord = Coordenadas(0.0, 0.0),
        weather = weather,
        base = "stations",
        main = main,
        visibility = 10000,
        wind = wind,
        clouds = Clouds(0),
        dt = dt,
        sys = Sys(0, 0, "N/A", 0, 0),
        timezone = 0,
        id = 0,
        name = "N/A",
        cod = 200,
        dt_txt = dt_txt ?: "No disponible"
    )
}
