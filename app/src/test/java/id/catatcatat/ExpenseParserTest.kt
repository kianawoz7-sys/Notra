package id.catatcatat

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ExpenseParserTest {
    @Test fun colloquialTwelveThousand() { assertEquals(12000L, ExpenseParser.parse("macha dua belas rebu").single().amount) }
    @Test fun twoMixedItems() {
        val rows = ExpenseParser.parse("Nasi ayam lima belas rebu, es teh lima rebu.")
        assertEquals(listOf("Nasi ayam", "Es teh"), rows.map { it.name })
        assertEquals(listOf(15000L, 5000L), rows.map { it.amount })
    }
    @Test fun fourDifferentExpenseTypes() {
        val rows = ExpenseParser.parse("Bayar kost delapan ratus rebu, paket data lima puluh rebu, ngeprint lima rebu, bensin tiga puluh rebu")
        assertEquals(listOf(800000L, 50000L, 5000L, 30000L), rows.map { it.amount })
        assertEquals(885000L, rows.sumOf { it.amount!! })
        assertTrue(rows.all { it.valid })
    }
    @Test fun compactNumericUnits() { assertEquals(listOf(12000L, 5000L, 30000L), ExpenseParser.parse("macha 12rb, es teh 5 rebu, bensin 30k").map { it.amount }) }
    @Test fun thousandSeparators() { assertEquals(1250000L, ExpenseParser.parse("kost Rp1.250.000").single().amount) }
    @Test fun decimalsBeforeUnits() { assertEquals(12500L, ExpenseParser.parse("macha 12,5 ribu").single().amount) }
    @Test fun millionAndThousands() { assertEquals(1250000L, ExpenseParser.parse("kost satu juta dua ratus lima puluh ribu").single().amount) }
    @Test fun hundredAndTens() { assertEquals(125000L, ExpenseParser.parse("paket data seratus dua puluh lima ribu").single().amount) }
    @Test fun amountBelowOneThousandIsExplicit() { assertEquals(500L, ExpenseParser.parse("print lima ratus rupiah").single().amount) }
    @Test fun mixedThousandsAndHundreds() { assertEquals(5500L, ExpenseParser.parse("es teh lima ribu lima ratus").single().amount) }
    @Test fun halfThousands() { assertEquals(12500L, ExpenseParser.parse("kopi dua belas setengah ribu").single().amount) }
    @Test fun bareSmallNumberNeedsReview() { val draft = ExpenseParser.parse("kopi 12").single(); assertNull(draft.amount); assertFalse(draft.valid); assertNotNull(draft.warning) }
    @Test fun bareFullRupiahWorks() { assertEquals(15000L, ExpenseParser.parse("kopi 15000").single().amount) }
    @Test fun missingFinalPriceStaysVisible() { val rows = ExpenseParser.parse("kopi 15 ribu, ngeprint"); assertEquals(2, rows.size); assertEquals("Ngeprint", rows.last().name); assertFalse(rows.last().valid) }
    @Test fun missingAllPricesNeedsManual() { assertFalse(ExpenseParser.parse("paket data").single().valid) }
    @Test fun negativePriceRejected() { assertFalse(ExpenseParser.parse("bensin -5000 rupiah").single().valid) }
    @Test fun zeroRejected() { assertFalse(ExpenseParser.parse("bensin nol rupiah").single().valid) }
    @Test fun stackedNumbersAreAmbiguous() { assertFalse(ExpenseParser.parse("kopi 2 10000").single().valid) }
    @Test fun hugeAmountRejected() { assertFalse(ExpenseParser.parse("kost 99999999999999999999999 rupiah").single().valid) }
    @Test fun moreThanFourItemsAllowed() { assertEquals(5, ExpenseParser.parse("a 1 ribu b 2 ribu c 3 ribu d 4 ribu e 5 ribu").size) }
    @Test fun connectorRemovedFromName() { assertEquals("Es teh", ExpenseParser.parse("nasi 15 ribu dan es teh 5 ribu").last().name) }
    @Test fun calendarWeekCrossesYear() { val range = periodRange(LocalDate.of(2026,1,1), Period.WEEK); assertEquals(LocalDate.of(2025,12,29), range.start); assertEquals(LocalDate.of(2026,1,4), range.end) }
    @Test fun februaryLeapYear() { assertEquals(LocalDate.of(2028,2,29), periodRange(LocalDate.of(2028,2,12), Period.MONTH).end) }
    @Test fun reportUsesExpenseDateAndIgnoresDeleted() {
        val records = listOf(Expense("a", "Kost", 800000, "2026-10-01", 1, 1), Expense("b", "Kopi", 12000, "2026-10-14", 1, 1), Expense("c", "Salah", 9000, "2026-10-14", 1, 1, true))
        assertEquals(12000L, visibleExpenses(records, periodRange(LocalDate.of(2026,10,14), Period.DAY)).sumOf { it.amount })
        assertEquals(812000L, visibleExpenses(records, periodRange(LocalDate.of(2026,10,14), Period.MONTH)).sumOf { it.amount })
    }
}
