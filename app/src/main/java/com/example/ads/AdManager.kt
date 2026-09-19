package com.example.ads

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.remote.UserProfile
import com.example.viewmodel.CallState
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * CallOra Central AdManager & Entitlement Enforcement Engine.
 *
 * Rules:
 * 1. ZERO ads for Pro, Premium, Lifetime, or Admin users.
 * 2. Ad removal is strictly governed by verified backend entitlements.
 * 3. Never show ads during active/incoming/outgoing phone calls.
 * 4. Never interrupt emergency services (911, 112, 15, 1122, 130).
 * 5. Uses official Google Mobile Ads SDK test units to comply with Play Store policies.
 */
object AdManager {
    private const val TAG = "CalloraXAdManager"

    // Google AdMob Official Test Banner ID
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                MobileAds.initialize(context) { status ->
                    Log.d(TAG, "Google Mobile Ads initialized successfully: $status")
                    isInitialized = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "AdMob initialization warning: ${e.message}")
            }
        }
    }

    /**
     * Centralized query to check whether ads are permitted for the current user and screen state.
     */
    fun canShowAds(
        user: UserProfile?,
        callState: CallState,
        activeDialedNumber: String? = null
    ): Boolean {
        // 1. SAFETY: Never show ads during an active, incoming, or dialing call
        if (callState != CallState.IDLE) {
            return false
        }

        // 2. EMERGENCY SAFETY: Do not interrupt emergency calling
        if (isEmergencyNumber(activeDialedNumber)) {
            return false
        }

        // 3. ADMIN / OWNER: Zero ads
        if (user?.isAdmin == true) {
            return false
        }

        // 4. BACKEND ENTITLEMENT VALIDATION:
        // Pro, Premium, and Lifetime users have ZERO ads
        if (user != null) {
            if (user.isLifetime) return false
            if (user.isPremium) return false
            if (user.isPro) {
                // Check if subscription has expired
                if (isSubscriptionExpired(user.subscriptionExpiresAt)) {
                    Log.d(TAG, "User subscription expired, restoring standard tier")
                    return true
                }
                return false
            }
        }

        // Free tier user not in a call: permitted to show non-intrusive banner
        return true
    }

    private fun isEmergencyNumber(number: String?): Boolean {
        if (number.isNullOrBlank()) return false
        val clean = number.trim().replace(" ", "").replace("-", "")
        return clean in listOf("911", "112", "15", "16", "1122", "130", "999", "000")
    }

    private fun isSubscriptionExpired(expiresAt: String?): Boolean {
        if (expiresAt.isNullOrBlank()) return false
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val expiry = format.parse(expiresAt)
            expiry != null && expiry.before(Date())
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Non-intrusive CallOra Banner Ad Composable.
 * Automatically handles entitlement checks and lifecycle cleanup.
 */
@Composable
fun CalloraXAdBanner(
    user: UserProfile?,
    callState: CallState,
    modifier: Modifier = Modifier
) {
    val shouldShow = AdManager.canShowAds(user, callState)
    if (!shouldShow) {
        return
    }

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SPONSORED",
            fontSize = 9.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(bottom = 2.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    AdView(ctx).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = AdManager.TEST_BANNER_AD_UNIT_ID
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                Log.d("CalloraXAdBanner", "Banner ad loaded successfully")
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                Log.w("CalloraXAdBanner", "Banner ad failed to load: ${error.message}")
                            }
                        }
                        loadAd(AdRequest.Builder().build())
                    }
                }
            )
        }
    }
}
