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
        targetSdk = 36
        versionCode = 2
        versionName = "0.1.1"
    }

    flavorDimensions += "wearApi"
    productFlavors {
        create("compat") {
            dimension = "wearApi"
            targetSdk = 36
            versionNameSuffix = "-compat"
        }
        create("wear7") {
            dimension = "wearApi"
            targetSdkPreview = "CinnamonBun"
            versionNameSuffix = "-wear7"
        }
    }

    buildTypes {
        // Keep debug normal so unit tests and local development stay predictable.
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }

        // Direct-install artifact: release-like optimization with the standard
        // Android debug key, so the APK can be sideloaded without release secrets.
        create("sideload") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            matchingFallbacks += listOf("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
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

    implementation("androidx.wear.compose:compose-foundation:1.6.2")
    implementation("androidx.wear.compose:compose-material3:1.6.2")
    implementation("androidx.wear:wear-input:1.2.0")

    implementation("androidx.work:work-runtime-ktx:2.11.2")

    // TileService returns ListenableFuture and uses Futures.immediateFuture.
    // Declare Guava explicitly instead of inheriting it from debug-only tooling.
    implementation("com.google.guava:guava:33.4.8-android")

    // Wear OS 3+ / Watch5 Pro surfaces.
    implementation("androidx.wear.tiles:tiles:1.6.2")
    implementation("androidx.wear.protolayout:protolayout:1.4.2")
    implementation("androidx.wear.protolayout:protolayout-material3:1.4.2")
    implementation("androidx.wear.protolayout:protolayout-expression:1.4.2")
    implementation("androidx.wear.watchface:watchface-complications-data-source-ktx:1.2.1")

    // Wear OS 7 Widgets stay out of the Watch5 Pro / Wear OS 6 APK.
    add("wear7Implementation", "androidx.compose.remote:remote-creation-compose:1.0.0-alpha17")
    add("wear7Implementation", "androidx.compose.remote:remote-core:1.0.0-alpha17")
    add("wear7Implementation", "androidx.glance.wear:wear:1.0.0-alpha16")
    add("wear7Implementation", "androidx.glance.wear:wear-core:1.0.0-alpha16")
    add("wear7Implementation", "androidx.wear.compose.remote:remote-material3:1.0.0-alpha09")

    testImplementation("junit:junit:4.13.2")
}
