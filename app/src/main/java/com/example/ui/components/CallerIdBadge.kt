package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SpamRed
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.WarningOrange

enum class CallerIdType {
    KNOWN,
    BUSINESS,
    UNKNOWN,
    SPAM,
    REPORTED,
    BLOCKED
}

@Composable
fun CallerIdBadge(
    type: CallerIdType,
    modifier: Modifier = Modifier,
    customText: String? = null
) {
    val (bgColor, textColor, icon, label) = when (type) {
        CallerIdType.KNOWN -> Quad(
            Color(0xFF10B981).copy(alpha = 0.15f),
            Color(0xFF34D399),
            Icons.Default.Person,
            customText ?: "Known Contact"
        )
        CallerIdType.BUSINESS -> Quad(
            Color(0xFF3B82F6).copy(alpha = 0.15f),
            Color(0xFF60A5FA),
            Icons.Default.Business,
            customText ?: "Verified Business"
        )
        CallerIdType.UNKNOWN -> Quad(
            Color(0xFF64748B).copy(alpha = 0.15f),
            Color(0xFF94A3B8),
            Icons.Default.Shield,
            customText ?: "Unknown Caller"
        )
        CallerIdType.SPAM -> Quad(
            SpamRed.copy(alpha = 0.18f),
            SpamRed,
            Icons.Default.ReportProblem,
            customText ?: "Spam Alert"
        )
        CallerIdType.REPORTED -> Quad(
            WarningOrange.copy(alpha = 0.18f),
            WarningOrange,
            Icons.Default.ReportProblem,
            customText ?: "Community Reported"
        )
        CallerIdType.BLOCKED -> Quad(
            Color(0xFFDC2626).copy(alpha = 0.2f),
            Color(0xFFF87171),
            Icons.Default.Block,
            customText ?: "Blocked"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
