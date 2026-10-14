package id.catatcatat

import kotlin.math.ceil

object VoiceTranscript {
    fun validate(text: String, audioSeconds: Float) {
        val words = Regex("[\\p{L}\\p{N}]+").findAll(text.lowercase()).map { it.value }.toList()
        val maxWords = maxOf(24, ceil(audioSeconds * 6).toInt() + 8)
        require(words.size <= maxWords) { "Hasil suara terlalu panjang untuk rekaman ini. Coba rekam ulang; belum ada pengeluaran yang disimpan." }
        // Refuse a loop; do not silently merge purchases which may be distinct.
        // Two to four identical purchases can still be reviewed normally.
        for (size in 3..minOf(16, words.size / 5)) {
            for (start in 0..words.size - size * 5) {
                val phrase = words.subList(start, start + size)
                if ((1..4).all { repeat -> words.subList(start + size * repeat, start + size * (repeat + 1)) == phrase }) {
                    throw IllegalArgumentException("Hasil suara mengulang kalimat yang sama. Coba rekam ulang atau isi manual; belum ada pengeluaran yang disimpan.")
                }
            }
        }
    }
}
