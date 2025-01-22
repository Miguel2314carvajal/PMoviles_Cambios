package com.grupo3.realidadaumentada.data

import com.grupo3.realidadaumentada.models.Faculty

object FacultyData {
    val faculties = listOf(
        Faculty(
            id = 1,
            name = "Facultad de Química",
            description = "Edificio 17 - Facultad de Ingeniería Química",
            latitude = -0.20972,
            longitude = -78.48877,
            modelPath = "models/dragon.glb",
            detectionRange = 20f
        ),
        Faculty(
            id = 2,
            name = "Facultad de Sistemas",
            description = "Facultad de Ingeniería en Sistemas",
            latitude = -0.21020,
            longitude = -78.48895,
            modelPath = "models/sofa.glb",
            detectionRange = 20f
        ),
        Faculty(
            id = 3,
            name = "Facultad de Ingeniería",
            description = "Edificio de Ingeniería",
            latitude = -0.20939,
            longitude = -78.48954,
            modelPath = "models/tom.glb",
            detectionRange = 20f
        ),
        Faculty(
            id = 4,
            name = "Facultad de Geología y Petroleos",
            description = "Facultad de Geología y Petroleos",
            latitude = -0.21096,
            longitude = -78.48920,
            modelPath = "models/scorpion.glb",
            detectionRange = 20f
        )
    )
} 