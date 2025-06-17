# Aplicación Android - Gestión de Cadena Logística

Este proyecto es una aplicación Android desarrollada como parte de un sistema de gestión de cadena logística. Permite la interacción con distintos dispositivos y sistemas de la cadena, proporcionando funcionalidades como monitorización de operaciones, captura de datos y control de procesos.

## Estructura del repositorio

    .idea/ → Configuración de Android Studio
    
    app/ → Módulo principal de la aplicación Android
    
    └── src/main/
    
        ├── java/... → Código fuente (dominio, actividades, lógica de negocio)
    
        └── res/ → Recursos de la aplicación (layouts, imágenes, strings, etc)
    
    gradle/ → Archivos auxiliares de configuración Gradle
    
    models/ → Modelos de transcripción utilizados en algunas funciones de captura de voz
    
    .gitignore → Exclusiones de control de versiones para Git
    
    build.gradle.kts → Configuración principal de Gradle (Kotlin DSL)
    
    gradle.properties → Propiedades globales de compilación
    
    gradlew / gradlew.bat → Wrappers para ejecución de Gradle
    
    settings.gradle.kts → Configuración de módulos del proyecto


## Descripción general

La aplicación se conecta a distintos puntos de la cadena logística, permitiendo:

- Supervisión de procesos en tiempo real.
- Captura de datos de operaciones.
- Interacción con dispositivos físicos.
- Funciones auxiliares de captura de voz en algunos flujos específicos.

## Requisitos de desarrollo

- **Android Studio** (recomendado: última versión estable)
- **JDK 17** (o la versión especificada en `gradle.properties`)
- Acceso a los modelos de transcripción (almacenados en `models/`) para las funcionalidades de voz.
cd <NOMBRE_DEL_PROYECTO>
