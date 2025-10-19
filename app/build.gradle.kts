import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
}
val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) FileInputStream(file).use { load(it) }
}

val CLOUDINARY_CLOUD_NAME: String =
    (localProps.getProperty("CLOUDINARY_CLOUD_NAME")
        ?: System.getenv("CLOUDINARY_CLOUD_NAME")).orEmpty()

val CLOUDINARY_UPLOAD_PRESET: String =
    (localProps.getProperty("CLOUDINARY_UPLOAD_PRESET")
        ?: System.getenv("CLOUDINARY_UPLOAD_PRESET")).orEmpty()

android {
    namespace = "com.proptit.todohive"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.proptit.todohive"
        minSdk = 32
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "CLOUD_NAME", "\"$CLOUDINARY_CLOUD_NAME\"")
        buildConfigField("String", "UPLOAD_PRESET", "\"$CLOUDINARY_UPLOAD_PRESET\"")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation("androidx.core:core-splashscreen:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.4")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.play.services.cast.tv)
    implementation(libs.google.material)
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // Room
    val room_version = "2.7.2"
    implementation("androidx.room:room-runtime:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    //Cloudinary
    implementation("com.cloudinary:cloudinary-android:3.0.2")

    // Desugar java.time
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}