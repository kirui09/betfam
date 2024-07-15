plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("kotlin-kapt")



}

android {
    namespace = "com.betfam.apptea"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.betfam.apptea"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "3.0"
        multiDexEnabled= true

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
        freeCompilerArgs = listOf("-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
    }

    buildFeatures {
        buildFeatures {
            viewBinding= true
            dataBinding= true
        }
    }

    packaging {
        resources.excludes.add("META-INF/*")
    }

}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation ("androidx.recyclerview:recyclerview:1.2.0")
    implementation ("androidx.recyclerview:recyclerview-selection:1.1.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")
    implementation ("com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation ("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
    implementation("androidx.room:room-common:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("com.android.volley:volley:1.2.1")
    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("androidx.lifecycle:lifecycle-viewmodel:2.3.1")
    implementation ("androidx.lifecycle:lifecycle-livedata:2.3.1")
    implementation ("androidx.lifecycle:lifecycle-common-java8:2.3.1")
    implementation ("com.google.dagger:dagger:2.38.1")
    kapt ("com.google.dagger:dagger-compiler:2.38.1")
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation ("androidx.navigation:navigation-fragment-ktx")

    implementation ("androidx.multidex:multidex:2.0.1")

// Room components
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    // Lifecycle components (compatible with Room 2.7.4)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.6.1")
    kapt("androidx.lifecycle:lifecycle-compiler:2.6.1")


    implementation("com.google.api-client:google-api-client:1.25.0")
    implementation("com.google.api-client:google-api-client-android:1.25.0")
    implementation("com.google.apis:google-api-services-sheets:v4-rev614-1.18.0-rc")
    implementation("com.google.http-client:google-http-client:1.44.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.oauth-client:google-oauth-client:1.35.0")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    // https://mvnrepository.com/artifact/com.google.apis/google-api-services-drive
    // https://mvnrepository.com/artifact/com.google.apis/google-api-services-drive
implementation("com.google.apis:google-api-services-drive:v3-rev197-1.25.0")
    implementation ("com.google.apis:google-api-services-sheets:v4.11.0")

    // https://mvnrepository.com/artifact/com.google.guava/guava
    implementation("com.google.guava:guava:33.1.0-jre")


    // https://mvnrepository.com/artifact/com.jakewharton.timber/timber
    implementation("com.jakewharton.timber:timber:5.0.1")

    // https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.12")

    implementation ("androidx.constraintlayout:constraintlayout:2.1.3")


}

