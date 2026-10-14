import java.util.Properties
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}
val localConfig = Properties().apply { rootProject.file("local.properties").inputStream().use(::load) }
val voiceAssets by tasks.registering(Sync::class) {
    from(localConfig.getProperty("model.dir")) { include("ggml-base-q5_1.bin", "*LICENSE.txt") }
    into("D:/CatatCatat-tools/packaged-model")
}
android {
    namespace = "id.catatcatat"
    compileSdk = 35
    ndkVersion = "28.2.13676358"
    defaultConfig {
        applicationId = "id.catatcatat"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "1.0.3"
        ndk { abiFilters += listOf("arm64-v8a", "x86_64") }
        externalNativeBuild { cmake {
            arguments += listOf("-DWHISPER_SOURCE_DIR=${localConfig.getProperty("whisper.dir")}", "-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON")
            cppFlags += listOf("-std=c++17", "-O3")
        } }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    externalNativeBuild { cmake {
        path = file("src/main/cpp/CMakeLists.txt")
        version = "3.22.1"
        buildStagingDirectory = file("D:/CatatCatat-tools/native-build")
    } }
    sourceSets.getByName("main").assets.setSrcDirs(listOf(voiceAssets))
    sourceSets.getByName("androidTest").assets.srcDir("D:/CatatCatat-tools/test-audio")
    androidResources { noCompress += "bin" }
    packaging { resources.excludes += listOf("/META-INF/{AL2.0,LGPL2.1}") }
}
tasks.named("preBuild") { dependsOn(voiceAssets) }
ksp { arg("room.schemaLocation", "$projectDir/schemas") }
dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")
    ksp("androidx.room:room-compiler:2.7.2")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}
