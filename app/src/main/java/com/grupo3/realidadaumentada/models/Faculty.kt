package com.grupo3.realidadaumentada.models

data class Faculty(
    val id: Int,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val modelPath: String = "models/tom.glb"
) 