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

class MainActivity : AppCompatActivity() {

    private lateinit var sceneView: ArSceneView
    lateinit var placeButton: ExtendedFloatingActionButton
    private lateinit var sofaNode: ArModelNode
    private lateinit var robotNode: ArModelNode
    private lateinit var rifleNode: ArModelNode
    private lateinit var videoNode: VideoNode
    private lateinit var mediaPlayer:MediaPlayer
    private lateinit var dragonNode: ArModelNode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sceneView = findViewById(R.id.sceneView)
        sceneView.lightEstimationMode = Config.LightEstimationMode.DISABLED

        mediaPlayer = MediaPlayer.create(this, R.raw.ad)
        placeButton = findViewById(R.id.place)

        initializeModels()

        placeButton.setOnClickListener {
            placeModels()
        }
    }

    private fun initializeModels() {
        // Inicializar sofa
        sofaNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply {
            loadModelGlbAsync(
                glbFileLocation = "models/sofa.glb",
                scaleToUnits = 1f,
                centerOrigin = Position(-0.5f)
            )
        }

        // Inicializar robot
        robotNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply {
            loadModelGlbAsync(
                glbFileLocation = "models/scorpion.glb",
                scaleToUnits = 0.5f,
                centerOrigin = Position(-0.5f)
            )
        }

        // Inicializar rifle
        rifleNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply {
            loadModelGlbAsync(
                glbFileLocation = "models/rifle.glb",
                scaleToUnits = 0.3f,
                centerOrigin = Position(-0.5f)
            )
        }

        // Inicializar dragón
        dragonNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply {
            loadModelGlbAsync(
                glbFileLocation = "models/dragon.glb",
                scaleToUnits = 1f,
                centerOrigin = Position(-0.5f, 7f)
            )
        }

        // Agregar todos los modelos a la escena
        sceneView.addChild(sofaNode)
        sceneView.addChild(robotNode)
        sceneView.addChild(rifleNode)
        sceneView.addChild(dragonNode)

        // Configurar el video en el sofaNode
        videoNode = VideoNode(
            sceneView.engine,
            scaleToUnits = 0.7f,
            centerOrigin = Position(y = -4f),
            glbFileLocation = "models/plane.glb",
            player = mediaPlayer
        ) { _, _ ->
            mediaPlayer.start()
        }
        sofaNode.addChild(videoNode)
    }

    private fun placeModels() {
        // Colocar sofa en la posición central
        sofaNode.position = Position(x = 0f, y = 0f, z = 0f)
        sofaNode.anchor()

        // Colocar robot a la derecha
        robotNode.position = Position(x = 1.5f, y = 0f, z = 0f)
        robotNode.anchor()

        // Colocar rifle a la izquierda
        rifleNode.position = Position(x = -1.5f, y = 0f, z = 0f)
        rifleNode.anchor()

        // Colocar dragón detrás de los otros modelos
        dragonNode.position = Position(x = -0f, y = -0f, z = -2f)
        dragonNode.anchor()

        sceneView.planeRenderer.isVisible = false
        placeButton.isGone = true
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer.stop()
    }
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

}