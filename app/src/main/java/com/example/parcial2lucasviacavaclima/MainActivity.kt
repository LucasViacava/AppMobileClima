package com.example.parcial2lucasviacavaclima

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.parcial2lucasviacavaclima.intent.ClimaIntencion
import com.example.parcial2lucasviacavaclima.ui.theme.Parcial2lucasviacavaclimaTheme
import com.example.parcial2lucasviacavaclima.view.MainPage
import com.example.parcial2lucasviacavaclima.viewmodel.ClimaViewModel
import com.example.parcial2lucasviacavaclima.repository.RepositorioApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>
    private val climaViewModel: ClimaViewModel by viewModels()
    private val repositorioApi = RepositorioApi()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getLastLocation()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }

        requestLocationPermission()

        setContent {
            val climaState = climaViewModel.estado.collectAsState()
            val ciudadesEncontradas = climaViewModel.ciudadesEncontradas.collectAsState()

            Parcial2lucasviacavaclimaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainPage(
                        climaEstado = climaState.value,
                        ciudadesEncontradas = ciudadesEncontradas.value,
                        onSearchCity = { cityName ->
                            climaViewModel.buscarCiudad(cityName)
                        },
                        onSelectCity = { ciudad ->
                            climaViewModel.procesarIntencion(ClimaIntencion.BuscarPronostico(ciudad))
                        },
                        onShowWeeklyForecast = { lat, lon ->
                            climaViewModel.procesarIntencion(ClimaIntencion.BuscarClimaPorUbicacion(lat, lon))
                            Toast.makeText(this, "Mostrando pronóstico semanal para $lat, $lon", Toast.LENGTH_SHORT).show()
                        },
                        onShareClimate = { mensaje ->
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, mensaje)
                            }
                            startActivity(Intent.createChooser(shareIntent, "Compartir clima con:"))
                        },
                        repositorioApi = repositorioApi
                    )
                }
            }
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getLastLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getLastLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    climaViewModel.getWeatherForLocation(latitude, longitude)
                } else {
                    Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
