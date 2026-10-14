package id.catatcatat

import org.junit.Assert.*
import org.junit.Test
import org.json.JSONObject
import java.util.UUID

class BackupTest {
    private fun row(name: String = "Kost") = Expense(UUID.randomUUID().toString(), name, 800000, "2026-10-14", 100, 101)
    @Test fun completeRoundTripIncludesDeletion() {
        val records = listOf(row("Kost \"Oktober\""), row("Print & kopi").copy(deleted = true))
        assertEquals(records.sortedBy { it.id }, BackupCodec.decode(BackupCodec.encode(records, 123)).records)
    }
    @Test fun signatureIgnoresOrderAndExportTime() { val a = row(); val b = row(); assertEquals(BackupCodec.signature(listOf(a,b)), BackupCodec.signature(listOf(b,a))) }
    @Test fun signatureDetectsEditsAndDeletes() { val a = row(); assertNotEquals(BackupCodec.signature(listOf(a)), BackupCodec.signature(listOf(a.copy(amount = 1)))); assertNotEquals(BackupCodec.signature(listOf(a)), BackupCodec.signature(listOf(a.copy(deleted = true)))) }
    @Test fun repeatedRestoreSkipsDuplicates() { val a = row(); val plan = BackupCodec.plan(listOf(a), BackupCodec.decode(BackupCodec.encode(listOf(a))).records); assertEquals(1, plan.unchanged); assertTrue(plan.additions.isEmpty()); assertTrue(plan.conflicts.isEmpty()) }
    @Test fun differentPurchasesWithSameNamePriceRemainSeparate() { val a = row(); val b = row(); assertEquals(listOf(b), BackupCodec.plan(listOf(a), listOf(b)).additions) }
    @Test fun deletedItemDoesNotSilentlyReturn() { val a = row(); val plan = BackupCodec.plan(listOf(a.copy(deleted = true)), listOf(a)); assertTrue(plan.additions.isEmpty()); assertEquals(1, plan.conflicts.size) }
    @Test fun changedItemRequiresConflictChoice() { val a = row(); assertEquals(1, BackupCodec.plan(listOf(a.copy(amount = 900000)), listOf(a)).conflicts.size) }
    @Test(expected = IllegalArgumentException::class) fun unsupportedVersion() { BackupCodec.decode(JSONObject(BackupCodec.encode(listOf(row()))).put("version", 2).toString()) }
    @Test(expected = IllegalArgumentException::class) fun wrongCurrency() { BackupCodec.decode(JSONObject(BackupCodec.encode(listOf(row()))).put("currency", "USD").toString()) }
    @Test(expected = IllegalArgumentException::class) fun duplicateIds() { val a = row(); BackupCodec.decode(BackupCodec.encode(listOf(a,a))) }
    @Test(expected = IllegalArgumentException::class) fun negativeMoney() { BackupCodec.decode(BackupCodec.encode(listOf(row().copy(amount = -1)))) }
    @Test(expected = IllegalArgumentException::class) fun decimalMoney() { val obj = JSONObject(BackupCodec.encode(listOf(row()))); obj.getJSONArray("expenses").getJSONObject(0).put("amount", 12.5); BackupCodec.decode(obj.toString()) }
    @Test(expected = IllegalArgumentException::class) fun invalidId() { BackupCodec.decode(BackupCodec.encode(listOf(row().copy(id = "not-a-uuid")))) }
    @Test(expected = Exception::class) fun impossibleDate() { BackupCodec.decode(BackupCodec.encode(listOf(row().copy(date = "2026-02-30")))) }
    @Test(expected = Exception::class) fun truncatedFile() { val text = BackupCodec.encode(listOf(row())); BackupCodec.decode(text.take(text.length / 2)) }
    @Test(expected = IllegalArgumentException::class) fun invalidStatus() { val obj = JSONObject(BackupCodec.encode(listOf(row()))); obj.getJSONArray("expenses").getJSONObject(0).put("deleted", "false"); BackupCodec.decode(obj.toString()) }
}
