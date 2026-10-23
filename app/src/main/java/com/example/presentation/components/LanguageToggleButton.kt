package com.example.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.AppLanguage

/**
 * Two typewriter keycaps for switching language.
 */
@Composable
fun LanguageToggleButton(
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(3.dp)

    Row(
        modifier = modifier
            .testTag("language_toggle_button")
            .defaultMinSize(minWidth = 48.dp, minHeight = 40.dp)
            .hardShadow(2.dp, colors.outline.copy(alpha = 0.45f), shape)
            .clip(shape)
            .background(colors.surface)
            .border(2.dp, colors.outline, shape)
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LanguageKey(
            label = "EN",
            selected = currentLanguage == AppLanguage.ENGLISH,
            onClick = { onLanguageChange(AppLanguage.ENGLISH) },
            testTag = "language_en_button"
        )

        Box(
            modifier = Modifier
                .width(1.dp)
                .height(18.dp)
                .background(colors.outlineVariant)
        )

        LanguageKey(
            label = "BN",
            selected = currentLanguage == AppLanguage.BANGLA,
            onClick = { onLanguageChange(AppLanguage.BANGLA) },
            testTag = "language_bn_button"
        )
    }
}

@Composable
private fun LanguageKey(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val colors = MaterialTheme.colorScheme
    val background by animateColorAsState(
        targetValue = if (selected) colors.primary else Color.Transparent,
        animationSpec = tween(180),
        label = "language_key_bg"
    )
    val content by animateColorAsState(
        targetValue = if (selected) colors.onPrimary else colors.onSurfaceVariant,
        animationSpec = tween(180),
        label = "language_key_ink"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(2.dp))
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = colors.primary),
                onClick = onClick
            )
            .padding(horizontal = 9.dp, vertical = 6.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                letterSpacing = 0.8.sp
            ),
            fontWeight = FontWeight.Bold,
            color = content
        )
    }
}
