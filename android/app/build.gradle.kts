plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// No GitHub Actions o número do build vira a versão; localmente fica 1.
val buildNumber = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toInt()

android {
    namespace = "com.yourlife.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yourlife.app"
        minSdk = 28 // mínimo exigido pelo Health Connect (Android 9)
        targetSdk = 35
        versionCode = buildNumber
        versionName = "1.0.$buildNumber"
    }

    // Chave fixa: todo build sai com a mesma assinatura, então o APK novo atualiza o antigo
    // sem precisar desinstalar (e as permissões do Health Connect continuam valendo).
    signingConfigs {
        create("release") {
            storeFile = file("../keystore/yourlife.jks")
            storePassword = "yourlife123"
            keyAlias = "yourlife"
            keyPassword = "yourlife123"
        }
    }

    buildTypes {
        release {
            // reduz o tamanho do APK removendo código e recursos não usados (sem renomear classes)
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
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
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.material3:material3")

    // Health Connect (Saúde do Google)
    implementation("androidx.health.connect:connect-client:1.1.0-alpha07")
}
