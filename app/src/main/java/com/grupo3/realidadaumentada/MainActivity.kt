package com.grupo3.realidadaumentada

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.isGone
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.ar.core.Config
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.AugmentedImageNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.material.setExternalTexture
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.VideoNode
import com.grupo3.realidadaumentada.models.Faculty
import com.grupo3.realidadaumentada.data.FacultyData
import android.Manifest
import android.location.Location
import android.location.LocationManager
import android.content.pm.PackageManager
import android.location.LocationListener
import android.widget.TextView
import android.widget.ImageView
import com.grupo3.realidadaumentada.util.CompassHelper
import android.os.Handler
import android.os.Looper

class MainActivity : AppCompatActivity() {

    private lateinit var sceneView: ArSceneView
    private lateinit var placeModelButton: ExtendedFloatingActionButton
    private lateinit var modelNode: ArModelNode
    private lateinit var navigationArrow: ArModelNode
    private lateinit var distanceText: TextView
    private var currentFaculty: Faculty? = null
    private lateinit var locationManager: LocationManager
    private lateinit var compassHelper: CompassHelper
    private lateinit var arrowImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        distanceText = findViewById(R.id.distanceText)
        distanceText.text = "Obteniendo ubicación..."
        distanceText.visibility = android.view.View.VISIBLE

        val facultyId = intent.getIntExtra(LocationsActivity.EXTRA_FACULTY_ID, -1)
        currentFaculty = FacultyData.faculties.find { it.id == facultyId }

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        compassHelper = CompassHelper(this)
        arrowImageView = findViewById(R.id.arrowImageView)
        
        setupAR()
        setupUI()
        initializeModel()
        setupNavigationArrow()
        
        // Iniciar ubicación inmediatamente
        startLocationUpdates()

        // Ocultar el botón inicialmente
        placeModelButton.visibility = android.view.View.GONE
    }

    private fun setupAR() {
        sceneView = findViewById(R.id.sceneView)
        sceneView.lightEstimationMode = Config.LightEstimationMode.DISABLED
    }

    private fun setupUI() {
        placeModelButton = findViewById<ExtendedFloatingActionButton>(R.id.placeModel).apply {
            text = "Colocar Modelo en ${currentFaculty?.name}"
            setOnClickListener { placeModel() }
        }
    }

    private fun initializeModel() {
        modelNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply {
            loadModelGlbAsync(
                glbFileLocation = currentFaculty?.modelPath ?: "models/dragon.glb",
                scaleToUnits = 1.0f,
                centerOrigin = Position(x = 0.0f, y = 0.0f, z = 0.0f)
            )
            onAnchorChanged = { anchor ->
                placeModelButton.isEnabled = anchor == null
            }
        }
        sceneView.addChild(modelNode)
        modelNode.position = Position(0f, 0f, -2f)
    }

    private fun placeModel() {
        if (!this::modelNode.isInitialized) {
            initializeModel()
        }
        modelNode.anchor()
        placeModelButton.isEnabled = false
    }

    private fun setupNavigationArrow() {
        navigationArrow = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply {
            loadModelGlbAsync(
                glbFileLocation = "models/navigation_arrow.glb",
                scaleToUnits = 0.3f,
                centerOrigin = Position(x = 0.0f, y = 0.0f, z = 0.0f)
            )
            position = Position(0f, 0.5f, -1f)
        }
        sceneView.addChild(navigationArrow)
    }

    private fun startLocationUpdates() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == 
            PackageManager.PERMISSION_GRANTED) {
            try {
                // Solicitar actualizaciones más frecuentes
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    100L, // 100ms
                    0.1f, // 0.1 metros
                    locationListener
                )
                
                // Obtener última ubicación conocida inmediatamente
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let { location ->
                    locationListener.onLocationChanged(location)
                }
                
                // Intentar con el proveedor de red si el GPS no responde
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    100L,
                    0.1f,
                    locationListener
                )
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    private fun formatDistance(distance: Float): String {
        return when {
            distance >= 1000 -> {
                String.format("%.2f km", distance / 1000)
            }
            else -> {
                String.format("%.0f m", distance)
            }
        }
    }

    private val locationListener = LocationListener { location ->
        currentFaculty?.let { faculty ->
            val facultyLocation = Location("").apply {
                latitude = faculty.latitude
                longitude = faculty.longitude
            }
            val distance = location.distanceTo(facultyLocation)
            val bearing = location.bearingTo(facultyLocation)
            
            runOnUiThread {
                val currentRotation = arrowImageView.rotation
                val targetRotation = bearing
                val smoothRotation = smoothRotation(currentRotation, targetRotation)
                arrowImageView.rotation = smoothRotation
                
                // Mostrar/ocultar botón según la distancia
                placeModelButton.visibility = if (distance <= faculty.detectionRange) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }
                
                when {
                    distance <= faculty.detectionRange -> {
                        distanceText.text = "¡Has llegado a\n${faculty.name}!"
                        distanceText.setBackgroundResource(R.drawable.success_background)
                    }
                    else -> {
                        distanceText.text = "${faculty.name}\nDistancia: ${formatDistance(distance)}"
                    }
                }
                distanceText.visibility = android.view.View.VISIBLE
            }
        }
    }

    private fun smoothRotation(current: Float, target: Float): Float {
        var delta = target - current
        
        // Normalizar el delta para que esté entre -180 y 180
        while (delta > 180) delta -= 360
        while (delta < -180) delta += 360
        
        // Suavizar el movimiento
        return current + (delta * 0.1f)
    }

    private fun calculateBearing(faculty: Faculty): Float {
        val currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        return currentLocation?.let { location ->
            val facultyLocation = Location("").apply {
                latitude = faculty.latitude
                longitude = faculty.longitude
            }
            location.bearingTo(facultyLocation)
        } ?: 0f
    }

    override fun onPause() {
        super.onPause()
        compassHelper.stop()
        try {
            locationManager.removeUpdates(locationListener)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
        compassHelper.start { rotation ->
            currentFaculty?.let { faculty ->
                val bearing = calculateBearing(faculty)
                runOnUiThread {
                    arrowImageView.rotation = rotation.y.toFloat() + bearing
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            locationManager.removeUpdates(locationListener)
            compassHelper.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        sceneView.destroy()
    }
}