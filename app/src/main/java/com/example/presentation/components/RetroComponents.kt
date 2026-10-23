package com.example.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.JobStatus
import com.example.ui.theme.StatusAppliedBgDark
import com.example.ui.theme.StatusAppliedBgLight
import com.example.ui.theme.StatusAppliedDark
import com.example.ui.theme.StatusAppliedLight
import com.example.ui.theme.StatusDraftBgDark
import com.example.ui.theme.StatusDraftBgLight
import com.example.ui.theme.StatusDraftDark
import com.example.ui.theme.StatusDraftLight
import com.example.ui.theme.StatusInterviewingBgDark
import com.example.ui.theme.StatusInterviewingBgLight
import com.example.ui.theme.StatusInterviewingDark
import com.example.ui.theme.StatusInterviewingLight
import com.example.ui.theme.StatusOfferBgDark
import com.example.ui.theme.StatusOfferBgLight
import com.example.ui.theme.StatusOfferDark
import com.example.ui.theme.StatusOfferLight
import com.example.ui.theme.StatusRejectedBgDark
import com.example.ui.theme.StatusRejectedBgLight
import com.example.ui.theme.StatusRejectedDark
import com.example.ui.theme.StatusRejectedLight

/** Shared field radius for every retro input. */
val RetroFieldShape = RoundedCornerShape(3.dp)

/** Hard-edged drop shadow — no blur, like an offset print block. */
fun Modifier.hardShadow(
    offset: Dp = 3.dp,
    color: Color,
    shape: Shape
): Modifier = drawBehind {
    if (offset > 0.dp) {
        val outline = shape.createOutline(size, layoutDirection, this)
        translate(left = offset.toPx(), top = offset.toPx()) {
            drawOutline(outline, color = color)
        }
    }
}

/**
 * The ruled-paper backdrop used behind every screen: faint notebook lines,
 * a dot grid and registration marks in the corners.
 */
@Composable
fun RetroScreenBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val background = MaterialTheme.colorScheme.background
    val ruleColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.055f)
    val dotColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
    val markColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .drawBehind {
                val ruleGap = 7.dp.toPx()
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = ruleColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                    y += ruleGap
                }

                val dotGap = 26.dp.toPx()
                var dotY = dotGap
                while (dotY < size.height) {
                    var dotX = dotGap
                    while (dotX < size.width) {
                        drawCircle(color = dotColor, radius = 1.1f, center = Offset(dotX, dotY))
                        dotX += dotGap
                    }
                    dotY += dotGap
                }

                val mark = 9.dp.toPx()
                val inset = 12.dp.toPx()
                val corners = listOf(
                    Offset(inset, inset),
                    Offset(size.width - inset, inset),
                    Offset(inset, size.height - inset),
                    Offset(size.width - inset, size.height - inset)
                )
                corners.forEach { c ->
                    drawLine(markColor, Offset(c.x - mark, c.y), Offset(c.x + mark, c.y), 1.2f)
                    drawLine(markColor, Offset(c.x, c.y - mark), Offset(c.x, c.y + mark), 1.2f)
                }
            },
        content = content
    )
}

/**
 * Bordered paper panel with a hard offset shadow.
 */
@Composable
fun RetroPanel(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    shadowColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
    shadowOffset: Dp = 3.dp,
    shape: Shape = RoundedCornerShape(4.dp),
    borderWidth: Dp = 2.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val animatedShadow by animateDpAsState(
        targetValue = if (pressed && onClick != null) 1.dp else shadowOffset,
        label = "retro_panel_shadow"
    )

    Box(
        modifier = modifier
            .hardShadow(animatedShadow, shadowColor, shape)
            .clip(shape)
            .background(backgroundColor)
            .border(borderWidth, borderColor, shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = enabled,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .alpha(if (enabled) 1f else 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

/** Chunky letterpress-style button. */
enum class RetroButtonStyle { Primary, Secondary, Danger, Ink }

@Composable
fun RetroButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    style: RetroButtonStyle = RetroButtonStyle.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    compact: Boolean = false,
    backgroundColor: Color? = null,
    contentColor: Color? = null
) {
    val colors = MaterialTheme.colorScheme
    val resolvedBackground = backgroundColor ?: when (style) {
        RetroButtonStyle.Primary -> colors.primary
        RetroButtonStyle.Secondary -> colors.surface
        RetroButtonStyle.Danger -> colors.error
        RetroButtonStyle.Ink -> colors.onSurface
    }
    val resolvedContent = contentColor ?: when (style) {
        RetroButtonStyle.Primary -> colors.onPrimary
        RetroButtonStyle.Secondary -> colors.onSurface
        RetroButtonStyle.Danger -> colors.onError
        RetroButtonStyle.Ink -> colors.background
    }
    val background = resolvedBackground
    val content = resolvedContent
    val border = colors.outline
    val shape = RoundedCornerShape(4.dp)

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val shadow by animateDpAsState(
        targetValue = if (pressed) 1.dp else 3.dp,
        label = "retro_button_shadow"
    )

    Box(
        modifier = modifier
            .hardShadow(shadow, border.copy(alpha = 0.55f), shape)
            .clip(shape)
            .background(background)
            .border(2.dp, border, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !loading,
                onClick = onClick
            )
            .alpha(if (enabled) 1f else 0.45f)
            .padding(
                horizontal = if (compact) 12.dp else 18.dp,
                vertical = if (compact) 9.dp else 14.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = content,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(if (compact) 16.dp else 19.dp)
                )
                Spacer(modifier = Modifier.width(9.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = if (compact) 11.sp else 12.5.sp
                ),
                fontWeight = FontWeight.Bold,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Square, shadowed icon button used in headers and cards. */
@Composable
fun RetroIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    size: Dp = 40.dp,
    shadowOffset: Dp = 2.dp
) {
    val shape = RoundedCornerShape(4.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val shadow by animateDpAsState(
        targetValue = if (pressed) 0.dp else shadowOffset,
        label = "retro_icon_shadow"
    )

    Box(
        modifier = modifier
            .size(size)
            .hardShadow(shadow, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), shape)
            .clip(shape)
            .background(backgroundColor)
            .border(2.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size * 0.48f)
        )
    }
}

/** Rubber-stamp label. */
@Composable
fun RetroStamp(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = color.copy(alpha = 0.12f),
    borderColor: Color = color,
    rotation: Float = 0f
) {
    Box(
        modifier = modifier
            .rotate(rotation)
            .clip(RoundedCornerShape(2.dp))
            .background(backgroundColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(2.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.5.sp,
                letterSpacing = 1.sp
            ),
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Small bordered data tag (dates, salary, contacts). */
@Composable
fun RetroTag(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    backgroundColor: Color = MaterialTheme.colorScheme.surface
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(2.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Dashed rule for ticket perforations. */
@Composable
fun RetroDashedDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
    thickness: Dp = 1.dp
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
    ) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = thickness.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(7.dp.toPx(), 5.dp.toPx()),
                0f
            )
        )
    }
}

/** Double rule that caps a masthead. */
@Composable
fun RetroTopBarRule(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(2.dp))
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
        )
    }
}

/** Numbered section heading, e.g. "02 — CONTACT CARD". */
@Composable
fun RetroSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    number: String? = null,
    icon: ImageVector? = null,
    accent: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (number != null) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(accent, RoundedCornerShape(2.dp))
                    .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    fontWeight = FontWeight.Bold,
                    color = onAccent(accent)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(10.dp))
        RetroDashedDivider(
            modifier = Modifier.weight(1f),
            color = accent.copy(alpha = 0.35f)
        )
    }
}

