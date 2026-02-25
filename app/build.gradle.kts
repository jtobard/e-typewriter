import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.etypwwriter.launcher"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.etypwwriter.launcher"
        minSdk = 26
        // Para dispositivos como Onyx Boox: ./gradlew assembleRelease -PbooxCompatible
        targetSdk = if (project.findProperty("booxCompatible") == "true") 30 else 35
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        val keyPropsFile = rootProject.file("key.properties")
        if (keyPropsFile.exists()) {
            create("release") {
                val p = Properties()
                keyPropsFile.inputStream().use { p.load(it) }
                storeFile = rootProject.file(p["storeFile"]!! as String)
                storePassword = p["storePassword"]!! as String
                keyAlias = p["keyAlias"]!! as String
                keyPassword = p["keyPassword"]!! as String
            }
        }
    }

    buildTypes {
        release {
            if (rootProject.file("key.properties").exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-text")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.compose.material3:material3-window-size-class")
    
    // DataStore for saving preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.activity:activity-ktx:1.9.3")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("org.robolectric:robolectric:4.11.1")
}
