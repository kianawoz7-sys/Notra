package id.catatcatat

/** Experimental attention windows must be compared against QUALITY on identical audio. */
enum class VoiceProfile(val key: String, val minimumContext: Int, val beamSize: Int) {
    QUALITY("quality", 0, 3),
    CONTEXT_512("context512", 512, 3),
    CONTEXT_384("context384", 384, 3),
    FAST_384("fast384", 384, 1);

    val preferFastCores: Boolean get() = this != QUALITY

    fun audioContext(sampleCount: Int): Int {
        require(sampleCount in 4800..960000) { "Panjang rekaman tidak valid." }
        if (minimumContext == 0) return 0
        // 50 encoder frames/second, plus two seconds after the complete recording.
        // Never crop to a fixed short window when the utterance itself is longer.
        val required = ((sampleCount.toLong() * 50 + 15999) / 16000).toInt() + 100
        val rounded = ((required + 127) / 128) * 128
        val context = maxOf(minimumContext, rounded)
        return if (context >= 1500) 0 else context
    }

    companion object {
        val default: VoiceProfile get() = fromKey(BuildConfig.VOICE_PROFILE)
        fun fromKey(key: String): VoiceProfile = entries.firstOrNull { it.key == key }
            ?: error("Profil suara tidak dikenal.")
    }
}
