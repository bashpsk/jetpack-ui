import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {

    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("maven-publish")
}

android {

    namespace = "io.bashpsk.jetpackui"
    compileSdk = 36

    defaultConfig {

        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {

        compilerOptions.jvmTarget = JvmTarget.JVM_17
    }

    buildFeatures {

        compose = true
    }

    publishing {

        singleVariant("release") {

            withSourcesJar()
        }
    }
}

dependencies {

    //  DEFAULT         :
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //  KOTLINX             :
//    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.collections.immutable)
//    implementation(libs.kotlinx.datetime)

    //  ICON            :
    implementation(libs.androidx.material.icons.extended)

    //  NAVIGATION      :
    implementation(libs.androidx.navigation.compose)

    //  ADAPTIVE LAYOUT :
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)

    //  PSK LIBS        :
    implementation(libs.bashpsk.empty.format)
}

publishing {

    publications {

        register<MavenPublication>("release") {

            groupId = "io.bashpsk"
            artifactId = "jetpack-ui"
            version = "1.0.0"

            afterEvaluate {

                from(components["release"])
            }
        }
    }
}