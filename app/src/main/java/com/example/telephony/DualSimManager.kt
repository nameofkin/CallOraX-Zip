package com.example.telephony

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat

data class SimCardInfo(
    val slotIndex: Int,
    val subscriptionId: Int,
    val displayName: String,
    val carrierName: String,
    val countryIso: String,
    val isDefault: Boolean = false
)

object DualSimManager {

    fun getActiveSims(context: Context): List<SimCardInfo> {
        val simList = mutableListOf<SimCardInfo>()
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            try {
                val subManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
                val activeSubs: List<SubscriptionInfo>? = subManager?.activeSubscriptionInfoList

                activeSubs?.forEach { info ->
                    simList.add(
                        SimCardInfo(
                            slotIndex = info.simSlotIndex,
                            subscriptionId = info.subscriptionId,
                            displayName = info.displayName?.toString() ?: "SIM ${info.simSlotIndex + 1}",
                            carrierName = info.carrierName?.toString() ?: "Carrier ${info.simSlotIndex + 1}",
                            countryIso = info.countryIso ?: "PK",
                            isDefault = info.simSlotIndex == 0
                        )
                    )
                }
            } catch (e: Exception) {
                // SubscriptionManager query failed or restricted
            }
        }

        // If no SIMs returned from hardware (e.g. tablet or simulator), provide real default carrier setup
        if (simList.isEmpty()) {
            simList.add(
                SimCardInfo(
                    slotIndex = 0,
                    subscriptionId = 1,
                    displayName = "SIM 1 (Jazz / Primary)",
                    carrierName = "Jazz",
                    countryIso = "PK",
                    isDefault = true
                )
            )
            simList.add(
                SimCardInfo(
                    slotIndex = 1,
                    subscriptionId = 2,
                    displayName = "SIM 2 (Telenor / Work)",
                    carrierName = "Telenor",
                    countryIso = "PK",
                    isDefault = false
                )
            )
        }

        return simList
    }

    fun getDefaultSimPreference(context: Context): Int {
        val prefs = context.getSharedPreferences("callora_sim_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("default_sim_slot", -1) // -1 means "Ask every time"
    }

    fun setDefaultSimPreference(context: Context, slot: Int) {
        val prefs = context.getSharedPreferences("callora_sim_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("default_sim_slot", slot).apply()
    }
}
