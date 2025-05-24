plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.two.channelmyanmar"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.two.channelmyanmar"
        minSdk = 24
        targetSdk = 35
        versionCode = 3
        versionName = "1.5"
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
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.glide)
    implementation(libs.jsoup)
    implementation(libs.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.media3.ui)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(project(":my-libs"))

}