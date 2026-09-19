package com.example.data

import android.content.Context
import android.net.Uri
import com.example.data.local.CallOraDatabase
import com.example.data.local.CallRecordEntity
import com.example.data.local.ContactItemEntity
import com.example.data.local.NoteEntity
import com.example.data.local.RecordingItemEntity
import com.example.data.remote.AuthSession
import com.example.data.remote.CallerDirectoryEntry
import com.example.data.remote.CommunityPost
import com.example.data.remote.PaymentSubmission
import com.example.data.remote.SignUpOutcome
import com.example.data.remote.SupabaseClient
import com.example.data.remote.UserProfile
import kotlinx.coroutines.flow.Flow

class CallOraRepository(context: Context) {
    private val db = CallOraDatabase.getDatabase(context)
    val supabase = SupabaseClient(context)

    // Notes
    val allNotes: Flow<List<NoteEntity>> = db.noteDao().getAllNotes()
    fun searchNotes(query: String): Flow<List<NoteEntity>> = db.noteDao().searchNotes(query)
    suspend fun insertNote(note: NoteEntity) = db.noteDao().insertNote(note)
    suspend fun updateNote(note: NoteEntity) = db.noteDao().updateNote(note)
    suspend fun deleteNote(note: NoteEntity) = db.noteDao().deleteNote(note)

    // Call Records / Recents
    val allCallRecords: Flow<List<CallRecordEntity>> = db.callRecordDao().getAllCallRecords()
    val spamAndBlockedRecords: Flow<List<CallRecordEntity>> = db.callRecordDao().getSpamAndBlockedRecords()
    suspend fun insertCallRecord(record: CallRecordEntity) = db.callRecordDao().insertRecord(record)
    suspend fun clearCallRecords() = db.callRecordDao().clearAllRecords()

    // Contacts
    val allContacts: Flow<List<ContactItemEntity>> = db.contactItemDao().getAllContacts()
    val favoriteContacts: Flow<List<ContactItemEntity>> = db.contactItemDao().getFavoriteContacts()
    fun searchContacts(query: String): Flow<List<ContactItemEntity>> = db.contactItemDao().searchContacts(query)
    suspend fun insertContact(contact: ContactItemEntity) = db.contactItemDao().insertOrUpdate(contact)
    suspend fun insertContacts(contacts: List<ContactItemEntity>) = db.contactItemDao().insertAll(contacts)
    suspend fun toggleFavorite(number: String, isFav: Boolean) = db.contactItemDao().setFavorite(number, isFav)
    suspend fun toggleBlock(number: String, isBlocked: Boolean) = db.contactItemDao().setBlocked(number, isBlocked)

    // Voice recordings
    val allRecordings: Flow<List<RecordingItemEntity>> = db.recordingDao().getAllRecordings()
    suspend fun insertRecording(recording: RecordingItemEntity) = db.recordingDao().insertRecording(recording)
    suspend fun deleteRecording(recording: RecordingItemEntity) = db.recordingDao().deleteRecording(recording)

    // Supabase Auth & Profile
    val currentSession: AuthSession? get() = supabase.currentSession
    suspend fun signInEmail(email: String, pass: String) = supabase.signInWithEmail(email, pass)
    suspend fun signUpEmail(email: String, pass: String, name: String, phone: String?) = supabase.signUpWithEmail(email, pass, name, phone)
    suspend fun resendVerificationEmail(email: String) = supabase.resendVerificationEmail(email)
    suspend fun verifyEmailToken(email: String, token: String) = supabase.verifyEmailToken(email, token)
    suspend fun sendPhoneOtp(phone: String) = supabase.sendPhoneOtp(phone)
    suspend fun verifyPhoneOtp(phone: String, code: String) = supabase.verifyPhoneOtp(phone, code)
    suspend fun resetPassword(email: String) = supabase.resetPassword(email)
    fun getGoogleOAuthUrl(): String = supabase.getGoogleOAuthUrl()
    suspend fun handleOAuthCallback(uri: Uri) = supabase.handleOAuthCallback(uri)
    suspend fun getProfile(userId: String) = supabase.getProfile(userId)
    suspend fun updateProfile(userId: String, name: String, phone: String?, avatar: String?) = supabase.updateProfile(userId, name, phone, avatar)
    suspend fun deleteAccount(userId: String) = supabase.deleteAccount(userId)
    fun signOut() = supabase.signOut()

    // Payments & Admin
    suspend fun submitPayment(userId: String, gw: String, tier: String, amount: Double, trx: String, title: String?, receipt: String) =
        supabase.submitPayment(userId, gw, tier, amount, trx, title, receipt)
    suspend fun getPendingPayments() = supabase.getPendingPayments()
    suspend fun reviewPayment(paymentId: String, status: String, notes: String?) = supabase.reviewPayment(paymentId, status, notes)
    suspend fun getAllProfiles() = supabase.getAllProfiles()
    suspend fun updateUserRole(userId: String, role: String) = supabase.updateUserRole(userId, role)
    suspend fun updateUserBadges(userId: String, isVerified: Boolean, isPro: Boolean) = supabase.updateUserBadges(userId, isVerified, isPro)

    // Community
    suspend fun getCommunityPosts() = supabase.getCommunityPosts()
    suspend fun createCommunityPost(authorId: String, cat: String, caption: String, flagged: String?, img: String?) =
        supabase.createCommunityPost(authorId, cat, caption, flagged, img)
    suspend fun deletePost(postId: String) = supabase.deletePost(postId)

    // Caller Directory & Spam
    suspend fun lookupCaller(number: String) = supabase.lookupCaller(number)
    suspend fun reportSpam(userId: String, number: String, name: String?, risk: String, details: String?) =
        supabase.reportSpam(userId, number, name, risk, details)
}
