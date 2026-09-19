package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalloraCyan
import com.example.ui.theme.CalloraViolet
import com.example.viewmodel.CallOraViewModel

@Composable
fun AuthScreen(
    viewModel: CallOraViewModel,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val authLoading by viewModel.authLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val authSuccess by viewModel.authSuccess.collectAsState()
    val emailVerificationPending by viewModel.emailVerificationPending.collectAsState()

    var isSignUp by remember { mutableStateOf(false) }
    var usePhoneLogin by remember { mutableStateOf(false) }

    // Form inputs
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var phoneOtpCode by remember { mutableStateOf("") }
    var isPhoneOtpSent by remember { mutableStateOf(false) }
    var emailOtpCode by remember { mutableStateOf("") }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF090B14),
                        Color(0xFF111424),
                        Color(0xFF090A12)
                    )
                )
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Brand Logo & Header
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "CalloraX",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "CalloraX",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Text(
                text = "Secure Telecom • Smart Caller ID • Dual SIM",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )

            // Dynamic Feedback: Success Alert
            authSuccess?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B).copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = msg,
                            fontSize = 12.sp,
                            color = Color(0xFFD1FAE5),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Dynamic Feedback: Error Alert
            authError?.let { err ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D).copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = err,
                            fontSize = 12.sp,
                            color = Color(0xFFFEE2E2),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Email Verification Pending Banner & OTP entry
            emailVerificationPending?.let { pendingEmail ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MarkEmailRead,
                                contentDescription = null,
                                tint = CalloraCyan,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Email Verification Required",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }

                        Text(
                            text = "Supabase sent a confirmation message to $pendingEmail. Check your inbox and click the activation link, or paste the 6-digit OTP code below:",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1)
                        )

                        OutlinedTextField(
                            value = emailOtpCode,
                            onValueChange = { emailOtpCode = it },
                            label = { Text("6-Digit Verification Token") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CalloraCyan,
                                unfocusedBorderColor = Color(0xFF475569)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.verifyEmailToken(pendingEmail, emailOtpCode)
                                },
                                enabled = !authLoading && emailOtpCode.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = CalloraCyan),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Verify & Login", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel.resendVerificationEmail(pendingEmail)
                                },
                                enabled = !authLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Resend Email", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Auth Method Toggle (Email vs Phone)
            Row(
                modifier = Modifier.padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = !usePhoneLogin,
                    onClick = { usePhoneLogin = false },
                    label = { Text("Email & Password", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CalloraViolet,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = usePhoneLogin,
                    onClick = { usePhoneLogin = true },
                    label = { Text("Phone OTP", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CalloraViolet,
                        selectedLabelColor = Color.White
                    )
                )
            }

            // Main Auth Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181B28)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (usePhoneLogin) {
                        // Phone Auth Mode
                        Text(
                            text = "Sign in via Phone Number",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )

                        Text(
                            text = "Enter your phone with country code (e.g. +923001234567). Uses Supabase SMS OTP provider.",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            placeholder = { Text("+923001234567") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = CalloraViolet) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CalloraViolet,
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (isPhoneOtpSent) {
                            OutlinedTextField(
                                value = phoneOtpCode,
                                onValueChange = { phoneOtpCode = it },
                                label = { Text("6-Digit SMS OTP Code") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CalloraViolet,
                                    unfocusedBorderColor = Color(0xFF334155)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    viewModel.verifyPhoneOtp(phone, phoneOtpCode)
                                },
                                enabled = !authLoading && phoneOtpCode.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = CalloraViolet),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (authLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                                } else {
                                    Text("Verify OTP & Sign In")
                                }
                            }

                            TextButton(
                                onClick = { viewModel.sendPhoneOtp(phone) },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("Resend SMS OTP", color = CalloraCyan, fontSize = 12.sp)
                            }
                        } else {
                            Button(
                                onClick = {
                                    viewModel.sendPhoneOtp(phone)
                                    isPhoneOtpSent = true
                                },
                                enabled = !authLoading && phone.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = CalloraViolet),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (authLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                                } else {
                                    Text("Send Verification SMS OTP")
                                }
                            }
                        }
                    } else {
                        // Email & Password Mode
                        Text(
                            text = if (isSignUp) "Create CallOra Account" else "Sign In to CallOra",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )

                        if (isSignUp) {
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Full Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CalloraViolet) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CalloraViolet,
                                    unfocusedBorderColor = Color(0xFF334155)
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("fullname_input")
                            )

                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Phone Number (Optional)") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = CalloraViolet) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CalloraViolet,
                                    unfocusedBorderColor = Color(0xFF334155)
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("phone_input")
                            )
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            placeholder = { Text("user@example.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = CalloraViolet) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CalloraViolet,
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("email_input")
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CalloraViolet) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CalloraViolet,
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("password_input")
                        )

                        if (!isSignUp) {
                            Text(
                                text = "Forgot Password?",
                                fontSize = 11.sp,
                                color = CalloraCyan,
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .clickable { showForgotPasswordDialog = true }
                            )
                        }

                        Button(
                            onClick = {
                                if (isSignUp) {
                                    viewModel.signUpEmail(email, password, fullName, phone)
                                } else {
                                    viewModel.signInEmail(email, password)
                                }
                            },
                            enabled = !authLoading && email.isNotBlank() && password.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = CalloraViolet),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_submit_button")
                        ) {
                            if (authLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                            } else {
                                Text(
                                    text = if (isSignUp) "Create Account" else "Sign In",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Divider with "OR"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Color(0xFF2E3547))
                        )
                        Text("OR", color = Color(0xFF64748B), fontSize = 10.sp)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Color(0xFF2E3547))
                        )
                    }

                    // Google OAuth Button (Supabase GoTrue OAuth)
                    Button(
                        onClick = {
                            viewModel.launchGoogleOAuth(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF23273A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("google_oauth_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = CalloraCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Continue with Google OAuth",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Quick Fill for Development / Demo (abdulbasitkamboh009@gmail.com)
                    TextButton(
                        onClick = {
                            email = "abdulbasitkamboh009@gmail.com"
                            password = "Password@123"
                            fullName = "Abdul Basit Kamboh"
                            phone = "+92 300 1234567"
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "Fill Default Account Credentials",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }

                    // Toggle Sign In vs Sign Up
                    TextButton(
                        onClick = { isSignUp = !isSignUp },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = if (isSignUp) "Already have an account? Sign In" else "New to CallOra? Create Account",
                            color = CalloraViolet,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Forgot Password Dialog
        if (showForgotPasswordDialog) {
            var resetEmail by remember { mutableStateOf(email) }
            AlertDialog(
                onDismissRequest = { showForgotPasswordDialog = false },
                title = { Text("Reset Password") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Enter your registered CallOra email to receive recovery instructions:")
                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
                            label = { Text("Email Address") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetPassword(resetEmail)
                            showForgotPasswordDialog = false
                        },
                        enabled = resetEmail.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = CalloraViolet)
                    ) {
                        Text("Send Reset Link")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
