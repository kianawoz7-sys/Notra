package id.catatcatat

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.security.MessageDigest
import java.time.LocalDate
import java.util.UUID

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC, createdAt DESC") fun observe(): Flow<List<Expense>>
    @Query("SELECT * FROM expenses ORDER BY id") suspend fun snapshot(): List<Expense>
    @Upsert suspend fun upsert(records: List<Expense>)
}
@Database(entities = [Expense::class], version = 1, exportSchema = true)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenses(): ExpenseDao
    companion object {
        fun create(context: Context) = Room.databaseBuilder(context, ExpenseDatabase::class.java, "catat-catat.db").build()
    }
}

data class Backup(val exportedAt: Long, val records: List<Expense>)
data class MergePlan(val additions: List<Expense>, val conflicts: List<Pair<Expense, Expense>>, val unchanged: Int)
object BackupCodec {
    const val MAX_BYTES = 25 * 1024 * 1024
    const val MAX_RECORDS = 50_000
    fun encode(records: List<Expense>, exportedAt: Long = System.currentTimeMillis()): String {
        val items = JSONArray()
        records.sortedBy { it.id }.forEach { items.put(JSONObject().put("id", it.id).put("name", it.name).put("amount", it.amount).put("date", it.date).put("createdAt", it.createdAt).put("updatedAt", it.updatedAt).put("deleted", it.deleted)) }
        return JSONObject().put("format", "catat-catat").put("version", 1).put("currency", "IDR").put("exportedAt", exportedAt).put("expenses", items).toString(2)
    }
    fun signature(records: List<Expense>): String = MessageDigest.getInstance("SHA-256").digest(encode(records, 0).toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    private fun integer(obj: JSONObject, key: String): Long {
        val value = obj.get(key)
        require(value is Number) { "Nilai $key pada backup tidak valid." }
        return runCatching { BigDecimal(value.toString()).longValueExact() }.getOrElse { throw IllegalArgumentException("Nilai $key pada backup tidak valid.") }
    }
    fun decode(text: String): Backup {
        require(text.toByteArray(Charsets.UTF_8).size <= MAX_BYTES) { "File backup terlalu besar." }
        val root = JSONObject(text)
        require(root.getString("format") == "catat-catat") { "Ini bukan cadangan Notra yang didukung." }
        require(integer(root, "version") == 1L) { "Versi backup belum didukung." }
        require(root.getString("currency") == "IDR") { "Mata uang backup belum didukung." }
        val exported = integer(root, "exportedAt")
        require(exported >= 0) { "Waktu backup tidak valid." }
        val array = root.getJSONArray("expenses")
        require(array.length() <= MAX_RECORDS) { "Catatan dalam backup terlalu banyak." }
        val seen = mutableSetOf<String>()
        val rows = (0 until array.length()).map { index ->
            val obj = array.getJSONObject(index)
            val id = obj.getString("id")
            require(UUID.fromString(id).toString() == id && seen.add(id)) { "ID catatan tidak valid atau ganda." }
            val name = obj.getString("name").trim()
            require(name.isNotEmpty() && name.length <= 200 && name.none(Char::isISOControl)) { "Nama catatan tidak valid." }
            val amount = integer(obj, "amount")
            require(amount in 1..MAX_AMOUNT) { "Nominal dalam backup tidak valid." }
            val date = obj.getString("date")
            require(LocalDate.parse(date).toString() == date) { "Tanggal catatan tidak valid." }
            val createdAt = integer(obj, "createdAt")
            val updatedAt = integer(obj, "updatedAt")
            require(createdAt >= 0 && updatedAt >= createdAt) { "Waktu catatan tidak valid." }
            require(obj.get("deleted") is Boolean) { "Status catatan tidak valid." }
            Expense(id, name, amount, date, createdAt, updatedAt, obj.getBoolean("deleted"))
        }
        return Backup(exported, rows)
    }
    fun plan(current: List<Expense>, incoming: List<Expense>): MergePlan {
        val existing = current.associateBy { it.id }
        val additions = mutableListOf<Expense>()
        val conflicts = mutableListOf<Pair<Expense, Expense>>()
        var unchanged = 0
        incoming.forEach { row ->
            val old = existing[row.id]
            when { old == null -> additions += row; old == row -> unchanged++; else -> conflicts += old to row }
        }
        return MergePlan(additions, conflicts, unchanged)
    }
}
