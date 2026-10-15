import java.util.Properties
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}
val localConfig = Properties().apply { rootProject.file("local.properties").inputStream().use(::load) }
val voiceModel = providers.gradleProperty("voiceModel").getOrElse("small")
require(voiceModel in setOf("small", "base")) { "voiceModel must be small or base" }
val voiceModelFile = "ggml-$voiceModel-q5_1.bin"
val voiceProfile = providers.gradleProperty("voiceProfile").getOrElse("quality")
val voiceExperiment = providers.gradleProperty("voiceExperiment").getOrElse("false").toBooleanStrict()
require(voiceProfile in setOf("quality", "context512", "context384", "fast384")) { "Unknown voiceProfile" }
require(voiceExperiment || voiceProfile == "quality") { "Speed profiles require -PvoiceExperiment=true; keep the main app on QUALITY" }
val voiceAssets by tasks.registering(Sync::class) {
    from(localConfig.getProperty("model.dir")) { include(voiceModelFile, "*LICENSE.txt") }
    into("D:/CatatCatat-tools/packaged-model")
    doFirst {
        val model = file("${localConfig.getProperty("model.dir")}/$voiceModelFile")
        val expectedBytes = if (voiceModel == "small") 190085487L else 59707625L
        check(model.isFile && model.length() == expectedBytes) { "Model $voiceModelFile belum lengkap. Jalankan tools/fetch_voice_models.py; model disimpan di D." }
    }
}
android {
    namespace = "id.catatcatat"
    compileSdk = 35
    ndkVersion = "28.2.13676358"
    defaultConfig {
        applicationId = if (voiceExperiment) "id.catatcatat.voicetest" else "id.catatcatat"
        minSdk = 26
        targetSdk = 35
        versionCode = 7
        versionName = if (voiceExperiment) "1.1.1-uji-$voiceProfile" else if (voiceModel == "small") "1.1.1" else "1.1.1-base"
        buildConfigField("String", "VOICE_MODEL_FILE", "\"$voiceModelFile\"")
        buildConfigField("String", "VOICE_PROFILE", "\"$voiceProfile\"")
        buildConfigField("boolean", "VOICE_EXPERIMENT", voiceExperiment.toString())
        resValue("string", "launcher_name", if (voiceExperiment) "Notra Uji" else "Notra")
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
