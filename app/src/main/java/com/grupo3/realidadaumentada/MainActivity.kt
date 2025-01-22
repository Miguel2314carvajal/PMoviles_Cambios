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

class MainActivity : AppCompatActivity() {

    private lateinit var sceneView: ArSceneView
    private lateinit var placeModelButton: ExtendedFloatingActionButton
    private lateinit var modelNode: ArModelNode
    private lateinit var navigationArrow: ArModelNode
    private lateinit var distanceText: TextView
    private var currentFaculty: Faculty? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val facultyId = intent.getIntExtra(LocationsActivity.EXTRA_FACULTY_ID, -1)
        currentFaculty = FacultyData.faculties.find { it.id == facultyId }

        setupAR()
        setupUI()
        initializeModel()
        setupNavigationArrow()
        startLocationUpdates()
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
                glbFileLocation = "models/arrow.glb",
                scaleToUnits = 0.5f,
                centerOrigin = Position(x = 0.0f, y = 0.0f, z = 0.0f)
            )
            position = Position(0f, 1.5f, -2f)
        }
        sceneView.addChild(navigationArrow)
    }

    private fun startLocationUpdates() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == 
            PackageManager.PERMISSION_GRANTED) {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                100L,
                0.1f,
                locationListener
            )
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
                navigationArrow.rotation = Rotation(0f, bearing, 0f)
                distanceText.text = String.format("%.2f metros", distance)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sceneView.destroy()
    }
}