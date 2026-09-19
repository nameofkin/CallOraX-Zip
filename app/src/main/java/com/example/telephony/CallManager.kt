package com.example.telephony

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telecom.TelecomManager
import androidx.core.content.ContextCompat

object CallManager {

    fun makeCall(context: Context, phoneNumber: String, simSlot: Int? = null) {
        val cleanNumber = phoneNumber.replace(" ", "").replace("-", "")
        val uri = Uri.parse("tel:$cleanNumber")

        val hasCallPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val intent = if (hasCallPermission) {
            Intent(Intent.ACTION_CALL, uri)
        } else {
            Intent(Intent.ACTION_DIAL, uri)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        // Pass SIM selection extra if available on Android
        if (simSlot != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
                val accounts = telecomManager?.callCapablePhoneAccounts
                if (!accounts.isNullOrEmpty() && simSlot < accounts.size) {
                    intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, accounts[simSlot])
                }
            } catch (e: Exception) {
                // Ignore telecom extra failure
            }
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to dialer intent
            val fallback = Intent(Intent.ACTION_DIAL, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        }
    }

    fun openSms(context: Context, phoneNumber: String, message: String = "") {
        val uri = Uri.parse("sms:${phoneNumber.replace(" ", "")}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // SMS app not found
        }
    }

    fun openWhatsApp(context: Context, phoneNumber: String) {
        // Strip non-digits except leading plus
        val digitsOnly = phoneNumber.filter { it.isDigit() }
        val uri = Uri.parse("https://wa.me/$digitsOnly")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Browser or WhatsApp not found
        }
    }

    fun openTelegram(context: Context, phoneNumber: String) {
        val digitsOnly = phoneNumber.filter { it.isDigit() }
        val uri = Uri.parse("https://t.me/+$digitsOnly")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Telegram not found
        }
    }
}
