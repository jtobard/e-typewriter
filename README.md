# e-typewriter launcher (Version 1.0)

<p align="center">
  <img src="images/icon.png" width="200" alt="E-Typewriter Launcher Icon">
</p>

Este es un launcher minimalista para Android con una estética inspirada en las máquinas de escribir, centrado en la tipografía y la simplicidad.

## Estado actual: Version 1.0
Se han completado todas las fases iniciales de desarrollo, proporcionando una experiencia funcional y elegante.

### Funcionalidades Implementadas:
- **Pantalla Principal (Home):**
    - Reloj minimalista de gran formato.
    - Lista de hasta 8 aplicaciones favoritas.
    - Acceso rápido a la edición de favoritos.
- **Cajón de Aplicaciones (App Drawer):**
    - Acceso mediante deslizamiento vertical (swipe up).
    - Listado alfabético de todas las aplicaciones instaladas.
    - **Búsqueda Dinámica:** Filtrado en tiempo real mientras escribes.
    - **Doble Columna:** Soporte nativo para tablets y dispositivos plegables (foldables).
- **Personalización y Organización:**
    - **Renombrar Apps:** Posibilidad de asignar nombres personalizados a cualquier aplicación.
    - **Carpetas:** Creación y gestión de carpetas en el cajón de aplicaciones.
    - **Ocultar Apps:** Mantén tu cajón limpio ocultando las apps que no usas frecuentemente (accesibles bajo "Ver más...").
- **Sistema:**
    - Configuración como launcher por defecto integrada.
    - Detección automática de apps desinstaladas para liberar espacios en favoritos.

## Especificaciones Técnicas
- **Namespace:** `com.etypwwriter.launcher`
- **Minimum SDK:** 26 (Android 8.0)
- **Target SDK:** 35 (Android 15)
- **UI Framework:** Jetpack Compose (Material 3)
- **Lenguaje:** Kotlin

## Generar APK para instalar (fuera de la tienda)

Para que cualquiera pueda descargar e instalar el APK (por ejemplo desde GitHub), necesitas un APK **firmado** en modo release.

### 1. Crear un keystore (solo una vez)

En la raíz del proyecto (o en una carpeta que no subas a Git):

```bash
keytool -genkey -v -keystore e-typewriter-release.keystore -alias etypewriter -keyalg RSA -keysize 2048 -validity 10000
```

Te pedirá una contraseña y datos (nombre, organización, etc.). **Guarda el keystore y las contraseñas en un lugar seguro**: sin ellos no podrás actualizar la app con la misma firma.

### 2. Configurar la firma

- Copia `key.properties.example` a `key.properties` en la raíz del proyecto.
- Edita `key.properties` y pon la ruta real al `.keystore`, el alias y las contraseñas.

El archivo `key.properties` no se sube a Git (está en `.gitignore`).

### 3. Generar el APK

```bash
./gradlew assembleRelease
```

El APK quedará en:

**`app/build/outputs/apk/release/app-release.apk`**

### 4. Publicar para descarga (ej. GitHub)

- Crea un [Release](https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-your-repository) en el repositorio.
- En el release, sube el archivo `app-release.apk` como adjunto.
- Los usuarios descargan el APK, lo instalan y en el móvil deben permitir “Instalar desde fuentes desconocidas” (o “Instalar aplicaciones desconocidas”) para ese navegador o gestor de archivos.

**Importante:** Guarda una copia del `.keystore` y de las contraseñas. Para futuras actualizaciones del mismo app id necesitarás firmar con el mismo keystore.

## Roadmap

Importante:
- Capacidad para desinstalar Apps desde la lista.
- Sacar de la lista principal las app en carpetas. Pero seguir permitiendo buscarlas con las demas apps.

Deseables:
- Resumen de notificaciones en la pantalla home.
- Cambiar el fondo por imagen custom.
- Letras mas cercanas a una máquina de escribir.



---
*Diseñado para quienes buscan reducir distracciones y apreciar la estética de lo simple.*
