package id.catatcatat

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

const val MAX_AMOUNT = 1_000_000_000_000L

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey val id: String,
    val name: String,
    val amount: Long,
    val date: String,
    val createdAt: Long,
    val updatedAt: Long,
    val deleted: Boolean = false
)

data class Draft(
    val key: String = UUID.randomUUID().toString(),
    val name: String = "",
    val amountText: String = "",
    val warning: String? = null
) {
    val amount: Long? get() = amountText.toLongOrNull()?.takeIf { it in 1..MAX_AMOUNT }
    val valid: Boolean get() = name.trim().isNotEmpty() && name.trim().length <= 200 && name.none(Char::isISOControl) && amount != null
}

fun rupiah(amount: Long): String = "Rp" + NumberFormat.getIntegerInstance(Locale("id", "ID")).format(amount)

enum class Period { DAY, WEEK, MONTH }
data class DateRange(val start: LocalDate, val end: LocalDate) {
    fun contains(date: String): Boolean = date >= start.toString() && date <= end.toString()
}
fun periodRange(anchor: LocalDate, period: Period): DateRange = when (period) {
    Period.DAY -> DateRange(anchor, anchor)
    Period.WEEK -> anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).let { DateRange(it, it.plusDays(6)) }
    Period.MONTH -> DateRange(anchor.withDayOfMonth(1), anchor.withDayOfMonth(anchor.lengthOfMonth()))
}
fun visibleExpenses(records: List<Expense>, range: DateRange) = records.filter { !it.deleted && range.contains(it.date) }.sortedWith(compareByDescending<Expense> { it.date }.thenByDescending { it.createdAt })

object ExpenseParser {
    private val digits = Regex("-?\\d+(?:[.,]\\d+)*")
    private val lex = Regex("-?\\d+(?:[.,]\\d+)*|[\\p{L}]+")
    private val numbers = mapOf("nol" to 0, "satu" to 1, "dua" to 2, "tiga" to 3, "empat" to 4, "lima" to 5, "enam" to 6, "tujuh" to 7, "delapan" to 8, "sembilan" to 9, "sepuluh" to 10, "sebelas" to 11, "seratus" to 100, "seribu" to 1000, "sejuta" to 1_000_000, "setengah" to 0)
    private val scales = mapOf("ribu" to 1000L, "rebu" to 1000L, "rb" to 1000L, "k" to 1000L, "juta" to 1_000_000L, "jt" to 1_000_000L)
    private val numericWords = numbers.keys + scales.keys + setOf("belas", "puluh", "ratus", "rupiah")
    private fun isNumber(s: String) = digits.matches(s) || s in numbers || s in setOf("belas", "puluh", "ratus")
    private fun cleanName(s: String): String = s.trim(' ', ',', '.', ';', ':', '!', '?', '\n', '"').replace(Regex("^(?:(?:dan|lalu|terus|kemudian)\\s+)+", RegexOption.IGNORE_CASE), "").replace(Regex("\\brp\\.?$", RegexOption.IGNORE_CASE), "").trim().replaceFirstChar { it.titlecase(Locale("id")) }

    fun parse(text: String): List<Draft> {
        val tokens = lex.findAll(text.lowercase(Locale("id"))).toList()
        val output = mutableListOf<Draft>()
        var nameStart = 0
        var i = 0
        while (i < tokens.size) {
            if (!isNumber(tokens[i].value)) { i++; continue }
            val first = i
            val currencyBefore = i > 0 && tokens[i - 1].value == "rp"
            while (i < tokens.size && (tokens[i].value in numericWords || digits.matches(tokens[i].value))) i++
            val phrase = tokens.subList(first, i).map { it.value }
            val end = tokens[i - 1].range.last + 1
            val name = cleanName(text.substring(nameStart, tokens[first].range.first))
            val result = runCatching { readAmount(phrase, currencyBefore) }
            output += Draft(name = name, amountText = result.getOrNull()?.toString() ?: "", warning = result.exceptionOrNull()?.message ?: if (name.isBlank()) "Lengkapi nama pengeluaran." else null)
            nameStart = end
        }
        val trailing = cleanName(text.substring(nameStart))
        if (trailing.isNotBlank()) output += Draft(name = trailing, warning = "Nominal belum terbaca. Isi sebelum menyimpan.")
        return output.ifEmpty { listOf(Draft(warning = "Suara belum terbaca. Coba lagi atau isi manual.")) }
    }

    private fun readAmount(words: List<String>, currency: Boolean): Long {
        val explicit = currency || words.any { it in scales || it in setOf("rupiah", "seribu", "sejuta") }
        var total = BigDecimal.ZERO
        val chunk = mutableListOf<String>()
        for (w in words) {
            when {
                w == "rupiah" -> Unit
                w in scales -> {
                    require(chunk.isNotEmpty()) { "Nominal belum lengkap." }
                    total += smallNumber(chunk) * BigDecimal.valueOf(scales.getValue(w))
                    chunk.clear()
                }
                w == "seribu" || w == "sejuta" -> {
                    require(chunk.isEmpty()) { "Periksa nominal yang terbaca." }
                    total += BigDecimal.valueOf(numbers.getValue(w).toLong())
                }
                else -> chunk += w
            }
        }
        if (chunk.isNotEmpty()) total += smallNumber(chunk)
        val amount = runCatching { total.longValueExact() }.getOrElse { throw IllegalArgumentException("Nominal harus berupa rupiah utuh.") }
        require(amount in 1..MAX_AMOUNT) { "Isi nominal lebih dari nol dan paling banyak satu triliun." }
        require(explicit || amount >= 1000) { "Satuan nominal belum jelas. Isi rupiah lengkap, misalnya 12000." }
        return amount
    }

    private fun smallNumber(words: List<String>): BigDecimal {
        var sum = BigDecimal.ZERO
        var last = BigDecimal.ZERO
        var pending = false
        for (word in words) {
            when (word) {
                "seratus" -> { require(!pending) { "Periksa nominal yang terbaca." }; sum += BigDecimal.valueOf(100) }
                "belas" -> { require(pending && last in BigDecimal.ONE..BigDecimal.valueOf(9)) { "Periksa nominal yang terbaca." }; last += BigDecimal.TEN }
                "puluh", "ratus" -> { require(pending) { "Nominal belum lengkap." }; sum += last * BigDecimal.valueOf(if (word == "puluh") 10 else 100); last = BigDecimal.ZERO; pending = false }
                "setengah" -> { sum += last + BigDecimal("0.5"); last = BigDecimal.ZERO; pending = false }
                else -> {
                    require(!pending) { "Ada angka yang bertumpuk. Periksa nominal." }
                    last = if (digits.matches(word)) {
                        val normalized = if (Regex("-?\\d{1,3}(?:[.,]\\d{3})+").matches(word)) word.replace(".", "").replace(",", "") else word.replace(',', '.')
                        BigDecimal(normalized)
                    } else BigDecimal.valueOf(numbers[word]?.toLong() ?: error("Nominal belum terbaca."))
                    require(last >= BigDecimal.ZERO) { "Nominal harus lebih dari nol." }
                    pending = true
                }
            }
        }
        return sum + last
    }
}
