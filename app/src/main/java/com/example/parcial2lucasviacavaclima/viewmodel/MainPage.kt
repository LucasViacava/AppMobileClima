package com.example.parcial2lucasviacavaclima.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parcial2lucasviacavaclima.model.Ciudad
import com.example.parcial2lucasviacavaclima.model.Clima
import com.example.parcial2lucasviacavaclima.model.ListForecast
import com.example.parcial2lucasviacavaclima.viewmodel.ClimaEstado
import com.example.parcial2lucasviacavaclima.repository.RepositorioApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(
    climaEstado: ClimaEstado,
    ciudadesEncontradas: List<Ciudad>,
    onSearchCity: (String) -> Unit,
    onSelectCity: (Ciudad) -> Unit,
    onShowWeeklyForecast: (Double, Double) -> Unit,
    onShareClimate: (String) -> Unit,
    repositorioApi: RepositorioApi
) {
    var searchQuery by remember { mutableStateOf("") }
    var pronosticoSemanal by remember { mutableStateOf<List<ListForecast>?>(null) }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clima") },
                actions = {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            onSearchCity(it)
                        },
                        textStyle = TextStyle(fontSize = 18.sp, color = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(Color.Gray.copy(alpha = 0.2f))
                            .clip(MaterialTheme.shapes.medium)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Buscar ciudad...",
                                        color = Color.Black.copy(alpha = 0.5f)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                if (ciudadesEncontradas.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        items(ciudadesEncontradas) { ciudad ->
                            CiudadItem(ciudad = ciudad, onClick = { onSelectCity(ciudad) })
                        }
                    }
                }

                when (climaEstado) {
                    ClimaEstado.Cargando -> CircularProgressIndicator()
                    is ClimaEstado.Error -> Text(
                        text = climaEstado.mensaje,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    is ClimaEstado.MostrarPronostico -> {
                        ShowWeather(climaEstado.pronostico.climaHoy)
                        Spacer(modifier = Modifier.height(16.dp))
                        val coroutineScope = rememberCoroutineScope()

                        Button(
                            onClick = {
                                cargando = true
                                error = null
                                coroutineScope.launch {
                                    try {
                                        val resultado = repositorioApi.traerPronostico(
                                            climaEstado.pronostico.latitud ?: 0.0,
                                            climaEstado.pronostico.longitud ?: 0.0
                                        )
                                        pronosticoSemanal = resultado
                                    } catch (e: Exception) {
                                        error = "Error al obtener el pronóstico"
                                    } finally {
                                        cargando = false
                                    }
                                }
                            }
                        ) {
                            Text(text = "Ver Pronóstico Semanal")
                        }
                        Button(onClick = {
                            val mensaje = "Clima actual en ${climaEstado.pronostico.ciudad.name}:\n" +
                                    "Cielo: ${climaEstado.pronostico.climaHoy.descripcion}, " +
                                    "Temp: ${climaEstado.pronostico.climaHoy.main.temp}°C"
                            onShareClimate(mensaje)
                        }) {
                            Text("Compartir Clima")
                        }

                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                pronosticoSemanal?.let { forecastList ->
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(forecastList) { forecast ->
                            Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = "Fecha: ${forecast.dt_txt}")
                                    Text(text = "Temperatura: ${forecast.main.temp}°C")
                                    Text(text = "Descripción: ${forecast.weather[0].description}")
                                }
                            }
                        }
                    }
                }

                if (cargando) {
                    CircularProgressIndicator()
                }
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    )
}

@Composable
fun CiudadItem(ciudad: Ciudad, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = ciudad.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = ciudad.country, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun ShowWeather(clima: Clima) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Text(
            text = "Cielo: ${clima.descripcion}",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Temperatura: ${clima.temperaturaMax}°C",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = "Humedad: ${clima.main.humidity}%",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
