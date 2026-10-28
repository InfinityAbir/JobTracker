package com.example.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.remote.parseMatchAnalysis
import com.example.domain.model.JobApplication
import com.example.domain.model.JobStatus
import com.example.presentation.offers.chooseComparedOffers
import com.example.util.JobExportHelper
import com.example.util.LocalAutoBackupManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NewFeaturesDataTest {

    // ---- match analysis parsing ----

    @Test
    fun parseMatch_readsPlainJson() {
        val result = parseMatchAnalysis(
            """{"matchPercent": 72, "strengths": ["Kotlin","Compose","Room"], "missingKeywords": ["GraphQL","CI/CD"], "tip": "Add metrics"}"""
        )!!
        assertEquals(72, result.matchPercent)
        assertEquals(listOf("Kotlin", "Compose", "Room"), result.strengths)
        assertEquals(listOf("GraphQL", "CI/CD"), result.missingKeywords)
        assertEquals("Add metrics", result.tip)
    }

    @Test
    fun parseMatch_stripsFencesAndChatter_clampsPercentAndCapsLists() {
        val text = "Sure! ```json\n{\"matchPercent\": 140, \"strengths\": [\"a\",\"b\",\"c\",\"d\"], " +
            "\"missingKeywords\": [\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\"], \"tip\": \"\"}\n``` hope it helps"
        val result = parseMatchAnalysis(text)!!
        assertEquals(100, result.matchPercent)
        assertEquals(3, result.strengths.size)
        assertEquals(8, result.missingKeywords.size)
    }

    @Test
    fun parseMatch_acceptsDecimalAndRejectsGarbage() {
        assertEquals(85, parseMatchAnalysis("""{"matchPercent": 85.4}""")!!.matchPercent)
        assertNull(parseMatchAnalysis("I could not do that"))
        assertNull(parseMatchAnalysis("""{"strengths": ["x"]}"""))
        assertNull(parseMatchAnalysis("""{"matchPercent": "high"}"""))
    }

    // ---- backup / export carries the new fields ----

    private val offer = JobApplication(
        id = 5, companyName = "Acme", jobTitle = "Engineer", dateApplied = "2026-09-01", status = JobStatus.OFFER,
        salary = "150k", workModel = "Remote", offerTotalComp = "190k", offerBenefits = "Health insurance,Annual bonus",
        offerPros = "Team\nGrowth", offerCons = "Pay", prepQuestions = "Q?", prepStar = "S", prepNegotiation = "N",
        interviewingAt = 1234L, lastFollowUpAt = 5678L
    )

    @Test
    fun exportedJson_roundTripsEveryNewField() {
        val restored = LocalAutoBackupManager.parseJsonToApplications(JobExportHelper.generateJson(listOf(offer))).single()
        assertEquals("Remote", restored.workModel)
        assertEquals("190k", restored.offerTotalComp)
        assertEquals(setOf("Health insurance", "Annual bonus"), restored.benefitKeys)
        assertEquals("Team\nGrowth", restored.offerPros)
        assertEquals("Pay", restored.offerCons)
        assertEquals("Q?", restored.prepQuestions)
        assertEquals("S", restored.prepStar)
        assertEquals("N", restored.prepNegotiation)
        assertEquals(1234L, restored.interviewingAt)
        assertEquals(5678L, restored.lastFollowUpAt)
    }

    @Test
    fun backupFromBeforeV4_stillRestores() {
        val legacy = """{"version":1,"applications":[{"id":1,"companyName":"Old Co","jobTitle":"Dev","dateApplied":"2026-08-01","status":"APPLIED"}]}"""
        val restored = LocalAutoBackupManager.parseJsonToApplications(legacy)
        assertEquals(1, restored.size)
        assertEquals("", restored[0].workModel)
        assertEquals(0L, restored[0].interviewingAt)
    }

    @Test
    fun csvIncludesNewColumns() {
        val csv = JobExportHelper.generateCsv(listOf(offer))
        assertTrue(csv.lines().first().endsWith("Work Model,Total Compensation"))
        assertTrue(csv.contains("Remote,190k"))
    }

    // ---- offer comparison selection ----

    private fun offerJob(id: Long) =
        JobApplication(id = id, companyName = "C$id", jobTitle = "T", dateApplied = "", status = JobStatus.OFFER)

    @Test
    fun comparedOffers_defaultToFirstThree_andHonourPicks() {
        val offers = (1L..5L).map(::offerJob)
        assertEquals(listOf(1L, 2L, 3L), chooseComparedOffers(offers, emptyList()).map { it.id })
        assertEquals(listOf(5L, 2L), chooseComparedOffers(offers, listOf(5L, 2L)).map { it.id })
        assertEquals(listOf(5L, 4L, 3L), chooseComparedOffers(offers, listOf(5L, 4L, 3L, 2L)).map { it.id })
        // Picks that no longer exist fall back to the default
        assertEquals(listOf(1L, 2L), chooseComparedOffers(offers.take(2), listOf(99L)).map { it.id })
        assertNotNull(chooseComparedOffers(offers, listOf(99L)).firstOrNull())
    }
}
