plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.gms.google.services)
    alias(libs.plugins.ksp)
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.firebase.crashlytics")
    id("kotlin-parcelize")
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
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mobility.enp"
        minSdk = 29
        targetSdk = 35
        versionCode = 250
        versionName = "1.9.1"

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
        create("stage") {
            dimension = "mode"
            applicationIdSuffix = ".stage"
            proguardFiles(getDefaultProguardFile("proguard-android.txt"))
            versionNameSuffix = "-stage"
            multiDexEnabled = true
            isDefault = true
            buildConfigField("String", "API_URL", "\"https://mobileapitest.toll4all.com/\"")
            buildConfigField("String", "TEST_USERNAME", "\"toll4alldev+8025@gmail.com\"")
            buildConfigField("String", "TEST_PASSWORD", "\"Demo!4team\"")
            buildConfigField(
                "String",
                "TAG_ORDER_BASE_URL",
                "\"https://test.toll4all.com/tag-narucivanje\""
            )
        }
        create("prod") {
            // Prod flavor configuration
            dimension = "mode"
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("String", "API_URL", "\"https://mobileapi.toll4all.com/\"")
            buildConfigField("String", "TEST_USERNAME", "\"cok.brb.11@gmail.com\"")
            buildConfigField("String", "TEST_PASSWORD", "\"testiranje1!\"")
            buildConfigField(
                "String",
                "TAG_ORDER_BASE_URL",
                "\"https://toll4all.com/tag-narucivanje\""
            )
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

    implementation(libs.android.pdf.viewer)
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
    implementation(libs.androidx.gson)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.webkit)
    // Firebase Libraries
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)
    implementation(libs.google.firebase.crashlytics)
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
    implementation(libs.itext7.core)
    ksp(libs.androidx.room.compiler)
    ksp(libs.compiler)

    //Moshi biblioteka
    implementation(libs.moshi)              // Glavna Moshi biblioteka za JSON parsiranje
    implementation(libs.moshi.kotlin)       // Kotlin ekstenzije i podrška za Kotlin specifične tipove
    ksp(libs.moshi.kotlin.codegen)          // KSP codegen za automatsko generisanje adaptera bez refleksije

}
