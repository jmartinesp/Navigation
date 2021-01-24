plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-parcelize")
    id("com.vanniktech.maven.publish")
}

android {
    compileSdkVersion(29)
    buildToolsVersion("30.0.2")

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    sourceSets["main"].java {
        srcDir("src/main/kotlin")
    }
}

dependencies {

    implementation("org.jetbrains.kotlin:kotlin-stdlib:${Kotlin.version}")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.fragment:fragment-ktx:1.2.5")
    implementation("androidx.activity:activity-ktx:1.1.0")
    implementation("androidx.appcompat:appcompat:1.2.0")
    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}