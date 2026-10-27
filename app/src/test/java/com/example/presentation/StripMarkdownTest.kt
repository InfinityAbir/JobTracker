package com.example.presentation

import com.example.presentation.ai.stripMarkdown
import org.junit.Assert.assertEquals
import org.junit.Test

class StripMarkdownTest {

    @Test
    fun removesHeadingsBoldAndRules() {
        val raw = "### 2. Talking Points\n\n1. **Deep Compose:** five years\n---\n* Kotlin\n* Room"
        assertEquals("2. Talking Points\n\n1. Deep Compose: five years\n\n• Kotlin\n• Room", stripMarkdown(raw))
    }

    @Test
    fun removesStarAndUnderscoreDividers() {
        assertEquals("Intro\n\nBody", stripMarkdown("Intro\n***\nBody"))
        assertEquals("Intro\n\nBody", stripMarkdown("Intro\n___\nBody"))
    }

    @Test
    fun leavesPlainTextAlone() {
        val raw = "Subject: Thank you\n\nDear Sarah,\nThanks for your time."
        assertEquals(raw, stripMarkdown(raw))
    }
}
