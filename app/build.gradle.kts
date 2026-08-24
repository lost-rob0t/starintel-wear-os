plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "actor.starintel.wear"
    compileSdkPreview = "CinnamonBun"

    defaultConfig {
        applicationId = "actor.starintel.wear"
        minSdk = 30
        targetSdkPreview = "CinnamonBun"
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.08.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.wear.compose:compose-foundation:1.6.2")
    implementation("androidx.wear.compose:compose-material3:1.6.2")
    implementation("androidx.wear:wear-input:1.2.0")

    implementation("androidx.work:work-runtime-ktx:2.11.2")

    // Stable Tiles fallback for Wear OS 3+.
    implementation("androidx.wear.tiles:tiles:1.6.2")
    implementation("androidx.wear.protolayout:protolayout:1.4.2")
    implementation("androidx.wear.protolayout:protolayout-material3:1.4.2")
    implementation("androidx.wear.protolayout:protolayout-expression:1.4.2")
    debugImplementation("androidx.wear.tiles:tiles-renderer:1.6.2")

    // Watch-face complications.
    implementation("androidx.wear.watchface:watchface-complications-data-source-ktx:1.2.1")

    // Wear OS 7 Widgets. These APIs are intentionally alpha as of Aug 2026.
    implementation("androidx.compose.remote:remote-creation-compose:1.0.0-alpha17")
    implementation("androidx.compose.remote:remote-core:1.0.0-alpha17")
    implementation("androidx.glance.wear:wear:1.0.0-alpha16")
    implementation("androidx.glance.wear:wear-core:1.0.0-alpha16")
    implementation("androidx.wear.compose.remote:remote-material3:1.0.0-alpha09")

    testImplementation("junit:junit:4.13.2")
}
