plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id ("com.google.gms.google-services")
    id ("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.example.testwhatsapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.testwhatsapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //FireBase
    implementation (platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation ("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-storage:21.0.1")
    implementation ("com.google.firebase:firebase-database")
    implementation ("com.google.firebase:firebase-auth-ktx:23.1.0")
    implementation ("com.google.firebase:firebase-database:21.0.0")

    //ViewModel
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")

    //Swipe Refresh Layout
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Dependency Injection Koin
    implementation ("io.insert-koin:koin-android:3.4.2")

    //Navigation Component
    implementation ("androidx.navigation:navigation-fragment-ktx:2.8.5")
    implementation ("androidx.navigation:navigation-ui-ktx:2.8.5")
    implementation ("androidx.navigation:navigation-fragment:2.8.5")
    implementation ("androidx.navigation:navigation-ui:2.8.5")

    // Material Design
    implementation ("androidx.appcompat:appcompat:1.7.0")
    implementation ("com.google.android.material:material:1.12.0")

    //Glide
    implementation ("com.github.bumptech.glide:glide:4.13.2")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")

    //WebRTC
    implementation ("io.getstream:stream-webrtc-android:1.3.7")

    // Agora
    implementation ("io.agora.rtc:full-sdk:3.5.0")
}
apply(plugin = "com.google.gms.google-services")