/** The primary "stamp" FAB. */
@Composable
fun RetroFab(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    RetroButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        icon = icon,
        style = RetroButtonStyle.Primary
    )
}

/** Colours for a given job status, adapted to the active theme. */
@Immutable
data class RetroStatusColors(
    val ink: Color,
    val background: Color
)

@Composable
fun retroStatusColors(status: JobStatus): RetroStatusColors {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return when (status) {
        JobStatus.APPLIED -> if (isDark) {
            RetroStatusColors(StatusAppliedDark, StatusAppliedBgDark)
        } else {
            RetroStatusColors(StatusAppliedLight, StatusAppliedBgLight)
        }

        JobStatus.INTERVIEWING -> if (isDark) {
            RetroStatusColors(StatusInterviewingDark, StatusInterviewingBgDark)
        } else {
            RetroStatusColors(StatusInterviewingLight, StatusInterviewingBgLight)
        }

        JobStatus.OFFER -> if (isDark) {
            RetroStatusColors(StatusOfferDark, StatusOfferBgDark)
        } else {
            RetroStatusColors(StatusOfferLight, StatusOfferBgLight)
        }

        JobStatus.REJECTED -> if (isDark) {
            RetroStatusColors(StatusRejectedDark, StatusRejectedBgDark)
        } else {
            RetroStatusColors(StatusRejectedLight, StatusRejectedBgLight)
        }
    }
}

@Composable
fun retroDraftColors(): RetroStatusColors {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (isDark) {
        RetroStatusColors(StatusDraftDark, StatusDraftBgDark)
    } else {
        RetroStatusColors(StatusDraftLight, StatusDraftBgLight)
    }
}

/** Pick readable content colour for an accent block. */
@Composable
fun onAccent(color: Color): Color {
    return if (color.luminance() > 0.55f) {
        Color(0xFF241A12)
    } else {
        Color(0xFFFBF2DF)
    }
}

/** Shared colours for all text fields so forms look like ledger paper. */
@Composable
fun retroTextFieldColors(): TextFieldColors {
    val colors = MaterialTheme.colorScheme
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = colors.onSurface,
        unfocusedTextColor = colors.onSurface,
        disabledTextColor = colors.onSurfaceVariant,
        focusedContainerColor = colors.surface,
        unfocusedContainerColor = colors.surface,
        disabledContainerColor = colors.surfaceVariant,
        errorContainerColor = colors.surface,
        cursorColor = colors.primary,
        focusedBorderColor = colors.primary,
        unfocusedBorderColor = colors.outline,
        disabledBorderColor = colors.outlineVariant,
        errorBorderColor = colors.error,
        focusedLabelColor = colors.primary,
        unfocusedLabelColor = colors.onSurfaceVariant,
        errorLabelColor = colors.error,
        focusedLeadingIconColor = colors.primary,
        unfocusedLeadingIconColor = colors.onSurfaceVariant,
        focusedTrailingIconColor = colors.primary,
        unfocusedTrailingIconColor = colors.onSurfaceVariant,
        focusedPlaceholderColor = colors.onSurfaceVariant.copy(alpha = 0.55f),
        unfocusedPlaceholderColor = colors.onSurfaceVariant.copy(alpha = 0.55f),
        focusedSupportingTextColor = colors.onSurfaceVariant,
        unfocusedSupportingTextColor = colors.onSurfaceVariant,
        errorSupportingTextColor = colors.error
    )
}

/** Content colour for a surface, exposed so custom rows stay legible. */
@Composable
fun retroContentColor(): Color = LocalContentColor.current

/** A compact horizontal stack used by headers. */
@Composable
fun RetroMetaRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        content = content
    )
}
