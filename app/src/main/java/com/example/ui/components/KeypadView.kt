package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telephony.SimCardInfo

data class KeypadKey(val digit: String, val letters: String = "")

val KEYPAD_KEYS = listOf(
    listOf(KeypadKey("1", ""), KeypadKey("2", "ABC"), KeypadKey("3", "DEF")),
    listOf(KeypadKey("4", "GHI"), KeypadKey("5", "JKL"), KeypadKey("6", "MNO")),
    listOf(KeypadKey("7", "PQRS"), KeypadKey("8", "TUV"), KeypadKey("9", "WXYZ")),
    listOf(KeypadKey("*", ""), KeypadKey("0", "+"), KeypadKey("#", ""))
)

// Black background, white/light buttons with dark text/icons
val KeypadPureBlack = Color(0xFF000000)
val KeypadButtonLight = Color(0xFFFFFFFF)
val KeypadButtonBorder = Color(0xFFE2E8F0)
val KeypadDigitDark = Color(0xFF0F172A)
val KeypadLettersDark = Color(0xFF475569)

@Composable
fun KeypadView(
    modifier: Modifier = Modifier,
    onDigitClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onCallClick: (simSlot: Int) -> Unit,
    simCards: List<SimCardInfo>,
    defaultSimSlot: Int
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(KeypadPureBlack)
            .padding(horizontal = 24.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 4 Rows of Keys
        KEYPAD_KEYS.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    KeypadButton(
                        digit = key.digit,
                        letters = key.letters,
                        onClick = { onDigitClick(key.digit) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Action Row: Dual SIM Calling Buttons & Backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Spacer/alignment placeholder
            Spacer(modifier = Modifier.size(64.dp))

            // SIM Call Button(s)
            if (simCards.size >= 2) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // SIM 1
                    CallActionButton(
                        simLabel = simCards.getOrNull(0)?.carrierName ?: "SIM 1",
                        slot = 0,
                        isPrimary = defaultSimSlot == 0 || defaultSimSlot == -1,
                        onClick = { onCallClick(0) }
                    )
                    // SIM 2
                    CallActionButton(
                        simLabel = simCards.getOrNull(1)?.carrierName ?: "SIM 2",
                        slot = 1,
                        isPrimary = defaultSimSlot == 1,
                        onClick = { onCallClick(1) }
                    )
                }
            } else {
                // Single Large Call Button
                CallActionButton(
                    simLabel = simCards.firstOrNull()?.carrierName ?: "Call",
                    slot = 0,
                    isPrimary = true,
                    onClick = { onCallClick(0) }
                )
            }

            // Backspace Button (White/light circular surface with dark backspace icon)
            Surface(
                onClick = onBackspace,
                shape = CircleShape,
                color = KeypadButtonLight,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .size(64.dp)
                    .border(1.dp, KeypadButtonBorder, CircleShape)
                    .testTag("keypad_backspace_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = KeypadDigitDark,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    digit: String,
    letters: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = KeypadButtonLight,
        shadowElevation = 3.dp,
        interactionSource = interactionSource,
        modifier = Modifier
            .size(72.dp)
            .border(1.dp, KeypadButtonBorder, CircleShape)
            .testTag("keypad_key_$digit")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = digit,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = KeypadDigitDark
            )
            if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KeypadLettersDark,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun CallActionButton(
    simLabel: String,
    slot: Int,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    val bgBrush = if (isPrimary) {
        Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF3B82F6), Color(0xFF2563EB)))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(32.dp))
            .background(bgBrush)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .testTag("call_action_sim_${slot + 1}"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Call with SIM ${slot + 1}",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = "SIM ${slot + 1}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = simLabel.take(8),
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}
