import java.io.FileInputStream
import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-kapt")
}
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) { // <-- This check is missing
    localProperties.load(FileInputStream(localPropertiesFile))
}
android {
    namespace = "com.nikhil.sellerapp"
    compileSdk = 35

    defaultConfig {

        buildConfigField("String", "SUPABASE_URL", "\"${localProperties.getProperty("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_KEY", "\"${localProperties.getProperty("SUPABASE_KEY")}\"")
        buildConfigField("String", "BRANDFETCH_API_KEY", "\"${localProperties.getProperty("BRANDFETCH_API_KEY")}\"")
        manifestPlaceholders["GOOGLE_PLACES_API_KEY"] = localProperties.getProperty("GOOGLE_PLACES_API_KEY") ?: ""
        applicationId = "com.nikhil.sellerapp"
        minSdk = 24
        targetSdk = 35
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
    packagingOptions {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/ASL2.0"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures{
        viewBinding=true
        buildConfig=true
    }
}

dependencies {
    // Google Places SDK for Android
    implementation("com.google.android.libraries.places:places:3.5.0")

    // Retrofit for networking (to call Brandfetch API)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Coroutines for asynchronous work
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // ViewModel KTX for viewModelScope
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")

    // REMOVED: Coil dependency
    // implementation("io.coil-kt:coil:2.6.0")

    // ADDED: Glide dependencies
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.androidx.recyclerview)
    implementation(libs.firebase.crashlytics.buildtools)
    kapt("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.getkeepsafe.taptargetview:taptargetview:1.13.3")
    implementation("com.airbnb.android:lottie:6.0.0")
    
    implementation("io.ktor:ktor-client-okhttp:3.1.3")
    //supabase storage
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.2"))
    implementation ("io.github.jan-tennert.supabase:storage-kt:3.0.2")
    //glide

    implementation(libs.firebase.firestore)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)


    implementation("com.hbb20:ccp:2.6.1")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.caverock:androidsvg:1.4")
    implementation("com.google.android.material:material:1.11.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}