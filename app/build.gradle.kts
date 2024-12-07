plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.gms.google.services)
    alias(libs.plugins.ksp)
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.firebase.crashlytics")
}

android {
    android.buildFeatures.buildConfig = true

    signingConfigs {
        create("release") {
            storeFile = file("C:\\AndroidStudioProjects\\enp-android\\Tool4All")
            storePassword = "Phene90x"
            keyAlias = "Tool4All"
            keyPassword = "Phene90x"
        }
    }

    bundle {  //affect googles generation of apk files from aab not to exclude alternative language strings
        language {
            enableSplit = false
        }
    }

    namespace = "com.mobility.enp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mobility.enp"
        minSdk = 29
        targetSdk = 34
        versionCode = 109
        versionName = "v-109"

        resourceConfigurations += listOf("en", "sr", "de", "mk", "tr", "b+cnr", "hr", "el", "bs")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        multiDexEnabled = true
        signingConfig = signingConfigs.getByName("release")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    flavorDimensions += "mode"
    productFlavors {
        create("dev") {
            // Dev flavor configuration
            dimension = "mode"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            buildConfigField("String", "API_URL", "\"https://mobileapidev.toll4all.com/\"")
            buildConfigField("String", "TEST_USERNAME", "\"toll4alldev+53972@gmail.com\"")
            buildConfigField("String", "TEST_PASSWORD", "\"demodemo\"")
        }
        create("stage") {
            dimension = "mode"
            applicationIdSuffix = ".stage"
            proguardFiles(getDefaultProguardFile("proguard-android.txt"))
            versionNameSuffix = "-stage"
            multiDexEnabled = true
            buildConfigField("String", "API_URL", "\"https://mobileapitest.toll4all.com/\"")
            buildConfigField("String", "TEST_USERNAME", "\"toll4alldev+51216@gmail.com\"")
            buildConfigField("String", "TEST_PASSWORD", "\"demodemo\"")
        }
        create("prod") {
            // Prod flavor configuration
            dimension = "mode"
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("String", "API_URL", "\"https://mobileapi.toll4all.com/\"")
            buildConfigField("String", "TEST_USERNAME", "\"daniel.ristic993@gmail.com\"")
            buildConfigField("String", "TEST_PASSWORD", "\"Dr1stic1++!\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

}

dependencies {

    //https://developer.android.com/build/migrate-to-catalogs#kts

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.databinding.runtime)
    implementation(libs.androidx.retrofit)
    implementation(libs.androidx.converter.gson)
    implementation(libs.androidx.okhttp)
    implementation(libs.androidx.logging.interceptor)
    implementation(libs.androidx.ccp)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.sqlite.ktx)
    implementation(libs.androidx.glide)
    implementation(libs.androidx.dexter)
    implementation(libs.androidx.gson)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.webkit)
    // Firebase Libraries
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)
    releaseImplementation(libs.google.firebase.crashlytics)
    implementation(libs.androidx.makeramen.roundedimageview)

    implementation(libs.jakewharton.retrofit2.kotlin.coroutines.adapter)

    androidTestImplementation(libs.androidx.espresso.contrib)

    testImplementation(libs.junit.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.androidx.fragment.testing)
    debugImplementation(libs.androidx.fragment.testing.manifest)

    //pdf
    implementation(libs.androidpdfviewer)
    implementation(libs.itext7.core)
    ksp(libs.androidx.room.compiler)
    ksp(libs.compiler)

    //Moshi biblioteka
    implementation(libs.moshi)  // Glavna Moshi biblioteka
    implementation(libs.moshi.kotlin)  // Podrška za Kotlin
}
