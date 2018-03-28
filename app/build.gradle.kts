import com.android.build.gradle.ProguardFiles.getDefaultProguardFile
import org.gradle.internal.impldep.com.amazonaws.PredefinedClientConfigurations.defaultConfig
import org.gradle.kotlin.dsl.*

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
}

android {
    compileSdkVersion(27)
    buildToolsVersion("27.0.3")
    defaultConfig {
        applicationId = "ru.spbau.mit.structurednotes"
        minSdkVersion(21)
        targetSdkVersion(27)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        setSourceCompatibility(JavaVersion.VERSION_1_8)
        setTargetCompatibility(JavaVersion.VERSION_1_8)
    }
    sourceSets {
        for (piece in listOf("main", "test", "androidTest")) {
            getByName(piece).java.srcDirs("src/$piece/kotlin")
        }
    }
}

dependencies {
    implementation(fileTree("lib") { include("*.jar") })
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jre8:1.2.20")
    implementation("com.android.support:appcompat-v7:27.1.0")
    implementation("com.android.support.constraint:constraint-layout:1.0.2")
    implementation("com.android.support:design:27.1.0")
    testImplementation("junit:junit:4.12")
    androidTestImplementation("com.android.support.test:runner:1.0.1")
    androidTestImplementation("com.android.support.test.espresso:espresso-core:3.0.1")
}
