package com.example.data.remote

data class AuthSession(
    val accessToken: String,
    val tokenType: String = "bearer",
    val expiresIn: Long = 3600,
    val refreshToken: String? = null,
    val user: UserInfo
)

data class SignUpOutcome(
    val session: AuthSession?,
    val isEmailConfirmationRequired: Boolean,
    val email: String,
    val message: String
)

data class UserInfo(
    val id: String,
    val email: String?,
    val phone: String?,
    val createdAt: String? = null
)

data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String?,
    val phoneNumber: String?,
    val avatarUrl: String?,
    val role: String = "user", // user, moderator, admin, superadmin
    val isVerified: Boolean = false,
    val isPro: Boolean = false,
    val subscriptionTier: String = "free", // free, trial, pro, premium, lifetime
    val subscriptionExpiresAt: String? = null,
    val twoFactorEnabled: Boolean = false,
    val biometricLockEnabled: Boolean = false,
    val smartSpamBlockingEnabled: Boolean = true,
    val callRecordingEnabled: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    val isAdmin: Boolean get() = role == "admin" || role == "superadmin"
    val isModerator: Boolean get() = role == "moderator" || isAdmin
    val isPremium: Boolean get() = subscriptionTier in listOf("premium", "lifetime") || isAdmin
    val isLifetime: Boolean get() = subscriptionTier == "lifetime"
    val hasActiveEntitlement: Boolean get() = isPro || isPremium || isLifetime || isAdmin
}

data class PlanTierInfo(
    val id: String,
    val displayName: String,
    val pricePkr: Int,
    val priceUsdt: Double = 0.0,
    val isPro: Boolean,
    val durationText: String,
    val badge: String? = null
)

val CALLORA_PLANS = listOf(
    PlanTierInfo("trial_3d", "3 Days Pro Free Trial", 0, 0.0, isPro = true, durationText = "3 Days", badge = "FREE TRIAL"),
    PlanTierInfo("month_1", "1 Month Pro", 99, 0.40, isPro = true, durationText = "1 Month"),
    PlanTierInfo("month_3", "3 Months Pro", 199, 0.80, isPro = true, durationText = "3 Months", badge = "POPULAR"),
    PlanTierInfo("month_6", "6 Months Pro", 499, 1.99, isPro = true, durationText = "6 Months", badge = "BEST VALUE"),
    PlanTierInfo("year_1", "1 Year Premium", 3999, 14.99, isPro = false, durationText = "1 Year", badge = "PREMIUM"),
    PlanTierInfo("lifetime", "Lifetime Access Pro+Premium", 4999, 19.99, isPro = false, durationText = "Lifetime Access", badge = "ULTIMATE")
)

data class PaymentSubmission(
    val id: String,
    val userId: String,
    val gateway: String, // easypaisa, binance_pay
    val planTier: String,
    val amountPkr: Double,
    val amountUsdt: Double? = null,
    val trxId: String,
    val senderAccountTitle: String? = null,
    val receiptStoragePath: String,
    val status: String = "pending", // pending, approved, rejected
    val adminNotes: String? = null,
    val userEmail: String? = null,
    val userName: String? = null,
    val createdAt: String
)

data class CommunityPost(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String?,
    val authorRole: String = "user",
    val authorVerified: Boolean = false,
    val category: String = "scam_alert", // scam_alert, safety_tip, general
    val caption: String,
    val imageUrl: String? = null,
    val flaggedNumber: String? = null,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val createdAt: String
)

data class PostComment(
    val id: String,
    val postId: String,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String?,
    val content: String,
    val createdAt: String
)

data class CallerDirectoryEntry(
    val id: String,
    val phoneNumber: String,
    val displayName: String,
    val organization: String? = null,
    val riskLevel: String = "safe", // safe, low, moderate, high, severe
    val spamReportsCount: Int = 0,
    val isVerifiedBusiness: Boolean = false,
    val category: String? = "Personal"
)

data class CallerReport(
    val id: String,
    val reportedBy: String,
    val phoneNumber: String,
    val callerName: String?,
    val riskLevel: String,
    val details: String?,
    val status: String = "under_review",
    val createdAt: String
)

data class SystemNotification(
    val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val type: String = "system",
    val isRead: Boolean = false,
    val createdAt: String
)
