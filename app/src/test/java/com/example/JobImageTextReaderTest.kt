package com.example

import com.example.util.JobImageTextReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JobImageTextReaderTest {

    @Test
    fun tidy_trimsTrailingSpacesAndCollapsesBlankRuns() {
        val raw = "Software Engineer   \n\n\n\nAcme Corp  \nSalary: 50k\n\n"
        assertEquals("Software Engineer\n\nAcme Corp\nSalary: 50k", JobImageTextReader.tidy(raw))
    }

    @Test
    fun tidy_blankInputStaysBlank() {
        assertEquals("", JobImageTextReader.tidy("  \n \n"))
    }

    @Test
    fun tidy_capsVeryLongText() {
        val result = JobImageTextReader.tidy("a".repeat(20_000))
        assertTrue(result.length <= 8000)
    }
}
