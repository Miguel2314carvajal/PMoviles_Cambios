package com.grupo3.realidadaumentada.data

import com.grupo3.realidadaumentada.models.Faculty

object FacultyData {
    val faculties = listOf(
        Faculty(
            id = 1,
            name = "Facultad de Ingeniería",
            description = "Edificio de Ingeniería",
            latitude = -34.9204,
            longitude = -56.1678,
            modelPath = "models/dragon.glb"
        ),
        Faculty(
            id = 2,
            name = "Facultad de Arquitectura",
            description = "Edificio de Arquitectura",
            latitude = -34.9215,
            longitude = -56.1665,
            modelPath = "models/sofa.glb"
        )
    )
} 