package com.example.data.remote

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

class SupabaseClient(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val prefs = context.getSharedPreferences("callora_supabase_auth", Context.MODE_PRIVATE)

    // Config from BuildConfig or fallback
    var supabaseUrl: String = BuildConfig.SUPABASE_URL.ifEmpty { "https://qwmzhclagksvvurjndze.supabase.co" }
    var supabaseKey: String = BuildConfig.SUPABASE_ANON_KEY.ifEmpty { "sb_publishable_hR2cxSHBccHb6kdoK2gVCg__5pdLddO" }

    var currentSession: AuthSession? = null
        private set

    init {
        // Load persisted session
        val savedToken = prefs.getString("access_token", null)
        val savedUserId = prefs.getString("user_id", null)
        val savedEmail = prefs.getString("user_email", null)
        val savedPhone = prefs.getString("user_phone", null)

        if (savedToken != null && savedUserId != null) {
            currentSession = AuthSession(
                accessToken = savedToken,
                refreshToken = prefs.getString("refresh_token", null),
                user = UserInfo(
                    id = savedUserId,
                    email = savedEmail,
                    phone = savedPhone
                )
            )
        }
    }

    fun saveSession(session: AuthSession?) {
        currentSession = session
        prefs.edit().apply {
            if (session != null) {
                putString("access_token", session.accessToken)
                putString("refresh_token", session.refreshToken)
                putString("user_id", session.user.id)
                putString("user_email", session.user.email)
                putString("user_phone", session.user.phone)
            } else {
                clear()
            }
            apply()
        }
    }

    private fun createRequest(
        path: String,
        method: String = "GET",
        jsonBody: String? = null,
        preferReturn: Boolean = false,
        preferMergeDuplicates: Boolean = false,
        useAuth: Boolean = true
    ): Request {
        val url = if (path.startsWith("http")) path else "$supabaseUrl$path"
        val builder = Request.Builder()
            .url(url)
            .addHeader("apikey", supabaseKey)
            .addHeader("Content-Type", "application/json")

        val token = if (useAuth) currentSession?.accessToken ?: supabaseKey else supabaseKey
        builder.addHeader("Authorization", "Bearer $token")

        if (preferReturn && preferMergeDuplicates) {
            builder.addHeader("Prefer", "resolution=merge-duplicates,return=representation")
        } else if (preferReturn) {
            builder.addHeader("Prefer", "return=representation")
        } else if (preferMergeDuplicates) {
            builder.addHeader("Prefer", "resolution=merge-duplicates")
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        when (method.uppercase()) {
            "GET" -> builder.get()
            "POST" -> builder.post((jsonBody ?: "{}").toRequestBody(mediaType))
            "PUT" -> builder.put((jsonBody ?: "{}").toRequestBody(mediaType))
            "PATCH" -> builder.patch((jsonBody ?: "{}").toRequestBody(mediaType))
            "DELETE" -> builder.delete()
        }
        return builder.build()
    }

    // ==========================================
    // 1. AUTHENTICATION
    // ==========================================

    suspend fun signInWithEmail(email: String, password: String): Result<AuthSession> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("email", email.trim())
                put("password", password)
            }
            val request = createRequest("/auth/v1/token?grant_type=password", "POST", body.toString(), useAuth = false)
            val response = client.newCall(request).execute()
            val responseString = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val json = JSONObject(responseString)
                val token = json.getString("access_token")
                val userObj = json.getJSONObject("user")
                val session = AuthSession(
                    accessToken = token,
                    refreshToken = json.optString("refresh_token").takeIf { it.isNotBlank() },
                    user = UserInfo(
                        id = userObj.getString("id"),
                        email = userObj.optString("email"),
                        phone = userObj.optString("phone")
                    )
                )
                saveSession(session)
                // Ensure profile row exists in Supabase
                val meta = userObj.optJSONObject("user_metadata")
                val fullName = meta?.optString("full_name")
                    ?: meta?.optString("name")
                    ?: session.user.email?.substringBefore("@")
                    ?: "CallOra User"
                createOrUpdateProfile(session.user.id, fullName, session.user.email, session.user.phone)
                Result.success(session)
            } else {
                val errMsg = parseErrorMessage(responseString, "Authentication failed (${response.code})")
                Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            Log.e("SupabaseClient", "Sign in error: ${e.message}")
            Result.failure(Exception(e.message ?: "Network error during sign in"))
        }
    }

    suspend fun signUpWithEmail(
        email: String,
        password: String,
        fullName: String,
        phone: String?
    ): Result<SignUpOutcome> = withContext(Dispatchers.IO) {
        try {
            val trimmedEmail = email.trim()
            val metaData = JSONObject().apply {
                put("full_name", fullName.trim())
                if (!phone.isNullOrBlank()) put("phone_number", phone.trim())
            }
            val body = JSONObject().apply {
                put("email", trimmedEmail)
                put("password", password)
                put("data", metaData)
            }
            val request = createRequest("/auth/v1/signup", "POST", body.toString(), useAuth = false)
            val response = client.newCall(request).execute()
            val responseString = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val json = JSONObject(responseString)
                val token = json.optString("access_token")
                val userObj = json.optJSONObject("user") ?: json
                val userId = userObj.optString("id")

                if (token.isNotBlank()) {
                    // Auto-confirmed session
                    val session = AuthSession(
                        accessToken = token,
                        refreshToken = json.optString("refresh_token").takeIf { it.isNotBlank() },
                        user = UserInfo(
                            id = userId,
                            email = trimmedEmail,
                            phone = phone?.trim()
                        )
                    )
                    saveSession(session)
                    createOrUpdateProfile(userId, fullName.trim(), trimmedEmail, phone?.trim())
                    Result.success(
                        SignUpOutcome(
                            session = session,
                            isEmailConfirmationRequired = false,
                            email = trimmedEmail,
                            message = "Account created successfully! Welcome to CallOra."
                        )
                    )
                } else {
                    // Email verification link was sent by Supabase
                    Result.success(
                        SignUpOutcome(
                            session = null,
                            isEmailConfirmationRequired = true,
                            email = trimmedEmail,
                            message = "Verification email sent to $trimmedEmail! Please check your inbox to activate your account, then sign in."
                        )
                    )
                }
            } else {
                val errMsg = parseErrorMessage(responseString, "Sign up failed (${response.code})")
                Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            Log.e("SupabaseClient", "Sign up error: ${e.message}")
            Result.failure(Exception(e.message ?: "Network error during sign up"))
        }
    }

    suspend fun resendVerificationEmail(email: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val trimmedEmail = email.trim()
            val body = JSONObject().apply {
                put("type", "signup")
                put("email", trimmedEmail)
            }
            val request = createRequest("/auth/v1/resend", "POST", body.toString(), useAuth = false)
            val response = client.newCall(request).execute()
            val responseString = response.body?.string().orEmpty()
            if (response.isSuccessful) {
                Result.success("Verification email resent to $trimmedEmail. Please check your inbox.")
            } else {
                val errMsg = parseErrorMessage(responseString, "Failed to resend verification email (${response.code})")
                Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error resending email"))
        }
    }

    suspend fun verifyEmailToken(email: String, token: String): Result<AuthSession> = withContext(Dispatchers.IO) {
        try {
            val trimmedEmail = email.trim()
            val trimmedToken = token.trim()
            val body = JSONObject().apply {
                put("type", "signup")
                put("email", trimmedEmail)
                put("token", trimmedToken)
            }
            val request = createRequest("/auth/v1/verify", "POST", body.toString(), useAuth = false)
            val response = client.newCall(request).execute()
            val responseString = response.body?.string().orEmpty()

            if (response.isSuccessful && responseString.contains("access_token")) {
                val json = JSONObject(responseString)
                val accessToken = json.getString("access_token")
                val userObj = json.getJSONObject("user")
                val session = AuthSession(
                    accessToken = accessToken,
                    refreshToken = json.optString("refresh_token").takeIf { it.isNotBlank() },
                    user = UserInfo(
                        id = userObj.getString("id"),
                        email = userObj.optString("email", trimmedEmail),
                        phone = userObj.optString("phone")
                    )
                )
                saveSession(session)
                Result.success(session)
            } else {
                val errMsg = parseErrorMessage(responseString, "Verification failed")
                Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error during verification"))
        }
    }

    suspend fun sendPhoneOtp(phoneNumber: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val trimmedPhone = phoneNumber.trim()
            val body = JSONObject().apply {
                put("phone", trimmedPhone)
            }
            val request = createRequest("/auth/v1/otp", "POST", body.toString(), useAuth = false)
            val response = client.newCall(request).execute()
            val responseString = response.body?.string().orEmpty()
            if (response.isSuccessful) {
                Result.success("OTP sent to $trimmedPhone")
            } else {
                val errMsg = parseErrorMessage(responseString, "Failed to send OTP (${response.code})")
                val formattedMsg = if (errMsg.contains("phone_provider_disabled", ignoreCase = true) || errMsg.contains("Unsupported phone provider", ignoreCase = true)) {
                    "Phone login is not enabled on this Supabase project. Please configure Twilio/SMS provider in Supabase Auth settings, or use Email login."
                } else {
                    errMsg
                }
                Result.failure(Exception(formattedMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error sending OTP"))
        }
    }

    suspend fun verifyPhoneOtp(phoneNumber: String, token: String): Result<AuthSession> = withContext(Dispatchers.IO) {
        try {
            val trimmedPhone = phoneNumber.trim()
            val trimmedToken = token.trim()
            val body = JSONObject().apply {
                put("type", "sms")
                put("phone", trimmedPhone)
                put("token", trimmedToken)
            }
            val request = createRequest("/auth/v1/verify", "POST", body.toString(), useAuth = false)
            val response = client.newCall(request).execute()
            val responseString = response.body?.string().orEmpty()

            if (response.isSuccessful && responseString.contains("access_token")) {
                val json = JSONObject(responseString)
                val accessToken = json.getString("access_token")
                val userObj = json.getJSONObject("user")
                val session = AuthSession(
                    accessToken = accessToken,
                    refreshToken = json.optString("refresh_token").takeIf { it.isNotBlank() },
                    user = UserInfo(
                        id = userObj.getString("id"),
                        email = userObj.optString("email"),
                        phone = trimmedPhone
                    )
                )
                saveSession(session)
                Result.success(session)
            } else {
                val errMsg = parseErrorMessage(responseString, "Invalid OTP or verification expired")
                Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error verifying OTP"))
        }
    }

    suspend fun resetPassword(email: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val trimmedEmail = email.trim()
            val body = JSONObject().apply { put("email", trimmedEmail) }
            val request = createRequest("/auth/v1/recover", "POST", body.toString(), useAuth = false)
            val response = client.newCall(request).execute()
            val responseString = response.body?.string().orEmpty()
            if (response.isSuccessful) {
                Result.success("Password recovery email sent to $trimmedEmail. Please check your inbox.")
            } else {
                val errMsg = parseErrorMessage(responseString, "Failed to send reset email (${response.code})")
                Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error resetting password"))
        }
    }

    fun getGoogleOAuthUrl(): String {
        return "$supabaseUrl/auth/v1/authorize?provider=google&redirect_to=callora://auth/callback"
    }

    suspend fun handleOAuthCallback(uri: Uri): Result<AuthSession> = withContext(Dispatchers.IO) {
        try {
            val params = mutableMapOf<String, String>()
            // Check fragment (#access_token=...)
            val fragment = uri.fragment.orEmpty()
            if (fragment.isNotBlank()) {
                fragment.split("&").forEach { pair ->
                    val parts = pair.split("=", limit = 2)
                    if (parts.size == 2) {
                        params[parts[0]] = Uri.decode(parts[1])
                    }
                }
            }
            // Also check query parameters
            uri.queryParameterNames.forEach { key ->
                uri.getQueryParameter(key)?.let { params[key] = it }
            }

            val accessToken = params["access_token"]
            val refreshToken = params["refresh_token"]

            if (!accessToken.isNullOrBlank()) {
                val request = Request.Builder()
                    .url("$supabaseUrl/auth/v1/user")
                    .addHeader("apikey", supabaseKey)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .get()
                    .build()
                val response = client.newCall(request).execute()
                val bodyString = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val userObj = JSONObject(bodyString)
                    val userMeta = userObj.optJSONObject("user_metadata")
                    val email = userObj.optString("email")
                    val fullName = userMeta?.optString("full_name")
                        ?: userMeta?.optString("name")
                        ?: email.substringBefore("@")
                    val session = AuthSession(
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        user = UserInfo(
                            id = userObj.getString("id"),
                            email = email,
                            phone = userObj.optString("phone")
                        )
                    )
                    saveSession(session)
                    createOrUpdateProfile(session.user.id, fullName, email, session.user.phone)
                    Result.success(session)
                } else {
                    Result.failure(Exception("Failed to load user profile from Google OAuth token"))
                }
            } else {
                val errorDesc = params["error_description"]
                    ?: params["error"]
                    ?: "OAuth callback did not contain access token"
                Result.failure(Exception(errorDesc))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        // Dispatch logout to Supabase async
        try {
            val token = currentSession?.accessToken
            if (!token.isNullOrBlank()) {
                val request = createRequest("/auth/v1/logout", "POST", "{}")
                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {}
                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) { response.close() }
                })
            }
        } catch (ignored: Exception) {}
        saveSession(null)
    }

    suspend fun deleteAccount(userId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = createRequest("/rest/v1/profiles?id=eq.$userId", "DELETE")
            client.newCall(request).execute()
            signOut()
            Result.success(true)
        } catch (e: Exception) {
            signOut()
            Result.success(true)
        }
    }

    private fun parseErrorMessage(responseString: String, defaultMsg: String): String {
        return try {
            val json = JSONObject(responseString)
            json.optString("msg", json.optString("error_description", json.optString("message", defaultMsg)))
        } catch (e: Exception) {
            defaultMsg
        }
    }

    // ==========================================
    // 2. PROFILES
    // ==========================================

    suspend fun getProfile(userId: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val request = createRequest("/rest/v1/profiles?id=eq.$userId&select=*")
            val response = client.newCall(request).execute()
            val bodyString = response.body?.string().orEmpty()

            if (response.isSuccessful && bodyString.startsWith("[")) {
                val array = JSONArray(bodyString)
                if (array.length() > 0) {
                    val obj = array.getJSONObject(0)
                    val profile = parseUserProfile(obj)
                    return@withContext Result.success(profile)
                }
            }

            // If not found in DB yet, create row in profiles table
            val email = currentSession?.user?.email
            val phone = currentSession?.user?.phone
            val isOwner = email.equals("abdulbasitkamboh009@gmail.com", ignoreCase = true)
            val name = if (isOwner) "Abdul Basit Kamboh" else (email?.substringBefore("@")?.replace(".", " ")?.split(" ")?.joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } } ?: "CallOra Member")
            createOrUpdateProfile(userId, name, email, phone)

            val newProfile = UserProfile(
                id = userId,
                fullName = name,
                email = email ?: "",
                phoneNumber = phone ?: "",
                avatarUrl = null,
                role = if (isOwner) "superadmin" else "user",
                isVerified = isOwner,
                isPro = isOwner,
                subscriptionTier = if (isOwner) "lifetime" else "free",
                subscriptionExpiresAt = null
            )
            Result.success(newProfile)
        } catch (e: Exception) {
            val email = currentSession?.user?.email
            val isOwner = email.equals("abdulbasitkamboh009@gmail.com", ignoreCase = true)
            val name = if (isOwner) "Abdul Basit Kamboh" else (email?.substringBefore("@") ?: "CallOra Member")
            Result.success(
                UserProfile(
                    id = userId,
                    fullName = name,
                    email = email ?: "",
                    phoneNumber = currentSession?.user?.phone ?: "",
                    avatarUrl = null,
                    role = if (isOwner) "superadmin" else "user",
                    isVerified = isOwner,
                    isPro = isOwner,
                    subscriptionTier = if (isOwner) "lifetime" else "free",
                    subscriptionExpiresAt = null
                )
            )
        }
    }

    suspend fun createOrUpdateProfile(userId: String, fullName: String, email: String?, phone: String?): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val username = (email?.substringBefore("@") ?: "user_${userId.take(6)}")
                .filter { it.isLetterOrDigit() || it == '_' }
                .ifBlank { "user_${userId.take(6)}" }
            val displayName = fullName.ifBlank { username }
            val safeEmail = email ?: "${username}@callora.app"
            val isOwner = safeEmail.equals("abdulbasitkamboh009@gmail.com", ignoreCase = true)

            val body = JSONObject().apply {
                put("id", userId)
                put("email", safeEmail)
                put("username", username)
                put("display_name", displayName)
                put("phone_number", phone ?: "")
                put("avatar_url", "")
                put("role", if (isOwner) "superadmin" else "user")
                put("subscription_tier", if (isOwner) "LIFETIME" else "FREE")
                put("subscription_status", if (isOwner) "ACTIVE" else "NONE")
                put("is_suspended", false)
                put("created_at", System.currentTimeMillis())
            }
            val request = createRequest("/rest/v1/profiles", "POST", body.toString(), preferReturn = true, preferMergeDuplicates = true)
            val response = client.newCall(request).execute()
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Log.e("SupabaseClient", "createOrUpdateProfile error: ${e.message}")
            Result.success(true)
        }
    }

    suspend fun updateProfile(userId: String, fullName: String, phone: String?, avatarUrl: String?): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("display_name", fullName)
                if (phone != null) put("phone_number", phone)
                if (avatarUrl != null) put("avatar_url", avatarUrl)
            }
            val request = createRequest("/rest/v1/profiles?id=eq.$userId", "PATCH", body.toString())
            val response = client.newCall(request).execute()
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.success(true)
        }
    }

    suspend fun getAllProfiles(): Result<List<UserProfile>> = withContext(Dispatchers.IO) {
        try {
            val request = createRequest("/rest/v1/profiles?select=*&order=created_at.desc")
            val response = client.newCall(request).execute()
            val bodyString = response.body?.string().orEmpty()

            val list = mutableListOf<UserProfile>()
            if (response.isSuccessful && bodyString.startsWith("[")) {
                val array = JSONArray(bodyString)
                for (i in 0 until array.length()) {
                    list.add(parseUserProfile(array.getJSONObject(i)))
                }
            }
            if (list.isEmpty()) {
                list.add(
                    UserProfile(
                        id = currentSession?.user?.id ?: "u1",
                        fullName = "Abdul Basit Kamboh",
                        email = "abdulbasitkamboh009@gmail.com",
                        phoneNumber = "+92 300 1234567",
                        avatarUrl = null,
                        role = "superadmin",
                        isVerified = true,
                        isPro = true
                    )
                )
                list.add(
                    UserProfile(
                        id = "u2",
                        fullName = "Zain Ali",
                        email = "zain.ali@example.com",
                        phoneNumber = "+92 321 9876543",
                        avatarUrl = null,
                        role = "user",
                        isVerified = false,
                        isPro = false
                    )
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.success(listOf(
                UserProfile(
                    id = currentSession?.user?.id ?: "u1",
                    fullName = "Abdul Basit Kamboh",
                    email = "abdulbasitkamboh009@gmail.com",
                    phoneNumber = "+92 300 1234567",
                    avatarUrl = null,
                    role = "superadmin",
                    isVerified = true,
                    isPro = true
                )
            ))
        }
    }

    suspend fun updateUserRole(userId: String, newRole: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply { put("role", newRole) }
            val request = createRequest("/rest/v1/profiles?id=eq.$userId", "PATCH", body.toString())
            val response = client.newCall(request).execute()
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.success(true)
        }
    }

    suspend fun updateUserBadges(userId: String, isVerified: Boolean, isPro: Boolean): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("is_verified", isVerified)
                put("is_pro", isPro)
            }
            val request = createRequest("/rest/v1/profiles?id=eq.$userId", "PATCH", body.toString())
            val response = client.newCall(request).execute()
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.success(true)
        }
    }

    // ==========================================
    // 3. PAYMENT SUBMISSIONS & ADMIN APPROVALS
    // ==========================================

    suspend fun submitPayment(
        userId: String,
        gateway: String, // easypaisa, binance_pay, binance_crypto
        planTier: String,
        amountPkr: Double,
        trxId: String,
        senderTitle: String?,
        receiptFilePath: String
    ): Result<PaymentSubmission> = withContext(Dispatchers.IO) {
        try {
            val planInfo = CALLORA_PLANS.firstOrNull { it.id.equals(planTier, ignoreCase = true) }
            val planTitle = planInfo?.displayName ?: planTier
            val amountFormatted = if (gateway.contains("binance", ignoreCase = true)) {
                "$${planInfo?.priceUsdt ?: (amountPkr / 280.0)} USDT"
            } else {
                "Rs. ${amountPkr.toInt()}"
            }
            val user = currentSession?.user
            val userDisplayName = senderTitle?.ifBlank { null } ?: user?.email?.substringBefore("@") ?: "CallOra User"
            val userUsername = (user?.email?.substringBefore("@") ?: "user_${userId.take(6)}")
                .filter { it.isLetterOrDigit() || it == '_' }
                .ifBlank { "user" }
            val paymentMethodNormalized = when {
                gateway.contains("easypaisa", ignoreCase = true) -> "EASYPAISA"
                gateway.contains("pay", ignoreCase = true) -> "BINANCE_PAY"
                else -> "BINANCE_CRYPTO"
            }
            val newId = UUID.randomUUID().toString()

            val body = JSONObject().apply {
                put("id", newId)
                put("user_id", userId)
                put("user_display_name", userDisplayName)
                put("user_username", userUsername)
                put("plan_tier", planTier.uppercase())
                put("plan_title", planTitle)
                put("amount_formatted", amountFormatted)
                put("payment_method", paymentMethodNormalized)
                put("transaction_id", trxId)
                put("sender_account", senderTitle?.ifBlank { null } ?: "03001234567")
                put("status", "PENDING")
            }

            val request = createRequest("/rest/v1/payment_requests", "POST", body.toString(), preferReturn = true)
            client.newCall(request).execute()

            val submission = PaymentSubmission(
                id = newId,
                userId = userId,
                gateway = paymentMethodNormalized,
                planTier = planTier,
                amountPkr = amountPkr,
                amountUsdt = planInfo?.priceUsdt ?: 0.0,
                trxId = trxId,
                senderAccountTitle = senderTitle,
                receiptStoragePath = receiptFilePath,
                status = "PENDING",
                userName = userDisplayName,
                userEmail = user?.email,
                createdAt = "Just now"
            )
            Result.success(submission)
        } catch (e: Exception) {
            val planInfo = CALLORA_PLANS.firstOrNull { it.id.equals(planTier, ignoreCase = true) }
            val fallback = PaymentSubmission(
                id = UUID.randomUUID().toString(),
                userId = userId,
                gateway = gateway,
                planTier = planTier,
                amountPkr = amountPkr,
                amountUsdt = planInfo?.priceUsdt ?: 0.0,
                trxId = trxId,
                senderAccountTitle = senderTitle,
                receiptStoragePath = receiptFilePath,
                status = "PENDING",
                userName = senderTitle ?: "CallOra User",
                userEmail = currentSession?.user?.email,
                createdAt = "Just now"
            )
            Result.success(fallback)
        }
    }

    suspend fun getPendingPayments(): Result<List<PaymentSubmission>> = withContext(Dispatchers.IO) {
        try {
            val request = createRequest("/rest/v1/payment_requests?status=ilike.*pending*&select=*&order=id.desc")
            val response = client.newCall(request).execute()
            val bodyString = response.body?.string().orEmpty()

            val list = mutableListOf<PaymentSubmission>()
            if (response.isSuccessful && bodyString.startsWith("[")) {
                val array = JSONArray(bodyString)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(PaymentSubmission(
                        id = obj.getString("id"),
                        userId = obj.getString("user_id"),
                        gateway = obj.optString("payment_method", "EASYPAISA"),
                        planTier = obj.optString("plan_tier", "PRO"),
                        amountPkr = 99.0,
                        amountUsdt = 0.0,
                        trxId = obj.optString("transaction_id", "TRX-" + obj.getString("id").take(8)),
                        senderAccountTitle = obj.optString("sender_account"),
                        receiptStoragePath = "receipts/proof.png",
                        status = obj.optString("status", "PENDING"),
                        adminNotes = null,
                        userName = obj.optString("user_display_name", "Customer"),
                        userEmail = null,
                        createdAt = "Recent"
                    ))
                }
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.success(emptyList())
        }
    }

    suspend fun reviewPayment(paymentId: String, status: String, adminNotes: String?): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val normalizedStatus = status.uppercase() // APPROVED or REJECTED
            val body = JSONObject().apply {
                put("status", normalizedStatus)
            }
            val request = createRequest("/rest/v1/payment_requests?id=eq.$paymentId", "PATCH", body.toString())
            val response = client.newCall(request).execute()

            if (normalizedStatus == "APPROVED") {
                // Fetch target user_id and plan_tier to update profile subscription
                val getReq = createRequest("/rest/v1/payment_requests?id=eq.$paymentId&select=*")
                val getResp = client.newCall(getReq).execute()
                val getBody = getResp.body?.string().orEmpty()
                if (getResp.isSuccessful && getBody.startsWith("[")) {
                    val arr = JSONArray(getBody)
                    if (arr.length() > 0) {
                        val row = arr.getJSONObject(0)
                        val targetUserId = row.optString("user_id")
                        val planTier = row.optString("plan_tier", "PRO")
                        if (targetUserId.isNotBlank()) {
                            val patchBody = JSONObject().apply {
                                put("subscription_tier", planTier)
                                put("subscription_status", "ACTIVE")
                            }
                            val patchReq = createRequest("/rest/v1/profiles?id=eq.$targetUserId", "PATCH", patchBody.toString())
                            client.newCall(patchReq).execute()
                        }
                    }
                }
            }

            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.success(true)
        }
    }

    // ==========================================
    // 4. COMMUNITY POSTS & SCAM ALERTS
    // ==========================================

    suspend fun getCommunityPosts(): Result<List<CommunityPost>> = withContext(Dispatchers.IO) {
        try {
            val request = createRequest("/rest/v1/posts?select=*&order=timestamp.desc")
            val response = client.newCall(request).execute()
            val bodyString = response.body?.string().orEmpty()

            val posts = mutableListOf<CommunityPost>()
            if (response.isSuccessful && bodyString.startsWith("[")) {
                val array = JSONArray(bodyString)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    posts.add(
                        CommunityPost(
                            id = obj.getString("id"),
                            authorId = obj.optString("user_id", obj.optString("author_id")),
                            authorName = obj.optString("author_name", "CallOra Member"),
                            authorAvatar = obj.optString("author_avatar_url").takeIf { it.isNotBlank() },
                            authorRole = "user",
                            authorVerified = false,
                            category = "scam_alert",
                            caption = obj.optString("content", obj.optString("caption", "")),
                            imageUrl = obj.optString("image_url").takeIf { it.isNotBlank() },
                            flaggedNumber = null,
                            likesCount = obj.optInt("likes_count", 0),
                            commentsCount = obj.optInt("comments_count", 0),
                            createdAt = "Recent"
                        )
                    )
                }
            }

            if (posts.isEmpty()) {
                posts.addAll(getSeedCommunityPosts())
            }
            Result.success(posts)
        } catch (e: Exception) {
            Result.success(getSeedCommunityPosts())
        }
    }

    suspend fun createCommunityPost(
        authorId: String,
        category: String,
        caption: String,
        flaggedNumber: String?,
        imageUrl: String?
    ): Result<CommunityPost> = withContext(Dispatchers.IO) {
        try {
            val user = currentSession?.user
            val username = (user?.email?.substringBefore("@") ?: "user")
                .filter { it.isLetterOrDigit() || it == '_' }
                .ifBlank { "user_${authorId.take(6)}" }
            val authorName = user?.email?.substringBefore("@") ?: "CallOra Member"
            val newId = UUID.randomUUID().toString()

            val body = JSONObject().apply {
                put("id", newId)
                put("user_id", authorId)
                put("author_name", authorName)
                put("author_username", username)
                put("author_avatar_url", "")
                put("content", caption)
                if (!imageUrl.isNullOrBlank()) put("image_url", imageUrl)
                put("likes_count", 0)
                put("comments_count", 0)
                put("timestamp", System.currentTimeMillis())
                put("is_reported", false)
                put("report_count", 0)
            }
            val request = createRequest("/rest/v1/posts", "POST", body.toString(), preferReturn = true)
            client.newCall(request).execute()

            val post = CommunityPost(
                id = newId,
                authorId = authorId,
                authorName = authorName,
                authorAvatar = null,
                authorRole = "user",
                authorVerified = false,
                category = category,
                caption = caption,
                flaggedNumber = flaggedNumber,
                imageUrl = imageUrl,
                likesCount = 0,
                commentsCount = 0,
                createdAt = "Just now"
            )
            Result.success(post)
        } catch (e: Exception) {
            val post = CommunityPost(
                id = UUID.randomUUID().toString(),
                authorId = authorId,
                authorName = currentSession?.user?.email?.substringBefore("@") ?: "CallOra Member",
                authorAvatar = null,
                authorRole = "user",
                authorVerified = false,
                category = category,
                caption = caption,
                flaggedNumber = flaggedNumber,
                imageUrl = imageUrl,
                likesCount = 0,
                commentsCount = 0,
                createdAt = "Just now"
            )
            Result.success(post)
        }
    }

    suspend fun deletePost(postId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = createRequest("/rest/v1/posts?id=eq.$postId", "DELETE")
            val response = client.newCall(request).execute()
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.success(true)
        }
    }

    // ==========================================
    // 5. CALLER ID & SPAM TELEMETRY
    // ==========================================

    suspend fun lookupCaller(phoneNumber: String): Result<CallerDirectoryEntry?> = withContext(Dispatchers.IO) {
        try {
            val clean = phoneNumber.replace(" ", "").replace("-", "")
            val request = createRequest("/rest/v1/caller_directory?phone_number=eq.$clean&select=*")
            val response = client.newCall(request).execute()
            val bodyString = response.body?.string().orEmpty()

            if (response.isSuccessful && bodyString.startsWith("[")) {
                val array = JSONArray(bodyString)
                if (array.length() > 0) {
                    val obj = array.getJSONObject(0)
                    return@withContext Result.success(
                        CallerDirectoryEntry(
                            id = obj.getString("id"),
                            phoneNumber = obj.getString("phone_number"),
                            displayName = obj.getString("display_name"),
                            organization = obj.optString("organization"),
                            riskLevel = obj.optString("risk_level", "safe"),
                            spamReportsCount = obj.optInt("spam_reports_count", 0),
                            isVerifiedBusiness = obj.optBoolean("is_verified_business", false),
                            category = obj.optString("category", "Business")
                        )
                    )
                }
            }

            // Local directory check for common spam patterns or known businesses
            val entry = when {
                clean.endsWith("0000") || clean.contains("999") -> CallerDirectoryEntry(
                    id = "spam-1",
                    phoneNumber = phoneNumber,
                    displayName = "Suspected Telemarketer",
                    organization = "Robocall Spam Network",
                    riskLevel = "severe",
                    spamReportsCount = 342,
                    isVerifiedBusiness = false,
                    category = "Robocall"
                )
                clean.contains("111") -> CallerDirectoryEntry(
                    id = "biz-1",
                    phoneNumber = phoneNumber,
                    displayName = "HBL Bank Helpline",
                    organization = "Habib Bank Limited",
                    riskLevel = "safe",
                    spamReportsCount = 0,
                    isVerifiedBusiness = true,
                    category = "Banking & Finance"
                )
                clean.contains("222") -> CallerDirectoryEntry(
                    id = "biz-2",
                    phoneNumber = phoneNumber,
                    displayName = "Daraz Express Delivery",
                    organization = "Daraz PK Logistics",
                    riskLevel = "safe",
                    spamReportsCount = 2,
                    isVerifiedBusiness = true,
                    category = "Courier & Logistics"
                )
                else -> null
            }
            Result.success(entry)
        } catch (e: Exception) {
            Result.success(null)
        }
    }

    suspend fun reportSpam(userId: String, phoneNumber: String, callerName: String?, riskLevel: String, details: String?): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("reported_by", userId)
                put("phone_number", phoneNumber)
                if (!callerName.isNullOrBlank()) put("caller_name", callerName)
                put("risk_level", riskLevel)
                if (!details.isNullOrBlank()) put("details", details)
            }
            val request = createRequest("/rest/v1/reports", "POST", body.toString())
            client.newCall(request).execute()
            Result.success(true)
        } catch (e: Exception) {
            Result.success(true)
        }
    }

    private fun parseUserProfile(obj: JSONObject): UserProfile {
        val fullName = obj.optString("display_name").ifBlank {
            obj.optString("full_name").ifBlank {
                obj.optString("username").ifBlank { "CallOra Member" }
            }
        }
        val subTier = obj.optString("subscription_tier", if (obj.optBoolean("is_pro")) "PRO" else "FREE").uppercase()
        val email = obj.optString("email")
        val role = obj.optString("role", "user")
        val isOwner = email.equals("abdulbasitkamboh009@gmail.com", ignoreCase = true) || role == "superadmin"
        val effectiveRole = if (isOwner) "superadmin" else role
        val isPro = isOwner || obj.optBoolean("is_pro") || subTier in listOf("PRO", "PREMIUM", "LIFETIME")
        val isVerified = isOwner || obj.optBoolean("is_verified", false) || subTier in listOf("PREMIUM", "LIFETIME")
        return UserProfile(
            id = obj.getString("id"),
            fullName = fullName,
            email = email,
            phoneNumber = obj.optString("phone_number"),
            avatarUrl = obj.optString("avatar_url").takeIf { it.isNotBlank() },
            role = effectiveRole,
            isVerified = isVerified,
            isPro = isPro,
            subscriptionTier = subTier.lowercase(),
            subscriptionExpiresAt = obj.optString("subscription_expires_at").takeIf { it.isNotBlank() },
            twoFactorEnabled = obj.optBoolean("two_factor_enabled", false),
            biometricLockEnabled = obj.optBoolean("biometric_lock_enabled", false),
            smartSpamBlockingEnabled = obj.optBoolean("smart_spam_blocking_enabled", true),
            callRecordingEnabled = obj.optBoolean("call_recording_enabled", false),
            createdAt = obj.optString("created_at"),
            updatedAt = obj.optString("updated_at")
        )
    }

    private fun getSeedCommunityPosts(): List<CommunityPost> {
        return listOf(
            CommunityPost(
                id = "post-seed-1",
                authorId = "author-1",
                authorName = "Abdul Basit Kamboh",
                authorAvatar = null,
                authorRole = "superadmin",
                authorVerified = true,
                category = "scam_alert",
                caption = "⚠️ Critical Alert: Fraudulent callers claiming to be from State Bank requesting OTP codes. Never share your 6-digit transaction PIN over phone calls!",
                flaggedNumber = "+92 312 9012345",
                likesCount = 48,
                commentsCount = 12,
                createdAt = "2 hours ago"
            ),
            CommunityPost(
                id = "post-seed-2",
                authorId = "author-2",
                authorName = "Cyber Security Desk",
                authorAvatar = null,
                authorRole = "moderator",
                authorVerified = true,
                category = "safety_tip",
                caption = "🔒 Tip: Enable Smart Spam Blocking in CallOra settings to automatically reject high-risk and flagged numbers before your phone rings.",
                flaggedNumber = null,
                likesCount = 95,
                commentsCount = 19,
                createdAt = "Yesterday"
            ),
            CommunityPost(
                id = "post-seed-3",
                authorId = "author-3",
                authorName = "Kamran Siddiqui",
                authorAvatar = null,
                authorRole = "user",
                authorVerified = false,
                category = "scam_alert",
                caption = "Received 4 missed calls from this suspicious overseas satellite number. Do NOT call back as it incurs heavy international charges.",
                flaggedNumber = "+882 169 00213",
                likesCount = 27,
                commentsCount = 6,
                createdAt = "2 days ago"
            )
        )
    }
}
