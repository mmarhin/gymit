plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

/*
 * Código y recursos que comparten la app del socio (:app) y la del torno
 * (:torno): el formato del pase QR, el tema de Compose con la paleta y el logo.
 * El formato del pase vive aquí para que quien lo genera y quien lo lee no
 * puedan desincronizarse; en el Bloque III la firma se añadirá en este módulo.
 */
android {
    namespace = "es.uma.seguridad.gymit.comun"
    compileSdk = 37

    defaultConfig {
        minSdk = 30
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    testImplementation(libs.junit)
}
