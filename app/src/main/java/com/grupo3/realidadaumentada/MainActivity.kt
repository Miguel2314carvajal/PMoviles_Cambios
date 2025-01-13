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
    private lateinit var placeSofaButton: ExtendedFloatingActionButton
    private lateinit var placeRobotButton: ExtendedFloatingActionButton
    private lateinit var placeTomButton: ExtendedFloatingActionButton
    private lateinit var placeRifleButton: ExtendedFloatingActionButton
    private lateinit var placeDragonButton: ExtendedFloatingActionButton
    private lateinit var sofaNode: ArModelNode
    private lateinit var robotNode: ArModelNode
    private lateinit var rifleNode: ArModelNode
    private lateinit var DragonNode: ArModelNode
    private lateinit var videoNode: VideoNode
    private lateinit var mediaPlayer:MediaPlayer
    private lateinit var tomNode: ArModelNode



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sceneView = findViewById(R.id.sceneView)
        sceneView.lightEstimationMode = Config.LightEstimationMode.DISABLED

        mediaPlayer = MediaPlayer.create(this, R.raw.ad)
        placeSofaButton = findViewById(R.id.placeSofa)
        placeRobotButton = findViewById(R.id.placeRobot)
        placeTomButton = findViewById(R.id.placeTom)
        placeRifleButton = findViewById(R.id.placeRifle)
        placeDragonButton = findViewById(R.id.placeDragon)

        initializeModels()

        placeSofaButton.setOnClickListener {
            placeSofa()
        }

        placeRobotButton.setOnClickListener {
            placeRobot()
        }

        placeTomButton.setOnClickListener {
            placeTom()
        }

        placeRifleButton.setOnClickListener {
            placeRifle()
        }

        placeDragonButton.setOnClickListener {
            placeDragon()
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

        // Inicializar tom
        tomNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply {
            loadModelGlbAsync(
                glbFileLocation = "models/tom.glb",
                scaleToUnits = 0.4f,
                centerOrigin = Position(-0.5f)
            )
        }

        // Inicializar Dragon
        DragonNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply {
            loadModelGlbAsync(
                glbFileLocation = "models/dragon.glb",
                scaleToUnits = 0.6f,
                centerOrigin = Position(-0.5f)
            )
        }

        // Configurar el video
        videoNode = VideoNode(
            sceneView.engine,
            scaleToUnits = 0.7f,
            centerOrigin = Position(y = -4f),
            glbFileLocation = "models/plane.glb",
            player = mediaPlayer
        ) { _, _ ->
            mediaPlayer.start()
        }

        // Agregar todos los modelos a la escena
        sceneView.addChild(sofaNode)
        sceneView.addChild(robotNode)
        sceneView.addChild(rifleNode)
        sceneView.addChild(tomNode)
        sceneView.addChild(DragonNode)
        sofaNode.addChild(videoNode)
    }

    private fun placeSofa() {
        sofaNode.position = Position(x = 0f, y = 0f, z = 0f)
        sofaNode.anchor()
        placeSofaButton.isGone = true
    }

    private fun placeRobot() {
        robotNode.position = Position(x = 1.5f, y = 0f, z = 0f)
        robotNode.anchor()
        placeRobotButton.isGone = true
    }

    private fun placeTom() {
        if (!sceneView.children.contains(tomNode)) {
            sceneView.addChild(tomNode)
        }
        tomNode.isVisible = true
        tomNode.anchor()
        placeTomButton.isGone = true
    }

    private fun placeRifle() {
        rifleNode.position = Position(x = -1.5f, y = 0f, z = 0f)
        rifleNode.anchor()
        placeRifleButton.isGone = true
    }

    private fun placeDragon() {
        DragonNode.position = Position(x = -1.5f, y = 0f, z = 0f)
        DragonNode.anchor()
        placeDragonButton.isGone = true
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