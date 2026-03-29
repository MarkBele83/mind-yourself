plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "de.stroebele.mindyourself"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.stroebele.mindyourself"
        minSdk = 30  // Wear OS 3 / Android 11
        targetSdk = 35
        versionCode = 17
        versionName = "1.5.11"
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
    implementation(project(":shared:domain"))
    implementation(project(":shared:data"))
    implementation(project(":shared:sync"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.wear.compose.material)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.compose.navigation)
    implementation(libs.horologist.compose.layout)
    implementation(libs.horologist.compose.material)

    implementation(libs.health.services.client)
    implementation(libs.play.services.wearable)
    implementation(libs.play.services.location)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.coroutines.guava)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
