package com.grupo3.realidadaumentada

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.*
import com.grupo3.realidadaumentada.adapters.FacultyAdapter
import com.grupo3.realidadaumentada.data.FacultyData
import com.grupo3.realidadaumentada.databinding.ActivityLocationsBinding
import com.grupo3.realidadaumentada.models.Faculty

class LocationsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLocationsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var facultyAdapter: FacultyAdapter
    private var currentLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLocationClient()
        setupRecyclerView()
        setupSearch()
        
        // Solicitar permisos y ubicación inmediatamente
        checkPermissionsAndRequestLocation()
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
            .setMinUpdateDistanceMeters(0.1f)
            .setMaxUpdateDelayMillis(100)
            .setWaitForAccurateLocation(false)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun setupRecyclerView() {
        facultyAdapter = FacultyAdapter(FacultyData.faculties) { faculty ->
            navigateToAR(faculty)
        }
        binding.facultiesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@LocationsActivity)
            adapter = facultyAdapter
        }
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener { text ->
            facultyAdapter.filter(text.toString())
        }
    }

    private fun checkPermissionsAndRequestLocation() {
        when {
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == 
                PackageManager.PERMISSION_GRANTED -> {
                startLocationUpdates()
                requestLastLocation()
            }
            else -> {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST
                )
            }
        }
    }

    private fun startLocationUpdates() {
        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
                .setMinUpdateDistanceMeters(0.1f)
                .setMaxUpdateDelayMillis(100)
                .setWaitForAccurateLocation(false)
                .build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                currentLocation = location
                // Actualizar inmediatamente
                updateDistancesImmediate(location)
            }
        }
    }

    private fun updateDistancesImmediate(location: Location) {
        FacultyData.faculties.forEach { faculty ->
            val facultyLocation = Location("").apply {
                latitude = faculty.latitude
                longitude = faculty.longitude
            }
            val distance = location.distanceTo(facultyLocation)
            runOnUiThread {
                facultyAdapter.updateDistance(faculty.id, distance)
            }
        }
    }

    private fun requestLastLocation() {
        try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location ->
                location?.let {
                    currentLocation = it
                    updateDistancesImmediate(it)
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun navigateToAR(faculty: Faculty) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(EXTRA_FACULTY_ID, faculty.id)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST && 
            grantResults.isNotEmpty() && 
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
            requestLastLocation()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST = 1000
        const val EXTRA_FACULTY_ID = "extra_faculty_id"
    }
} 