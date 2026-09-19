package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.telephony.DualSimManager
import com.example.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    val tagline = context.getString(R.string.app_tagline)
    assertEquals("CallOraX", appName)
    assertEquals("Your Privacy", tagline)
  }

  @Test
  fun `dual SIM default detection returns valid slots`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sims = DualSimManager.getActiveSims(context)
    assertTrue("Should detect active SIMs or defaults", sims.isNotEmpty())
    assertEquals(0, sims[0].slotIndex)
  }

  @Test
  fun `eleven themes are configured`() {
    assertEquals(11, AppTheme.values().size)
    assertNotNull(AppTheme.DARK)
    assertNotNull(AppTheme.LIGHT)
    assertNotNull(AppTheme.MIDNIGHT)
    assertNotNull(AppTheme.MIXED_PREMIUM)
  }

  @Test
  fun `google oauth url is correctly formatted with deep link callback`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val client = com.example.data.remote.SupabaseClient(context)
    val oauthUrl = client.getGoogleOAuthUrl()
    assertTrue("Should target Supabase authorize endpoint", oauthUrl.contains("/auth/v1/authorize"))
    assertTrue("Should specify google provider", oauthUrl.contains("provider=google"))
    assertTrue("Should redirect to callora auth callback", oauthUrl.contains("redirect_to=callora://auth/callback"))
  }

  @Test
  fun `auth session persistence works correctly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val client = com.example.data.remote.SupabaseClient(context)
    val testSession = com.example.data.remote.AuthSession(
      accessToken = "test_token_12345",
      user = com.example.data.remote.UserInfo(
        id = "test_user_id",
        email = "test@callora.app",
        phone = "+923001234567"
      )
    )
    client.saveSession(testSession)
    val restoredSession = client.currentSession
    assertNotNull(restoredSession)
    assertEquals("test_token_12345", restoredSession?.accessToken)
    assertEquals("test@callora.app", restoredSession?.user?.email)

    client.signOut()
    assertEquals(null, client.currentSession)
  }
}
