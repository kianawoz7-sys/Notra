package id.catatcatat

object VoiceModels {
    const val SMALL_NAME = "ggml-small-q5_1.bin"
    const val BASE_NAME = "ggml-base-q5_1.bin"
    val defaultName: String get() = BuildConfig.VOICE_MODEL_FILE
    data class Model(val bytes: Long, val sha256: String)
    val supported = mapOf(
        SMALL_NAME to Model(190085487L, "ae85e4a935d7a567bd102fe55afc16bb595bdb618e11b2fc7591bc08120411bb"),
        // Each APK packages only its selected model; Base is a separate comparison build.
        BASE_NAME to Model(59707625L, "422f1ae452ade6f30a004d7e5c6a43195e4433bc370bf23fac9cc591f01a8898")
    )
}
